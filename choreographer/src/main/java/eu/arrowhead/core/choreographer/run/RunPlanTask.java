package eu.arrowhead.core.choreographer.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunTask extends Thread {

    //=================================================================================================
    // members

    private final Logger logger = LogManager.getLogger(RunTask.class);

    //=================================================================================================
    // methods

    //=================================================================================================
    @Override
    public void run() {
        logger.debug("RunPlanTask.run started...");


    }
}
