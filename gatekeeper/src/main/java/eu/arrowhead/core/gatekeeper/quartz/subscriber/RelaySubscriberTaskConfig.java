package eu.arrowhead.core.gatekeeper.quartz.subscriber;

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
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class RelaySubscriberTaskConfig {

	//=================================================================================================
	// members
	
	private Logger logger = LogManager.getLogger(RelaySubscriberTaskConfig.class);
	
	@Autowired
	private ApplicationContext applicationContext;

	@Value(CommonConstants.$RELAY_CHECK_INTERVAL_WD)
	private int schedulerInterval;
	
	private static final int SCHEDULER_DELAY = 2;
	
	static final String NAME_OF_TRIGGER = "Relay_Subscriber_Task_Trigger";
	private static final String NAME_OF_TASK = "Relay_Subscriber_Task";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SchedulerFactoryBean relaySubscriberTaskScheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setJobFactory(jobFactory);
		schedulerFactory.setJobDetails(relaySubscriberTaskDetail().getObject());
		schedulerFactory.setTriggers(relaySubscriberTaskTrigger().getObject());
		schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
		logger.info("Relay Subscriber task scheduled.");
        
		return schedulerFactory;        
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SimpleTriggerFactoryBean relaySubscriberTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(relaySubscriberTaskDetail().getObject());
        trigger.setRepeatInterval(schedulerInterval * CommonConstants.CONVERSION_MILLISECOND_TO_SECOND);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(NAME_OF_TRIGGER);
        
        return trigger;
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JobDetailFactoryBean relaySubscriberTaskDetail() {
        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(RelaySubscriberTask.class);
        jobDetailFactory.setName(NAME_OF_TASK);
        jobDetailFactory.setDurability(true);
        
        return jobDetailFactory;
    }
}