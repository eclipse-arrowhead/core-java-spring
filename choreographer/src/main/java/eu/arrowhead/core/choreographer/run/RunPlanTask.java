package eu.arrowhead.core.choreographer.run;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunPlanTask extends Thread {

    //=================================================================================================
    // members

    private final Logger logger = LogManager.getLogger(RunPlanTask.class);

    private final ChoreographerPlan plan;
    private final ChoreographerSession session;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public RunPlanTask(ChoreographerPlan plan, ChoreographerSession session) {
        this.plan = plan;
        this.session = session;
    }


    //-------------------------------------------------------------------------------------------------
    @Override
    public void run() {
        logger.debug("RunPlanTask.run started...");

        ChoreographerAction currentAction = plan.getFirstAction();

        while (currentAction != null) {
            runAction(currentAction.getId());
            currentAction = currentAction.getNextAction();
        }
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    public void runAction(final long id) {

    }

    public void runStep(final long id) {

    }

    public void runFirstStep(final long id) {

    }
}
