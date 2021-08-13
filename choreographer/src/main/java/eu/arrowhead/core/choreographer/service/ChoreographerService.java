/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.choreographer.service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionException;

@Service
public class ChoreographerService {

    //=================================================================================================
    // members
	
	public static final String START_SESSION_DESTINATION = "start-session";
	
	private static final String START_SESSION_MSG = "Plan execution started.";

    @Autowired
    private ChoreographerPlanDBService planDBService;
    
    @Autowired
    private ChoreographerSessionDBService sessionDBService;

    @Autowired
    private ChoreographerDriver driver;

    @Autowired
    protected CoreSystemRegistrationProperties registrationProperties;

    private SystemRequestDTO requesterSystem;
    
    private final Logger logger = LogManager.getLogger(ChoreographerService.class);

    //=================================================================================================
    // methods

    @PostConstruct
    public void init() {
        requesterSystem = new SystemRequestDTO();
        requesterSystem.setSystemName(registrationProperties.getCoreSystemName().toLowerCase());
        requesterSystem.setAddress(registrationProperties.getCoreSystemDomainName());
        requesterSystem.setPort(registrationProperties.getCoreSystemDomainPort());
    }

    //-------------------------------------------------------------------------------------------------
    @JmsListener(destination = START_SESSION_DESTINATION)
    public void receiveStartSessionMessage(final ChoreographerStartSessionDTO startSessionDTO) {
        final long sessionId = startSessionDTO.getSessionId();

        try {
	        final ChoreographerPlan plan = planDBService.getPlanById(startSessionDTO.getPlanId());
	        sessionDBService.worklog(plan.getName(), sessionId, START_SESSION_MSG, null);
	        final ChoreographerSession session = sessionDBService.changeSessionStatus(sessionId, ChoreographerSessionStatus.RUNNING);
	        sendNotification(session, START_SESSION_MSG, null);
	        
	        selectExecutorsForPlan(sessionId, plan);
	        
	        //TODO: continue
	
//	        final ChoreographerAction firstAction = plan.getFirstAction();
//	        final Set<ChoreographerStep> firstSteps = new HashSet<>(firstAction.getFirstStepEntries());
//	
//	
//	        firstSteps.parallelStream().forEach(firstStep -> {
//	            try {
//	                executeStep(firstStep, sessionId);
//	            } catch (InterruptedException e) {
//	                choreographerDBService.setSessionStatus(sessionId, ChoreographerSessionStatus.ABORTED);
//	                logger.debug(e.getMessage(), e);
//	            }
//	        });
        } catch (final ChoreographerSessionException ex) {
        	throw ex;
        } catch (final Throwable t) {
        	throw new ChoreographerSessionException(sessionId, t);
        }
    }

