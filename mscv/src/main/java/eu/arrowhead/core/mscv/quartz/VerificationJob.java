package eu.arrowhead.core.mscv.quartz;

import eu.arrowhead.core.mscv.service.VerificationExecutionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class VerificationJob implements Job {

    public static final String LIST_ID = "VERIFICATION_LIST_ID";
    public static final String TARGET_ID = "TARGET_ID";
    private final Logger logger = LogManager.getLogger();

    private final VerificationExecutionService verificationExecutionService;

    @Autowired
    public VerificationJob(final VerificationExecutionService verificationExecutionService) {
        super();
        this.verificationExecutionService = verificationExecutionService;
    }

    @Override
    public void execute(final JobExecutionContext context) {
        try {
            final JobDataMap dataMap = context.getMergedJobDataMap();
            final long listId = dataMap.getLong(LIST_ID);
            final long targetId = dataMap.getLong(TARGET_ID);
            logger.info("Executing VerificationJob for list '{}' and target '{}'", listId, targetId);
            verificationExecutionService.executeByIdAndTarget(listId, targetId);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
