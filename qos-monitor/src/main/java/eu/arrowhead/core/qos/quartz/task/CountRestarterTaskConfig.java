/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.quartz.task;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class CountRestarterTaskConfig {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(CountRestarterTaskConfig.class);

	@Autowired
	private ApplicationContext applicationContext; //NOSONAR

	private static final int SCHEDULER_DELAY = 7;
	private static final String NUM_OF_THREADS = "1";
	
	private static final String CRON_EXPRESSION_MIDNIGHT_EVERY_DAY= "0 0 0 ? * * *";
	private static final String NAME_OF_TRIGGER = "Counter_Restart_Task_Trigger";
	private static final String NAME_OF_TASK = "Counter_Restart_Task_Detail";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	public SchedulerFactoryBean counterRestarterTaskSheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);

		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		final Properties schedulerProperties = new Properties();     
		schedulerProperties.put(CoreCommonConstants.QUARTZ_THREAD_PROPERTY, NUM_OF_THREADS);
	    schedulerFactory.setQuartzProperties(schedulerProperties);

		schedulerFactory.setJobFactory(jobFactory);
		schedulerFactory.setJobDetails(counterRestartTaskDetails().getObject());
		schedulerFactory.setTriggers(counterRestartTaskTrigger().getObject());
		schedulerFactory.setStartupDelay(SCHEDULER_DELAY);

		logger.info("CountRestarterTask task adjusted.");

		return schedulerFactory;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	public CronTriggerFactoryBean counterRestartTaskTrigger() {

		CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
		trigger.setJobDetail(counterRestartTaskDetails().getObject());
		trigger.setCronExpression(CRON_EXPRESSION_MIDNIGHT_EVERY_DAY);
		trigger.setName(NAME_OF_TRIGGER);

		return trigger;
	}

	//-------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean counterRestartTaskDetails() {
		final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(CountRestarterTask.class);
		jobDetailFactory.setName(NAME_OF_TASK);
		jobDetailFactory.setDurability(true);

		return jobDetailFactory;
	}
}