	//-------------------------------------------------------------------------------------------------
    @JmsListener(destination = "session-step-done")
    public void receiveSessionStepDoneMessage(final ChoreographerSessionRunningStepDataDTO sessionFinishedStepDataDTO) {
//        long sessionId = sessionFinishedStepDataDTO.getSessionId();
//        long runningStepId = sessionFinishedStepDataDTO.getRunningStepId();
//
//        //System.out.println(sessionFinishedStepDataDTO.getSessionId() + " " + sessionFinishedStepDataDTO.getRunningStepId());
//
//        choreographerDBService.setRunningStepStatus(runningStepId, ChoreographerSessionStatus.DONE, "Step execution is done.");
//
//        ChoreographerSessionStep runningStep = choreographerDBService.getRunningStepById(runningStepId);
//        ChoreographerStep currentStep = choreographerDBService.getStepById(runningStep.getStep().getId());
//
//        //System.out.println(currentStep.getName());
//
//        if (currentStep.getNextSteps().isEmpty()) {
//            boolean canGoToNextAction = true;
//            logger.debug("Step has no next steps therefore it should be checked if can go to next action.");
//            ChoreographerAction currentAction = currentStep.getAction();
//
//            List<ChoreographerSessionStep> currentRunningStepList = choreographerDBService.getAllRunningStepsBySessionId(sessionId);
//
//            for (ChoreographerSessionStep runningStepInstance : currentRunningStepList) {
//                //System.out.println(runningStepInstance.getId());
//                if (!runningStepInstance.getStatus().equals(ChoreographerSessionStatus.DONE)) {
//                    //System.out.println("canGoToNextStep should be set to false");
//                    canGoToNextAction = false;
//                    break;
//                }
//                if (runningStepInstance.getStep().getNextSteps().isEmpty() && !runningStep.getStatus().equals(ChoreographerSessionStatus.DONE)) {
//                    //System.out.println("-------------------  canGoToNextAction should be set to false!!!! --------------------");
//                    canGoToNextAction = false;
//                    break;
//                }
//            }
//
//            if (canGoToNextAction) {
//                // System.out.println("If there is next Action then it should run now!");
//                ChoreographerAction nextAction = currentAction.getNextAction();
//                if (nextAction != null) {
//                    Set<ChoreographerStep> firstStepsInNewAction = new HashSet<>(nextAction.getFirstStepEntries());
//
//                    firstStepsInNewAction.parallelStream().forEach(firstStepInNewAction -> {
//                        try {
//                            executeStep(firstStepInNewAction, sessionId);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                }
//            }
//        }
//
//        for (ChoreographerStepNextStepConnection nextStep : currentStep.getNextSteps()) {
//            boolean allPreviousStepsDone = true;
//
//            //System.out.println(nextStep.getNextStepEntry().getName() + " in for");
//            // Check if all previous steps of the next step are done.
//            for (ChoreographerStepNextStepConnection prevStep : nextStep.getNextStepEntry().getSteps()) {
//                //System.out.println(prevStep.getId() + "    " + prevStep.getStepEntry().getName());
//                ChoreographerSessionStep prevRunningStep = choreographerDBService.getRunningStepBySessionIdAndStepId(sessionId, prevStep.getStepEntry().getId());
//                if (!prevRunningStep.getStatus().equals(ChoreographerSessionStatus.DONE)) {
//                    allPreviousStepsDone = false;
//                }
//            }
//
//            if (allPreviousStepsDone) {
//                // Run next step
//                try {
//                    executeStep(nextStep.getNextStepEntry(), sessionId);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                //insertInitiatedRunningStep(nextStep.getNextStepEntry().getId(), sessionId);
//            }
//        }
    }

    //-------------------------------------------------------------------------------------------------
    @JmsListener(destination = "session-step-error")
    public void receiveSessionStepErrorMessage(final ChoreographerSessionRunningStepDataDTO sessionRunningStepDataDTO) {
        //TODO: Implement logic corresponding to what happens when a running step in a session returns from an executor with an error.
    }

