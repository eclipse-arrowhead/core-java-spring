package eu.arrowhead.core.serviceregistry.quartz.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class ProvidersReachabilityTaskConfig {
	
	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(ProvidersReachabilityTaskConfig.class);
	
	@Autowired
    private ApplicationContext applicationContext; //NOSONAR
	
	@Value(CoreCommonConstants.$SERVICE_REGISTRY_PING_SCHEDULED_WD)
	private boolean pingScheduled;
	
	@Value(CoreCommonConstants.$SERVICE_REGISTRY_PING_INTERVAL_WD)
	private int pingInterval;
	
	private static final int SCHEDULER_DELAY = 10;
	
	private static final String NAME_OF_TRIGGER = "Providers_Reachability_Task_Trigger";
	private static final String NAME_OF_TASK = "Providers_Reachability_Task_Detail";
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
    public SchedulerFactoryBean providersReachabilityTaskScheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		
		if (pingScheduled) {			
	        schedulerFactory.setJobFactory(jobFactory);
	        schedulerFactory.setJobDetails(providersReachabilityTaskDetail().getObject());
	        schedulerFactory.setTriggers(providersReachabilityTaskTrigger().getObject());
	        schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
	        logger.info("Providers reachabilitiy task adjusted with ping interval: {} minutes", pingInterval);
		} else {
			logger.info("Providers reachabilitiy task is not adjusted");
		}
		
		return schedulerFactory;        
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SimpleTriggerFactoryBean providersReachabilityTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(providersReachabilityTaskDetail().getObject());
        trigger.setRepeatInterval(pingInterval * CoreCommonConstants.CONVERSION_MILLISECOND_TO_MINUTE);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(NAME_OF_TRIGGER);
        
        return trigger;
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JobDetailFactoryBean providersReachabilityTaskDetail() {
        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ProvidersReachabilityTask.class);
        jobDetailFactory.setName(NAME_OF_TASK);
        jobDetailFactory.setDurability(true);
        
        return jobDetailFactory;
    }
}