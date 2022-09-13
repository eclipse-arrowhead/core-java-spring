package eu.arrowhead.core.mscv.quartz;

import eu.arrowhead.common.CoreCommonConstants;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class QuartzJobConfiguration {

    private final int schedulerInterval;

    public QuartzJobConfiguration(@Value(CoreCommonConstants.$URI_CRAWLER_INTERVAL_WD) final int schedulerInterval) {
        this.schedulerInterval = schedulerInterval;
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    public Trigger uriCrawlerTaskTrigger() {
        return TriggerBuilder.newTrigger()
                             .forJob(uriCrawlerTaskJob())
                             .withIdentity(UriCrawlerTask.class.getSimpleName())
                             .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(schedulerInterval))
                             .build();
    }

    @Bean
    public JobDetail uriCrawlerTaskJob() {
        return JobBuilder.newJob(UriCrawlerTask.class)
                         .withIdentity(UriCrawlerTask.class.getSimpleName())
                         .storeDurably()
                         .build();
    }
}
