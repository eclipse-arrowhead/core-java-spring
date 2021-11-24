/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.gateway.quartz;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class RelayConnectionRemovalTaskConfig {
	
	//=================================================================================================
	// members
	
	private Logger logger = LogManager.getLogger(RelayConnectionRemovalTaskConfig.class);
	
	@Autowired
	private ApplicationContext applicationContext;

	public static final int SCHEDULER_INTERVAL = 10;
	
	private static final int SCHEDULER_DELAY = 2;
	private static final int NUM_OF_THREADS = 1;
	
	private static final String NAME_OF_TRIGGER = "Relay_Connection_Removal_Task_Trigger";
	private static final String NAME_OF_TASK = "Relay_Connection_Removal_Task";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SchedulerFactoryBean relayConnectionRemovalTaskScheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		final Properties schedulerProperties = new Properties();     
		schedulerProperties.put(CoreCommonConstants.QUARTZ_THREAD_PROPERTY, String.valueOf(NUM_OF_THREADS));
	    schedulerFactory.setQuartzProperties(schedulerProperties);
		
		schedulerFactory.setJobFactory(jobFactory);
		schedulerFactory.setJobDetails(relayConnectionRemovalTaskDetail().getObject());
		schedulerFactory.setTriggers(relayConnectionRemovalTaskTrigger().getObject());
		schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
		logger.info("Relay Connection removal task scheduled.");
        
		return schedulerFactory;        
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SimpleTriggerFactoryBean relayConnectionRemovalTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(relayConnectionRemovalTaskDetail().getObject());
        trigger.setRepeatInterval(SCHEDULER_INTERVAL * CoreCommonConstants.CONVERSION_MILLISECOND_TO_SECOND);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(NAME_OF_TRIGGER);
        
        return trigger;
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JobDetailFactoryBean relayConnectionRemovalTaskDetail() {
        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(RelayConnectionRemovalTask.class);
        jobDetailFactory.setName(NAME_OF_TASK);
        jobDetailFactory.setDurability(true);
        
        return jobDetailFactory;
    }
}
