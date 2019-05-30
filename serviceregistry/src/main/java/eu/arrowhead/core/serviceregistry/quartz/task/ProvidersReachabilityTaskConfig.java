package eu.arrowhead.core.serviceregistry.quartz.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import eu.arrowhead.core.serviceregistry.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class ProvidersReachabilityTaskConfig {
	
	protected Logger logger = LogManager.getLogger(ProvidersReachabilityTaskConfig.class);
	
	@Autowired
    private ApplicationContext applicationContext;
	
	@Value ("${ping_scheduled}")
	private boolean pingScheduled;
	
	@Value ("${ping_interval}")
	private int pingInterval;
	
	@Bean
    public SchedulerFactoryBean providersReachabilityTaskScheduler() {
		AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
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
		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(providersReachabilityTaskDetail().getObject());
        trigger.setRepeatInterval(pingInterval * 60000);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName("Providers_Reachability_Task_Trigger");
        return trigger;
    }
	
	@Bean
    public JobDetailFactoryBean providersReachabilityTaskDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ProvidersReachabilityTask.class);
        jobDetailFactory.setName("Providers_Reachability_Task_Detail");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }
	
}
