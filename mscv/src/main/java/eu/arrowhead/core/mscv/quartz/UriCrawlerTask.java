package eu.arrowhead.core.mscv.quartz;

import java.util.List;
import java.util.Objects;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.quartz.QuartzUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class UriCrawlerTask implements Job {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(UriCrawlerTask.class);

    private final DriverUtilities driver;
    private final List<CoreSystemService> requiredServices;

    public UriCrawlerTask(final DriverUtilities driver) {
        this.driver = driver;
        requiredServices = driver.getContext(CoreCommonConstants.REQUIRED_URI_LIST, List.of());
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Override
    public void execute(final JobExecutionContext context) {
        logger.debug("executing {}", getClass().getSimpleName());

        if (Objects.nonNull(driver.getContext(CoreCommonConstants.SERVER_STANDALONE_MODE))) {
            QuartzUtilities.silentlyDeleteJob(context);
            return;
        }

        if (requiredServices.isEmpty()) {
            QuartzUtilities.silentlyDeleteJob(context);
            return;
        }

        int count = 0;
        for (final CoreSystemService coreSystemService : requiredServices) {

            try {
                driver.findUriByServiceRegistry(coreSystemService);
                count++;
            } catch (final Exception e) {
                logger.debug("Unable to find {}: {}", coreSystemService, e.getClass());
            }
        }

        logger.debug("finished {}. Number of acquired URI: {}/{}",
                     getClass().getSimpleName(), count, requiredServices.size());

        logger.debug("FINISHED: URI crawler task. Number of acquired URI: {}/{}", count, requiredServices.size());

        if (count == requiredServices.size()) {
            QuartzUtilities.silentlyDeleteJob(context);
        }
    }
}
