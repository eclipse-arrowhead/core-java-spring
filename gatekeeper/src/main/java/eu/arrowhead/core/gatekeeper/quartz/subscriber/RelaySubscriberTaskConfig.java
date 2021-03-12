/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.gatekeeper.quartz.subscriber;

import java.util.Properties;

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
public class RelaySubscriberTaskConfig {

	//=================================================================================================
	// members
	
	private Logger logger = LogManager.getLogger(RelaySubscriberTaskConfig.class);
	
	@Autowired
	private ApplicationContext applicationContext;

	@Value(CoreCommonConstants.$RELAY_CHECK_INTERVAL_WD)
	private int schedulerInterval;
	
	private static final int SCHEDULER_DELAY = 2;
	private static final String NUM_OF_THREADS = "10";
	
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
		final Properties schedulerProperties = new Properties();     
		schedulerProperties.put(CoreCommonConstants.QUARTZ_THREAD_PROPERTY, NUM_OF_THREADS);
	    schedulerFactory.setQuartzProperties(schedulerProperties);
		
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
        trigger.setRepeatInterval(schedulerInterval * CoreCommonConstants.CONVERSION_MILLISECOND_TO_SECOND);
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