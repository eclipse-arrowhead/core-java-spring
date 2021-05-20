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

package eu.arrowhead.core.choreographer.jms;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.dto.internal.ChoreographerStatusType;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
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
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Receiver {

    //=================================================================================================
    // members

    private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @Autowired
    private HttpService httpService;

    @Autowired
    protected CoreSystemRegistrationProperties coreSystemRegistrationProperties;

    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String,Object> arrowheadContext;

    @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
    private boolean sslEnabled;

    private final Logger logger = LogManager.getLogger(Receiver.class);

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
        ChoreographerAction firstAction = plan.getFirstAction();
        Set<ChoreographerStep> firstSteps = new HashSet<>(firstAction.getFirstStepEntries());

        choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.RUNNING);

        firstSteps.parallelStream().forEach(firstStep -> {
            try {
                runStep(firstStep, sessionId);
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
                            runStep(firstStepInNewAction, sessionId);
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
                    runStep(nextStep.getNextStepEntry(), sessionId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //insertInitiatedRunningStep(nextStep.getNextStepEntry().getId(), sessionId);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    public void runStep(ChoreographerStep step, long sessionId) throws InterruptedException {
        logger.debug("Running " + step.getId() + "     " + step.getName() + "       sessionId: " + sessionId + "!");

        ChoreographerRunningStep runningStep = insertInitiatedRunningStep(step.getId(), sessionId);

        ServiceQueryFormDTO serviceQuery = new ServiceQueryFormDTO();
        serviceQuery.setServiceDefinitionRequirement(step.getServiceName().toLowerCase());

        if (sslEnabled) {
            final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
            requesterSystem.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }

        final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO.Builder(requesterSystem)
                                                                                             .requestedService(serviceQuery)
                                                                                             .flag(OrchestrationFlags.Flag.MATCHMAKING, true)
                                                                                             .flag(OrchestrationFlags.Flag.OVERRIDE_STORE, true)
                                                                                             .flag(OrchestrationFlags.Flag.TRIGGER_INTER_CLOUD, false)
                                                                                             .build();

        final OrchestrationResponseDTO orchestrationResponse = queryOrchestrator(orchestrationForm);

        List<OrchestrationResultDTO> orchestrationResultList = orchestrationResponse.getResponse();

        logger.debug(orchestrationResultList);

        ChoreographerSessionRunningStepDataDTO runningStepDataDTO = new ChoreographerSessionRunningStepDataDTO(sessionId, runningStep.getId());

        if (!orchestrationResultList.isEmpty()) {
            OrchestrationResultDTO orchestrationResult = orchestrationResultList.get(0);
            UriComponents uri = Utilities.createURI(orchestrationResult.getProvider().getAuthenticationInfo(),
                    orchestrationResult.getProvider().getAddress(),
                    orchestrationResult.getProvider().getPort(),
                    orchestrationResult.getServiceUri());

            httpService.sendRequest(uri, HttpMethod.POST, Void.class, runningStepDataDTO);
        } else {
            choreographerDBService.setRunningStepStatus(runningStep.getId(),ChoreographerStatusType.ABORTED,
                    "Step aborted because the Orchestrator couldn't find any suitable providers. Rerun the plan when there are suitable providers for all steps!");
            choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.ABORTED);
        }
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunningStep insertInitiatedRunningStep(final long stepId, final long sessionId) {
        return choreographerDBService.registerRunningStep(stepId, sessionId, ChoreographerStatusType.RUNNING, "Step running is initiated and search for provider started.");
    }

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
}
