package eu.arrowhead.common.quartz.uricrawler;



import java.util.Map;

import javax.annotation.Resource;

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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.quartz.AutoWiringSpringBeanQuartzTaskFactory;

@Configuration
public class UriCrawlerTaskConfig {

	//=================================================================================================
	// members
	
	private Logger logger = LogManager.getLogger(UriCrawlerTaskConfig.class);
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	private static final int SCHEDULER_DELAY = 16;
	private static final int SCHEDULER_INTERVAL = 30; // TODO: make property from this one
	
	static final String NAME_OF_TRIGGER = "URI_Crawler_Task_Trigger";
	private static final String NAME_OF_TASK = "URI_Crawler_Task";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SchedulerFactoryBean uriCrawlerTaskScheduler() {
		final AutoWiringSpringBeanQuartzTaskFactory jobFactory = new AutoWiringSpringBeanQuartzTaskFactory();
		jobFactory.setApplicationContext(applicationContext);
		
		final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_STANDALONE_MODE)) {
			schedulerFactory.setJobFactory(jobFactory);
			schedulerFactory.setJobDetails(uriCrawlerTaskDetail().getObject());
			schedulerFactory.setTriggers(uriCrawlerTaskTrigger().getObject());
			schedulerFactory.setStartupDelay(SCHEDULER_DELAY);
			logger.info("URI Crawler task scheduled.");
		} else {
			logger.info("URI Crawler task is not scheduled in standalone mode.");
		}
        
		return schedulerFactory;        
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public SimpleTriggerFactoryBean uriCrawlerTaskTrigger() {
		final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(uriCrawlerTaskDetail().getObject());
        trigger.setRepeatInterval(SCHEDULER_INTERVAL * CommonConstants.CONVERSION_MILLISECOND_TO_SECOND);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName(NAME_OF_TRIGGER);
        
        return trigger;
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JobDetailFactoryBean uriCrawlerTaskDetail() {
        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(UriCrawlerTask.class);
        jobDetailFactory.setName(NAME_OF_TASK);
        jobDetailFactory.setDurability(true);
        
        return jobDetailFactory;
    }
}