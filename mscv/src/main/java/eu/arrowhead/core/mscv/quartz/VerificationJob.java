package eu.arrowhead.core.mscv.quartz;

import eu.arrowhead.core.mscv.service.MscvExecutionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class VerificationJob implements Job {

    public static final String LIST_ID = "VERIFICATION_LIST_ID";
    public static final String TARGET_ID = "TARGET_ID";
    private final Logger logger = LogManager.getLogger();

    private MscvExecutionService executionService;

    public VerificationJob() { super(); }

    @Override
    public void execute(final JobExecutionContext context) {
        logger.info("test");
        try {
            context.getScheduler().deleteJob(context.getJobDetail().getKey());
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
