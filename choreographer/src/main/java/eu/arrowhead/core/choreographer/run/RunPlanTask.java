package eu.arrowhead.core.choreographer.run;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.database.repository.ChoreographerRunningStepRepository;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class RunPlanTask extends Thread {

    //=================================================================================================
    // members

    private final Logger logger = LogManager.getLogger(RunPlanTask.class);

    private final ChoreographerPlan plan;
    private final ChoreographerSession session;

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @Autowired
    private ChoreographerRunningStepRepository choreographerRunningStepRepository;

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
            try {
                runAction(currentAction);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentAction = currentAction.getNextAction();
        }
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    public void runAction(final ChoreographerAction action) throws InterruptedException {
        Set<ChoreographerStep> currentSteps = new HashSet<>(action.getFirstStepEntries());
        for (ChoreographerStep firstStep : currentSteps) {
            for (int i = 0; i < firstStep.getQuantity(); i++) {
                runFirstStep(firstStep);
            }
        }

        while(!currentSteps.isEmpty()) {
            Set<ChoreographerStep> stepsToAdd = new HashSet<>();
            currentSteps.parallelStream().forEach(step -> {
                if (step.getNextSteps() != null) {
                    step.getNextSteps().parallelStream().forEach(connection -> {
                        stepsToAdd.add(connection.getNextStepEntry());

                    });
                }
            });
            currentSteps = stepsToAdd;
            currentSteps.parallelStream().forEach(step -> {
                try {
                    runStep(step);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    //-------------------------------------------------------------------------------------------------
    public void runStep(ChoreographerStep step) throws InterruptedException {
        boolean allPreviousStepsDone = true;
        for (ChoreographerStepNextStepConnection conn : step.getSteps()) {
            System.out.println("Previous  step id: " + conn.getStepEntry().getId() + "   name: " + step.getName());
            final Optional<ChoreographerRunningStep> runningStep = choreographerRunningStepRepository.findByStepIdAndSessionId(conn.getStepEntry().getId(), session.getId());
            if (runningStep.isPresent()) {
                if (!runningStep.get().getStatus().equals("Done")) {
                    allPreviousStepsDone = false;
                }
            }
        }
        if (allPreviousStepsDone) {
            System.out.println("Running " + step.getId() + "     " + step.getName() + "sessionId: " + session.getId() + "!");
            ChoreographerRunningStep runningStep = insertRunningStep(step.getId(), session.getId());
            Random rand = new Random();
            int randInt = rand.nextInt(15);
            Thread.sleep(new Random().nextInt(randInt * 1000 + 1000));
            choreographerDBService.setRunningStepToDone(runningStep.getId());
        }
    }

    //-------------------------------------------------------------------------------------------------
    public void runFirstStep(ChoreographerStep firstStep) throws InterruptedException {
        System.out.println("Running " + firstStep.getId() + "     " + firstStep.getName() + "       sessionId: " + session.getId() + "!");
        ChoreographerRunningStep runningStep = insertRunningStep(firstStep.getId(), session.getId());
        Random rand = new Random();
        int randInt = rand.nextInt(15);
        Thread.sleep(new Random().nextInt(randInt * 1000 + 1000));
        choreographerDBService.setRunningStepToDone(runningStep.getId());
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunningStep insertRunningStep(final long stepId, final long sessionId) {
        return choreographerDBService.registerRunningStep(stepId, sessionId, "Running", "Step is running.");
    }
}
