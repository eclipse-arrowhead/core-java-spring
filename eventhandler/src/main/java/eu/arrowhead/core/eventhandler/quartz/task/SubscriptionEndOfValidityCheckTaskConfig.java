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

package eu.arrowhead.core.eventhandler.quartz.task;

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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class SubscriptionEndOfValidityCheckTaskConfig {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(SubscriptionEndOfValidityCheckTaskConfig.class);
	
	@Autowired
    private ApplicationContext applicationContext; //NOSONAR
	
	@Value(CoreCommonConstants.$EVENTHANDLER_TTL_SCHEDULED_WD)
	private boolean ttlScheduled;
	
	@Value(CoreCommonConstants.$EVENTHANDLER_TTL_INTERVAL_WD)
	private int ttlInterval;
	
	private static final int SCHEDULER_DELAY = 17;
	private static final String NUM_OF_THREADS = "1";
	
	private static final String NAME_OF_TRIGGER = "Subscription_End_OF_Validity_Check_Task_Trigger";
	private static final String NAME_OF_TASK = "Subscription_End_OF_Validity_Check_Task_Detail";	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	public SchedulerFactoryBean servicesEndOfValidityCheckSheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		final Properties schedulerProperties = new Properties();     
		schedulerProperties.put(CoreCommonConstants.QUARTZ_THREAD_PROPERTY, NUM_OF_THREADS);
	    schedulerFactory.setQuartzProperties(schedulerProperties);
		
		if (ttlScheduled) {			
			schedulerFactory.setJobFactory(jobFactory);
	        schedulerFactory.setJobDetails(eventFilterEndOfValidityCheckTaskDetail().getObject());
	        schedulerFactory.setTriggers(eventFilterEndOfValidityCheckTaskTrigger().getObject());
	        schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
	        logger.info("Subscription end of validity task adjusted with ttl interval: {} minutes", ttlInterval);
		} else {
			logger.info("Subscription end of validity task is not adjusted");
		}
		
		return schedulerFactory;        
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SimpleTriggerFactoryBean eventFilterEndOfValidityCheckTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(eventFilterEndOfValidityCheckTaskDetail().getObject());
        trigger.setRepeatInterval(ttlInterval * CommonConstants.CONVERSION_MILLISECOND_TO_MINUTE);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(NAME_OF_TRIGGER);
        
        return trigger;
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JobDetailFactoryBean eventFilterEndOfValidityCheckTaskDetail() {
		final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(SubscriptionEndOfValidityCheckTask.class);
        jobDetailFactory.setName(NAME_OF_TASK);
        jobDetailFactory.setDurability(true);
        
        return jobDetailFactory;
    }
}