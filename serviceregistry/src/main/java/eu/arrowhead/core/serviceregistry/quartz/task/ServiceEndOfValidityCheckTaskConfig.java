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
public class ServiceEndOfValidityCheckTaskConfig {
	
protected Logger logger = LogManager.getLogger(ProvidersReachabilityTaskConfig.class);
	
	@Autowired
    private ApplicationContext applicationContext;
	
	@Value ("${ttl_scheduled}")
	private boolean ttlScheduled;
	
	@Value ("${ttl_interval}")
	private int ttlInterval;
	
	@Bean
	public SchedulerFactoryBean servicesEndOfValidityCheckSheduler() {
		AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		if (ttlScheduled) {			
	        schedulerFactory.setJobFactory(jobFactory);
	        schedulerFactory.setJobDetails(servicesEndOfValidityCheckTaskDetail().getObject());
	        schedulerFactory.setTriggers(servicesEndOfValidityCheckTaskTrigger().getObject());
	        logger.info("Services end of validity task adjusted with ttl interval: " + ttlInterval + " minutes");
		} else {
			logger.info("Services end of validity task is not adjusted");
		}
		return schedulerFactory;        
    }
	
	@Bean
    public SimpleTriggerFactoryBean servicesEndOfValidityCheckTaskTrigger() {
		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(servicesEndOfValidityCheckTaskDetail().getObject());
        trigger.setRepeatInterval(ttlInterval * 60000);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName("Services_End_OF_Validity_Check_Task_Trigger");
        return trigger;
    }
	
	@Bean
    public JobDetailFactoryBean servicesEndOfValidityCheckTaskDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ServiceEndOfValidityCheckTask.class);
        jobDetailFactory.setName("Services_End_OF_Validity_Check_Task_Detail");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }
}
