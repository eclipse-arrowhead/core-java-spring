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
public class ServiceEndOfValidityCheckTaskConfig {
	
	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(ProvidersReachabilityTaskConfig.class);
	
	@Autowired
    private ApplicationContext applicationContext; //NOSONAR
	
	@Value(CoreCommonConstants.$SERVICE_REGISTRY_TTL_SCHEDULED_WD)
	private boolean ttlScheduled;
	
	@Value(CoreCommonConstants.$SERVICE_REGISTRY_TTL_INTERVAL_WD)
	private int ttlInterval;
	
	private static final int SCHEDULER_DELAY = 15;
	
	private static final String NAME_OF_TRIGGER = "Services_End_OF_Validity_Check_Task_Trigger";
	private static final String NAME_OF_TASK = "Services_End_OF_Validity_Check_Task_Detail";	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	public SchedulerFactoryBean servicesEndOfValidityCheckSheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		if (ttlScheduled) {			
	        schedulerFactory.setJobFactory(jobFactory);
	        schedulerFactory.setJobDetails(servicesEndOfValidityCheckTaskDetail().getObject());
	        schedulerFactory.setTriggers(servicesEndOfValidityCheckTaskTrigger().getObject());
	        schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
	        logger.info("Services end of validity task adjusted with ttl interval: {} minutes", ttlInterval);
		} else {
			logger.info("Services end of validity task is not adjusted");
		}
		
		return schedulerFactory;        
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SimpleTriggerFactoryBean servicesEndOfValidityCheckTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(servicesEndOfValidityCheckTaskDetail().getObject());
        trigger.setRepeatInterval(ttlInterval * CoreCommonConstants.CONVERSION_MILLISECOND_TO_MINUTE);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(NAME_OF_TRIGGER);
        
        return trigger;
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JobDetailFactoryBean servicesEndOfValidityCheckTaskDetail() {
        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ServiceEndOfValidityCheckTask.class);
        jobDetailFactory.setName(NAME_OF_TASK);
        jobDetailFactory.setDurability(true);
        
        return jobDetailFactory;
    }
}