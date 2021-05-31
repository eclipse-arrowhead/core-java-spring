package eu.arrowhead.core.mscv.quartz;

import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static eu.arrowhead.common.CoreCommonConstants.$VERIFICATION_INTERVAL_WD;

@Component
public class VerificationJobFactory {

    private final Logger logger = LogManager.getLogger();
    private final Scheduler scheduler;
    private final Integer scheduleInterval;

    public VerificationJobFactory(final Scheduler scheduler,
                                  @Value($VERIFICATION_INTERVAL_WD) final Integer scheduleInterval) {
        super();
        this.scheduler = scheduler;
        this.scheduleInterval = scheduleInterval;
    }

    //-------------------------------------------------------------------------------------------------
    public void createVerificationJob(final VerificationEntryList entryList, final Target target) throws SchedulerException {
        final String identity = createIdentity(entryList, target);
        final JobDetail jobDetail = JobBuilder.newJob(VerificationJob.class)
                                              .usingJobData(VerificationJob.LIST_ID, entryList.getId())
                                              .usingJobData(VerificationJob.TARGET_ID, target.getId())
                                              .withIdentity(identity)
                                              .build();

        final Trigger trigger = TriggerBuilder.newTrigger()
                                              .forJob(jobDetail)
                                              .withIdentity(identity)
                                              .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(scheduleInterval))
                                              .build();
        logger.info("Scheduling a new job with entryList '{}' for target '{}' with identity '{}'",
                    entryList, target, identity);
        scheduler.scheduleJob(jobDetail, trigger);
    }

    //-------------------------------------------------------------------------------------------------
    public void removeVerificationJob(final VerificationEntryList entryList, final Target target) {
        final String identity = createIdentity(entryList, target);
        final JobKey jobKey = JobKey.jobKey(identity);
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            logger.warn("Unable to delete Job {}: {}", jobKey, e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    private String createIdentity(final VerificationEntryList entryList, final Target target) {
        return String.format("%s-%s-%s", entryList.getName(), target.getName(), target.getOs());
    }
}
