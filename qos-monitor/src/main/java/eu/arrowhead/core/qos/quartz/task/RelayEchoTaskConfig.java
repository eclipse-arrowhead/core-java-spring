package eu.arrowhead.core.qos.quartz.task;

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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class RelayEchoTaskConfig {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(RelayEchoTaskConfig.class);

	@Autowired
	private ApplicationContext applicationContext; //NOSONAR

	@Value(CoreCommonConstants.$RELAY_ECHO_TTL_INTERVAL_WD)
	private int ttlInterval;

	private static final int SCHEDULER_DELAY = 10;

	private static final String NAME_OF_TRIGGER = "Relay_Echo_Task_Trigger";
	private static final String NAME_OF_TASK = "Relay_Echo_Task_Detail";
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	public SchedulerFactoryBean relayEchoScheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

		schedulerFactory.setJobFactory(jobFactory);
		schedulerFactory.setJobDetails(relayEchoTaskDetails().getObject());
		schedulerFactory.setTriggers(relayEchoTaskTrigger().getObject());
		schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
		logger.info("Relay echo task adjusted with ttl interval: {} minutes", ttlInterval);

		return schedulerFactory;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	public SimpleTriggerFactoryBean relayEchoTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(relayEchoTaskDetails().getObject());
		trigger.setRepeatInterval(ttlInterval * CommonConstants.CONVERSION_MILLISECOND_TO_MINUTE);
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		trigger.setName(NAME_OF_TRIGGER);

		return trigger;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean relayEchoTaskDetails() {
		final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(RelayEchoTask.class);
		jobDetailFactory.setName(NAME_OF_TASK);
		jobDetailFactory.setDurability(true);

		return jobDetailFactory;
	}
}
