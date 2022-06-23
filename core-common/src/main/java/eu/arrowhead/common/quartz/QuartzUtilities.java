package eu.arrowhead.common.quartz;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.util.Assert;

public class QuartzUtilities {

    private static final Logger LOGGER = LogManager.getLogger();

    private QuartzUtilities() { throw new UnsupportedOperationException(); }

    public static boolean deleteJob(final JobExecutionContext context) throws SchedulerException {
        return deleteJob(context.getScheduler(), context.getJobDetail());
    }

    public static boolean deleteJob(final Scheduler scheduler, final JobDetail jobDetail) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(jobDetail, "JobDetail must not be null");
        LOGGER.debug("Deleting Job {}", jobDetail.getKey());
        return scheduler.deleteJob(jobDetail.getKey());
    }

    public static boolean unscheduleJob(final JobExecutionContext context) throws SchedulerException {
        Assert.notNull(context, "JobExecutionContext must not be null");
        return unscheduleJob(context.getScheduler(), context.getTrigger());
    }

    public static boolean unscheduleJob(final Scheduler scheduler, final Trigger trigger) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(trigger, "Trigger must not be null");
        LOGGER.debug("Unscheduling Job {}", trigger.getKey());
        return scheduler.unscheduleJob(trigger.getKey());
    }

    public static ZonedDateTime rescheduleJob(final JobExecutionContext context, final Trigger newTrigger) throws SchedulerException {
        Assert.notNull(context, "JobExecutionContext must not be null");
        return rescheduleJob(context.getScheduler(), context.getTrigger(), newTrigger);
    }

    public static ZonedDateTime rescheduleJob(final Scheduler scheduler, final Trigger oldTrigger, final Trigger newTrigger) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(oldTrigger, "Existing trigger must not be null");
        Assert.notNull(newTrigger, "New trigger must not be null");
        LOGGER.debug("Rescheduling Job {}", oldTrigger.getKey());
        final Date date = scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.systemDefault());
    }

    public static void pauseJob(final JobExecutionContext context) throws SchedulerException {
        Assert.notNull(context, "JobExecutionContext must not be null");
        pauseJob(context.getScheduler(), context.getJobDetail());
    }

    public static void pauseJob(final Scheduler scheduler, final JobDetail jobDetail) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(jobDetail, "JobDetail must not be null");
        LOGGER.debug("Pausing Job {}", jobDetail.getKey());
        scheduler.pauseJob(jobDetail.getKey());
    }

    public static void pauseTrigger(final JobExecutionContext context, final Trigger trigger) throws SchedulerException {
        Assert.notNull(context, "JobExecutionContext must not be null");
        pauseTrigger(context.getScheduler(), trigger);
    }

    public static void pauseTrigger(final Scheduler scheduler, final Trigger trigger) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(trigger, "Trigger must not be null");
        LOGGER.debug("Pausing Trigger {}", trigger.getKey());
        scheduler.pauseTrigger(trigger.getKey());
    }

    public static void resumeJob(final JobExecutionContext context) throws SchedulerException {
        Assert.notNull(context, "JobExecutionContext must not be null");
        resumeJob(context.getScheduler(), context.getJobDetail());
    }

    public static void resumeJob(final Scheduler scheduler, final JobDetail jobDetail) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(jobDetail, "JobDetail must not be null");
        LOGGER.debug("Resuming Job {}", jobDetail.getKey());
        scheduler.resumeJob(jobDetail.getKey());
    }

    public static void resumeTrigger(final JobExecutionContext context, final Trigger trigger) throws SchedulerException {
        Assert.notNull(context, "JobExecutionContext must not be null");
        resumeTrigger(context.getScheduler(), trigger);
    }

    public static void resumeTrigger(final Scheduler scheduler, final Trigger trigger) throws SchedulerException {
        Assert.notNull(scheduler, "Scheduler must not be null");
        Assert.notNull(trigger, "Trigger must not be null");
        LOGGER.debug("Resuming Trigger {}", trigger.getKey());
        scheduler.pauseTrigger(trigger.getKey());
    }


    public static boolean silentlyDeleteJob(final JobExecutionContext context) {
        if (Objects.isNull(context)) {
            return false;
        } else {
            return silentlyDeleteJob(context.getScheduler(), context.getJobDetail());
        }
    }

    public static boolean silentlyDeleteJob(final Scheduler scheduler, final JobDetail jobDetail) {
        try {
            return deleteJob(scheduler, jobDetail);
        } catch (final SchedulerException e) {
            LOGGER.warn("Unable to delete Job {}: {}", jobDetail.getKey(), e.getMessage(), e);
            return false;
        }
    }

    public static boolean silentlyUnscheduleJob(final JobExecutionContext context) {
        if (Objects.isNull(context)) {
            return false;
        } else {
            return silentlyUnscheduleJob(context.getScheduler(), context.getTrigger());
        }
    }

    public static boolean silentlyUnscheduleJob(final Scheduler scheduler, final Trigger trigger) {
        try {
            return unscheduleJob(scheduler, trigger);
        } catch (final SchedulerException e) {
            LOGGER.warn("Unable to unschedule Job {}: {}", trigger.getKey(), e.getMessage(), e);
            return false;
        }
    }

    public static ZonedDateTime silentlyRescheduleJob(final JobExecutionContext context, final Trigger newTrigger) {
        if (Objects.isNull(context)) {
            return null;
        } else {
            return silentlyRescheduleJob(context.getScheduler(), context.getTrigger(), newTrigger);
        }
    }

    public static ZonedDateTime silentlyRescheduleJob(final Scheduler scheduler, final Trigger oldTrigger, final Trigger newTrigger) {
        try {
            return rescheduleJob(scheduler, oldTrigger, newTrigger);
        } catch (SchedulerException e) {
            LOGGER.warn("Unable to reschedule Job {}: {}", oldTrigger.getKey(), e.getMessage(), e);
            return null;
        }
    }

    public static void silentlyPauseJob(final JobExecutionContext context) {
        if (Objects.nonNull(context)) {
            silentlyPauseJob(context.getScheduler(), context.getJobDetail());
        }
    }

    public static void silentlyPauseJob(final Scheduler scheduler, final JobDetail jobDetail) {
        try {
            pauseJob(scheduler, jobDetail);
        } catch (SchedulerException e) {
            LOGGER.warn("Unable to pause Job {}: {}", jobDetail.getKey(), e.getMessage(), e);
        }
    }

    public static void silentlyPauseTrigger(final JobExecutionContext context, final Trigger trigger) {
        if (Objects.nonNull(context)) {
            silentlyPauseTrigger(context.getScheduler(), trigger);
        }
    }

    public static void silentlyPauseTrigger(final Scheduler scheduler, final Trigger trigger) {
        try {
            pauseTrigger(scheduler, trigger);
        } catch (SchedulerException e) {
            LOGGER.warn("Unable to pause Trigger {}: {}", trigger.getKey(), e.getMessage(), e);
        }
    }

    public static void silentlyResumeJob(final JobExecutionContext context) {
        if (Objects.nonNull(context)) {
            silentlyResumeJob(context.getScheduler(), context.getJobDetail());
        }
    }

    public static void silentlyResumeJob(final Scheduler scheduler, final JobDetail jobDetail) {
        try {
            resumeJob(scheduler, jobDetail);
        } catch (SchedulerException e) {
            LOGGER.warn("Unable to resume Job {}: {}", jobDetail.getKey(), e.getMessage(), e);
        }
    }

    public static void silentlyResumeTrigger(final JobExecutionContext context, final Trigger trigger) {
        if (Objects.nonNull(context)) {
            silentlyResumeTrigger(context.getScheduler(), trigger);
        }
    }

    public static void silentlyResumeTrigger(final Scheduler scheduler, final Trigger trigger) {
        try {
            resumeTrigger(scheduler, trigger);
        } catch (SchedulerException e) {
            LOGGER.warn("Unable to resume Trigger {}: {}", trigger.getKey(), e.getMessage(), e);
        }
    }
}
