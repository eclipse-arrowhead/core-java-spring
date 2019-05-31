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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.core.serviceregistry.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class ProvidersReachabilityTaskConfig {
	
	protected Logger logger = LogManager.getLogger(ProvidersReachabilityTaskConfig.class);
	
	@Autowired
    private ApplicationContext applicationContext;
	
	@Value (CommonConstants.$SERVICE_REGISTRY_PING_SCHEDULED_WD)
	private boolean pingScheduled;
	
	@Value (CommonConstants.$SERVICE_REGISTRY_PING_INTERVAL_WD)
	private int pingInterval;
	
	private final String nameOfTrigger = "Providers_Reachability_Task_Trigger";
	private final String nameOfTask = "Providers_Reachability_Task_Detail";
	
	@Bean
    public SchedulerFactoryBean providersReachabilityTaskScheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		if (pingScheduled) {			
	        schedulerFactory.setJobFactory(jobFactory);
	        schedulerFactory.setJobDetails(providersReachabilityTaskDetail().getObject());
	        schedulerFactory.setTriggers(providersReachabilityTaskTrigger().getObject());
	        logger.info("Providers reachabilitiy task adjusted with ping interval: " + pingInterval + " minutes");
		} else {
			logger.info("Providers reachabilitiy task is not adjusted");
		}
		return schedulerFactory;        
    }
	
	@Bean
    public SimpleTriggerFactoryBean providersReachabilityTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(providersReachabilityTaskDetail().getObject());
        trigger.setRepeatInterval(pingInterval * 60000);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(nameOfTrigger);
        return trigger;
    }
	
	@Bean
    public JobDetailFactoryBean providersReachabilityTaskDetail() {
        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ProvidersReachabilityTask.class);
        jobDetailFactory.setName(nameOfTask);
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }
	
}