    public void executeStep(final ChoreographerStep step, final long sessionId) throws InterruptedException {
//        logger.debug("Execution of step with the id of " + step.getId() + " and sessionId of " + sessionId + " started.");
//
//        ChoreographerSessionStep runningStep = insertInitiatedRunningStep(step.getId(), sessionId);
//
//        ChoreographerSuitableExecutorResponseDTO suitableExecutors = choreographerDBService.getSuitableExecutorIdsByStepId(step.getId());
//
//        if (suitableExecutors.getSuitableExecutorIds().isEmpty()) {
//            //TODO: Research how error handling works in JMS.
//        }
//
//        final long executorId = getRandomElementFromList(suitableExecutors.getSuitableExecutorIds());
//
//        System.out.println(executorId);
//
//        List<ChoreographerStepDetail> preconditionStepDetails = step.getStepDetails().stream().filter(t -> t.getType().equals("PRECONDITION")).collect(Collectors.toList());
//        ChoreographerStepDetail mainStepDetail = step.getStepDetails().stream().filter(t -> t.getType().equals("MAIN")).collect(Collectors.toList()).get(0);
//
//        final List<OrchestrationResponseDTO> preconditionOrchestrationResponses = new ArrayList<>();
//        OrchestrationResponseDTO mainStepOrchestrationResponse = new OrchestrationResponseDTO();
//
//        try {
//            if (!preconditionStepDetails.isEmpty()) {
//                for (ChoreographerStepDetail stepDetail : preconditionStepDetails) {
//                    preconditionOrchestrationResponses.add(queryOrchestrator(createOrchestrationFormRequestFromString(stepDetail.getDto())));
//                }
//            }
//            mainStepOrchestrationResponse = queryOrchestrator(createOrchestrationFormRequestFromString(mainStepDetail.getDto()));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        // TODO: Maybe check later that each Orchestration request returned at least one response.
//
//        // TODO: Delete the following line.
//        System.out.println(mainStepOrchestrationResponse.getResponse().get(0).getProvider().toString());
//
//        ChoreographerExecutorResponseDTO executor = choreographerDBService.getExecutorEntryByIdResponse(executorId);
//
//        UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, executor.getAddress(), executor.getPort(), executor.getBaseUri() + "/start");
//
//        ChoreographerSessionRunningStepDataDTO runningStepDataDTO = new ChoreographerSessionRunningStepDataDTO(sessionId, runningStep.getId(), preconditionOrchestrationResponses, mainStepOrchestrationResponse);
//
//        try {
//            httpService.sendRequest(uri, HttpMethod.POST, Void.class, runningStepDataDTO);
//        } catch (UnavailableServerException ex) {
//            //TODO: Needs handling, possibly retrying and / or choosing another viable Executor from the list.
//            System.out.println("Executor nem online!!");
//
//            //TODO: Check how the following code sequence should play out.
//            //choreographerDBService.setRunningStepStatus(runningStep.getId(),ChoreographerStatusType.ABORTED,
//            //        "Step aborted because the Orchestrator couldn't find any suitable providers. Rerun the plan when there are suitable providers for all steps!");
//            //choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.ABORTED);
//        }
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionStep insertInitiatedRunningStep(final long stepId, final long sessionId) {
    	//TODO: implement
    	return null;
//        return choreographerDBService.registerRunningStep(stepId, sessionId, ChoreographerSessionStatus.RUNNING, "Step running is initiated and search for provider started.");
    }

    //=================================================================================================
    // assistant methods
    
    //-------------------------------------------------------------------------------------------------
    private void sendNotification(final ChoreographerSession session, final String message, final String details) {
    	logger.debug("sendNotification started..."); 
    	
    	try {
    		if (!Utilities.isEmpty(session.getNotifyUri())) {
    			final ChoreographerNotificationDTO payload = new ChoreographerNotificationDTO(session.getId(),
    																						  session.getPlan().getId(),
    																						  session.getPlan().getName(),
    																						  Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
    																						  session.getStatus(),
    																						  message,
    																						  details);
    			driver.sendSessionNotification(session.getNotifyUri(), payload);
    		}
    	} catch (final Exception ex) {
    		// any problem in notification sending should not affect the plan execution
    		logger.warn("Unable to send notification - " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
    		logger.debug(ex);
    	}
	}
    
	//-------------------------------------------------------------------------------------------------
	private void selectExecutorsForPlan(final long sessionId, final ChoreographerPlan plan) {
		logger.debug("selectExecutorsForPlan started...");
		
		final List<ChoreographerStep> steps = planDBService.collectStepsFromPlan(plan);
		// need some storage to store an executor cache and blacklist for every session
		//TODO: continue
	}


    public long getRandomElementFromList(final List<Long> list) {
        final Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public OrchestrationFormRequestDTO createOrchestrationFormRequestFromString(final String dto) throws IOException {
        final OrchestrationFormRequestDTO orchestrationForm = new ObjectMapper().readValue(dto, OrchestrationFormRequestDTO.class);

        orchestrationForm.setRequesterSystem(requesterSystem);
        final OrchestrationFlags orchestrationFlags = orchestrationForm.getOrchestrationFlags();
        orchestrationFlags.put(OrchestrationFlags.Flag.EXTERNAL_SERVICE_REQUEST, true);
        orchestrationFlags.put(OrchestrationFlags.Flag.MATCHMAKING, true);
        orchestrationFlags.put(OrchestrationFlags.Flag.OVERRIDE_STORE, true);
        orchestrationFlags.put(OrchestrationFlags.Flag.TRIGGER_INTER_CLOUD, false);

        return orchestrationForm;
    }
}
