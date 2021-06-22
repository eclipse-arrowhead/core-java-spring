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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepDetail;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.dto.internal.ChoreographerStatusType;
import eu.arrowhead.common.dto.internal.ChoreographerSuitableExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChoreographerService {

    //=================================================================================================
    // members

    private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @Autowired
    private ChoreographerDriver choreographerDriver;

    @Autowired
    private HttpService httpService;

    @Autowired
    protected CoreSystemRegistrationProperties coreSystemRegistrationProperties;

    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String,Object> arrowheadContext;

    @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
    private boolean sslEnabled;

    private final Logger logger = LogManager.getLogger(ChoreographerService.class);

    private SystemRequestDTO requesterSystem;

    //=================================================================================================
    // methods

    @PostConstruct
    public void init() {
        requesterSystem = new SystemRequestDTO();
        requesterSystem.setSystemName(coreSystemRegistrationProperties.getCoreSystemName().toLowerCase());
        requesterSystem.setAddress(coreSystemRegistrationProperties.getCoreSystemDomainName());
        requesterSystem.setPort(coreSystemRegistrationProperties.getCoreSystemDomainPort());
    }

    //-------------------------------------------------------------------------------------------------
    @JmsListener(destination = "start-session")
    public void receiveStartSessionMessage(ChoreographerStartSessionDTO startSessionDTO) {
        long sessionId = startSessionDTO.getSessionId();

        ChoreographerPlan plan = choreographerDBService.getPlanById(startSessionDTO.getPlanId());

        System.out.println("Plan with session id of " + startSessionDTO.getSessionId() + " and planId of " + startSessionDTO.getPlanId() + " started successfully");

        //TODO: Get the first step or first steps in a plan and search for appropriate executors than call the Executor if available.
        //TODO: If after starting a plan a step doesn't have a corresponding Executor put the plan execution to hold !! (Can be restarted manually later or periodically automatically.)

        ChoreographerAction firstAction = plan.getFirstAction();
        Set<ChoreographerStep> firstSteps = new HashSet<>(firstAction.getFirstStepEntries());

        choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.RUNNING);

        firstSteps.parallelStream().forEach(firstStep -> {
            try {
                //runStep(firstStep, sessionId);
                executeStep(firstStep, sessionId);
            } catch (InterruptedException e) {
                choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.ABORTED);
                logger.debug(e.getMessage(), e);
            }
        });
    }

    //-------------------------------------------------------------------------------------------------
    @JmsListener(destination = "session-step-done")
    public void receiveSessionStepDoneMessage(ChoreographerSessionRunningStepDataDTO sessionFinishedStepDataDTO) {
        long sessionId = sessionFinishedStepDataDTO.getSessionId();
        long runningStepId = sessionFinishedStepDataDTO.getRunningStepId();

        //System.out.println(sessionFinishedStepDataDTO.getSessionId() + " " + sessionFinishedStepDataDTO.getRunningStepId());

        choreographerDBService.setRunningStepStatus(runningStepId, ChoreographerStatusType.DONE, "Step execution is done.");

        ChoreographerRunningStep runningStep = choreographerDBService.getRunningStepById(runningStepId);
        ChoreographerStep currentStep = choreographerDBService.getStepById(runningStep.getStep().getId());

        //System.out.println(currentStep.getName());

        if (currentStep.getNextSteps().isEmpty()) {
            boolean canGoToNextAction = true;
            logger.debug("Step has no next steps therefore it should be checked if can go to next action.");
            ChoreographerAction currentAction = currentStep.getAction();

            List<ChoreographerRunningStep> currentRunningStepList = choreographerDBService.getAllRunningStepsBySessionId(sessionId);

            for (ChoreographerRunningStep runningStepInstance : currentRunningStepList) {
                //System.out.println(runningStepInstance.getId());
                if (!runningStepInstance.getStatus().equals(ChoreographerStatusType.DONE)) {
                    //System.out.println("canGoToNextStep should be set to false");
                    canGoToNextAction = false;
                    break;
                }
                if (runningStepInstance.getStep().getNextSteps().isEmpty() && !runningStep.getStatus().equals(ChoreographerStatusType.DONE)) {
                    //System.out.println("-------------------  canGoToNextAction should be set to false!!!! --------------------");
                    canGoToNextAction = false;
                    break;
                }
            }

            if (canGoToNextAction) {
                // System.out.println("If there is next Action then it should run now!");
                ChoreographerAction nextAction = currentAction.getNextAction();
                if (nextAction != null) {
                    Set<ChoreographerStep> firstStepsInNewAction = new HashSet<>(nextAction.getFirstStepEntries());

                    firstStepsInNewAction.parallelStream().forEach(firstStepInNewAction -> {
                        try {
                            executeStep(firstStepInNewAction, sessionId);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }

        for (ChoreographerStepNextStepConnection nextStep : currentStep.getNextSteps()) {
            boolean allPreviousStepsDone = true;

            //System.out.println(nextStep.getNextStepEntry().getName() + " in for");
            // Check if all previous steps of the next step are done.
            for (ChoreographerStepNextStepConnection prevStep : nextStep.getNextStepEntry().getSteps()) {
                //System.out.println(prevStep.getId() + "    " + prevStep.getStepEntry().getName());
                ChoreographerRunningStep prevRunningStep = choreographerDBService.getRunningStepBySessionIdAndStepId(sessionId, prevStep.getStepEntry().getId());
                if (!prevRunningStep.getStatus().equals(ChoreographerStatusType.DONE)) {
                    allPreviousStepsDone = false;
                }
            }

            if (allPreviousStepsDone) {
                // Run next step
                try {
                    executeStep(nextStep.getNextStepEntry(), sessionId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //insertInitiatedRunningStep(nextStep.getNextStepEntry().getId(), sessionId);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    @JmsListener(destination = "session-step-error")
    public void receiveSessionStepErrorMessage(ChoreographerSessionRunningStepDataDTO sessionRunningStepDataDTO) {
        //TODO: Implement logic corresponding to what happens when a running step in a session returns from an executor with an error.
    }

    public void executeStep(ChoreographerStep step, long sessionId) throws InterruptedException {
        logger.debug("Execution of step with the id of " + step.getId() + " and sessionId of " + sessionId + " started.");

        ChoreographerRunningStep runningStep = insertInitiatedRunningStep(step.getId(), sessionId);

        ChoreographerSuitableExecutorResponseDTO suitableExecutors = choreographerDBService.getSuitableExecutorIdsByStepId(step.getId());

        if (suitableExecutors.getSuitableExecutorIds().isEmpty()) {
            //TODO: Research how error handling works in JMS.
        }

        final long executorId = getRandomElementFromList(suitableExecutors.getSuitableExecutorIds());

        System.out.println(executorId);

        List<ChoreographerStepDetail> preconditionStepDetails = step.getStepDetails().stream().filter(t -> t.getType().equals("PRECONDITION")).collect(Collectors.toList());
        ChoreographerStepDetail mainStepDetail = step.getStepDetails().stream().filter(t -> t.getType().equals("MAIN")).collect(Collectors.toList()).get(0);

        final List<OrchestrationResponseDTO> preconditionOrchestrationResponses = new ArrayList<>();
        OrchestrationResponseDTO mainStepOrchestrationResponse = new OrchestrationResponseDTO();

        try {
            if (!preconditionStepDetails.isEmpty()) {
                for (ChoreographerStepDetail stepDetail : preconditionStepDetails) {
                    preconditionOrchestrationResponses.add(queryOrchestrator(createOrchestrationFormRequestFromString(stepDetail.getDto())));
                }
            }
            mainStepOrchestrationResponse = queryOrchestrator(createOrchestrationFormRequestFromString(mainStepDetail.getDto()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // TODO: Maybe check later that each Orchestration request returned at least one response.

        // TODO: Delete the following line.
        System.out.println(mainStepOrchestrationResponse.getResponse().get(0).getProvider().toString());

        ChoreographerExecutorResponseDTO executor = choreographerDBService.getExecutorEntryByIdResponse(executorId);

        UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, executor.getAddress(), executor.getPort(), executor.getBaseUri() + "/start");

        ChoreographerSessionRunningStepDataDTO runningStepDataDTO = new ChoreographerSessionRunningStepDataDTO(sessionId, runningStep.getId(), preconditionOrchestrationResponses, mainStepOrchestrationResponse);

        try {
            httpService.sendRequest(uri, HttpMethod.POST, Void.class, runningStepDataDTO);
        } catch (UnavailableServerException ex) {
            //TODO: Needs handling, possibly retrying and / or choosing another viable Executor from the list.
            System.out.println("Executor nem online!!");

            //TODO: Check how the following code sequence should play out.
            //choreographerDBService.setRunningStepStatus(runningStep.getId(),ChoreographerStatusType.ABORTED,
            //        "Step aborted because the Orchestrator couldn't find any suitable providers. Rerun the plan when there are suitable providers for all steps!");
            //choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.ABORTED);
        }
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunningStep insertInitiatedRunningStep(final long stepId, final long sessionId) {
        return choreographerDBService.registerRunningStep(stepId, sessionId, ChoreographerStatusType.RUNNING, "Step running is initiated and search for provider started.");
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    public OrchestrationResponseDTO queryOrchestrator(final OrchestrationFormRequestDTO form) {
        logger.debug("queryOrchestrator started...");

        Assert.notNull(form, "form is null.");

        final UriComponents orchestrationProcessUri = getOrchestrationProcessUri();
        final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(orchestrationProcessUri, HttpMethod.POST, OrchestrationResponseDTO.class, form);

        return response.getBody();
    }

    //-------------------------------------------------------------------------------------------------
    private UriComponents getOrchestrationProcessUri() {
        logger.debug("getOrchestrationProcessUri started...");

        if (arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)) {
            try {
                return (UriComponents) arrowheadContext.get(ORCHESTRATION_PROCESS_URI_KEY);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find orchestration process URI.");
            }
        }
        throw new ArrowheadException("Choreographer can't find orchestration process URI.");
    }

    public long getRandomElementFromList(List<Long> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public OrchestrationFormRequestDTO createOrchestrationFormRequestFromString(String dto) throws IOException {
        OrchestrationFormRequestDTO orchestrationForm = new ObjectMapper().readValue(dto, OrchestrationFormRequestDTO.class);

        orchestrationForm.setRequesterSystem(requesterSystem);
        OrchestrationFlags orchestrationFlags = orchestrationForm.getOrchestrationFlags();
        orchestrationFlags.put(OrchestrationFlags.Flag.EXTERNAL_SERVICE_REQUEST, true);
        orchestrationFlags.put(OrchestrationFlags.Flag.MATCHMAKING, true);
        orchestrationFlags.put(OrchestrationFlags.Flag.OVERRIDE_STORE, true);
        orchestrationFlags.put(OrchestrationFlags.Flag.TRIGGER_INTER_CLOUD, false);

        return orchestrationForm;
    }
}
