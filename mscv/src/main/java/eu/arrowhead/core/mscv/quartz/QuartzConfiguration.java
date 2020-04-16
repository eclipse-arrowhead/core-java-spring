package eu.arrowhead.core.mscv.quartz;

import eu.arrowhead.common.CoreCommonConstants;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

    private final int schedulerInterval;

    public QuartzConfiguration(@Value(CoreCommonConstants.$URI_CRAWLER_INTERVAL_WD) final int schedulerInterval) {
        this.schedulerInterval = schedulerInterval;
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    public Trigger uriCrawlerTaskTrigger() {
        final JobDetail jobDetail = JobBuilder.newJob(UriCrawlerTask.class)
                                              .withIdentity(UriCrawlerTask.class.getSimpleName())
                                              .build();
        return TriggerBuilder.newTrigger()
                             .forJob(jobDetail)
                             .withIdentity(UriCrawlerTask.class.getSimpleName())
                             .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(schedulerInterval))
                             .build();
    }
}
