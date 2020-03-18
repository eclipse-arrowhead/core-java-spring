package eu.arrowhead.core.choreographer.jms;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.dto.internal.ChoreographerSessionFinishedStepDataDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class Receiver {

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @JmsListener(destination = "start-session")
    public void receiveStartSessionMessage(ChoreographerStartSessionDTO startSessionDTO) {
        long sessionId = startSessionDTO.getSessionId();

        ChoreographerPlan plan = choreographerDBService.getPlanById(startSessionDTO.getPlanId());
        ChoreographerAction firstAction = plan.getFirstAction();
        Set<ChoreographerStep> firstSteps = new HashSet<>(firstAction.getFirstStepEntries());

        choreographerDBService.setSessionStatus(sessionId, "Running");

        firstSteps.parallelStream().forEach(firstStep -> {
            try {
                runFirstStep(firstStep, sessionId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @JmsListener(destination = "session-step-done")
    public void receiveSessionStepDoneMessage(ChoreographerSessionFinishedStepDataDTO sessionFinishedStepDataDTO) {
        long sessionId = sessionFinishedStepDataDTO.getSessionId();
        long runningStepId = sessionFinishedStepDataDTO.getRunningStepId();

        System.out.println(sessionFinishedStepDataDTO.getSessionId() + " " + sessionFinishedStepDataDTO.getRunningStepId());

        choreographerDBService.setRunningStepStatus(runningStepId, "Done", "Step execution is done.");

        ChoreographerRunningStep runningStep = choreographerDBService.getRunningStepById(runningStepId);
        ChoreographerStep currentStep = choreographerDBService.getStepById(runningStep.getStep().getId());

        System.out.println(currentStep.getName());

        if (currentStep.getNextSteps().isEmpty()) {
            boolean canGoToNextAction = true;
            System.out.println("Step has no next steps therefore it should be checked if can go to next action.");
            ChoreographerAction currentAction = currentStep.getAction();

            List<ChoreographerRunningStep> currentRunningStepList = choreographerDBService.getAllRunningStepsBySessionId(sessionId);

            for (ChoreographerRunningStep runningStepInstance : currentRunningStepList) {
                System.out.println(runningStepInstance.getId());
                if (!runningStepInstance.getStatus().equals("Done")) {
                    System.out.println("canGoToNextStep should be set to false");
                    canGoToNextAction = false;
                    break;
                }
                if (runningStepInstance.getStep().getNextSteps().isEmpty() && !runningStep.getStatus().equals("Done")) {
                    System.out.println("-------------------  canGoToNextAction should be set to false!!!! --------------------");
                    canGoToNextAction = false;
                    break;
                }
            }

            if (canGoToNextAction) {
                @// TODO: 2020. 03. 18. Test what happens if there is multiple Actions in a plan. 
                System.out.println("If there is next Action then it should run now! Testing needed.");
                ChoreographerAction nextAction = currentAction.getNextAction();
                if (nextAction != null) {
                    Set<ChoreographerStep> firstStepsInNewAction = new HashSet<>(nextAction.getFirstStepEntries());

                    firstStepsInNewAction.parallelStream().forEach(firstStepInNewAction -> {
                        try {
                            runFirstStep(firstStepInNewAction, sessionId);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }

        for (ChoreographerStepNextStepConnection nextStep : currentStep.getNextSteps()) {
            boolean allPreviousStepsDone = true;

            System.out.println(nextStep.getNextStepEntry().getName() + " in for");
            // Check if all previous steps of the next step are done.
            for (ChoreographerStepNextStepConnection prevStep : nextStep.getNextStepEntry().getSteps()) {
                System.out.println(prevStep.getId() + "    " + prevStep.getStepEntry().getName());
                ChoreographerRunningStep prevRunningStep = choreographerDBService.getRunningStepBySessionIdAndStepId(sessionId, prevStep.getId());
                if (!prevRunningStep.getStatus().equals("Done")) {
                    allPreviousStepsDone = false;
                }
            }

            if (allPreviousStepsDone) {
                // Run next step
                insertInitiatedRunningStep(nextStep.getNextStepEntry().getId(), sessionId);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    public void runFirstStep(ChoreographerStep firstStep, long sessionId) throws InterruptedException {
        System.out.println("Running " + firstStep.getId() + "     " + firstStep.getName() + "       sessionId: " + sessionId + "!");
        ChoreographerRunningStep runningStep = insertInitiatedRunningStep(firstStep.getId(), sessionId);
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunningStep insertInitiatedRunningStep(final long stepId, final long sessionId) {
        return choreographerDBService.registerRunningStep(stepId, sessionId, "Initiated", "Step running is initiated and search for provider started.");
    }
}
