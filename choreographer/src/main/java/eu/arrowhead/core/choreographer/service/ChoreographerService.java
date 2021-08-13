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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionException;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;

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
    private SessionDataStorage sessionDataStorage;
    
    @Autowired
    private ExecutorSelector executorSelector;

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
	        
	        final ChoreographerAction firstAction = plan.getFirstAction();
	        final Set<ChoreographerStep> firstSteps = new HashSet<>(planDBService.getFirstSteps(firstAction));
	
	
	        firstSteps.parallelStream().forEach(step -> {
	            try {
	                executeStep(step, sessionId);
	            } catch (final ChoreographerSessionException ex) {
	            	throw ex;
	            } catch (final Throwable t) {
	            	throw new ChoreographerSessionException(sessionId, t);
	            }
	        });
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
		final SessionExecutorCache cache = new SessionExecutorCache();
		sessionDataStorage.put(sessionId, cache);
		
		for (final ChoreographerStep step : steps) {
			final ChoreographerExecutor executor = executorSelector.selectAndInit(sessionId, step, cache.getExclusions());
			if (executor == null) { // means we can't execute at least one of the steps currently 
				throw new ChoreographerSessionException(sessionId, "Can't find properly working executor for step: " + createFullyQualifiedStepName(step));
			}
			cache.put(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion(), executor);
		}
		sessionDBService.worklog(plan.getName(), sessionId, "Find executor to all steps.", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String createFullyQualifiedStepName(final ChoreographerStep step) {
		logger.debug("createFullyQualifiedStepName started...");
		
		return step.getAction().getPlan().getName() + CommonConstants.DOT + step.getAction().getName() + CommonConstants.DOT + step.getName();
	}
	
	//-------------------------------------------------------------------------------------------------
    private void executeStep(final ChoreographerStep step, final long sessionId) {
    	logger.debug("executeStep started...");
    	logger.debug("Execution of step with the id of " + step.getId() + " and sessionId of " + sessionId + " started.");
	
    	final String fullStepName = createFullyQualifiedStepName(step);
		final ChoreographerSessionStep sessionStep = sessionDBService.changeSessionStepStatus(sessionId, step, ChoreographerSessionStepStatus.RUNNING, "Running step: " + fullStepName);
		OrchestrationResponseDTO mainOrchestrationResponseDTO = null;
		try {
			mainOrchestrationResponseDTO = driver.queryOrchestrator(createOrchestrationFormRequestFromServiceQueryForm(Utilities.fromJson(step.getSrTemplate(), ServiceQueryFormDTO.class)));
			if (mainOrchestrationResponseDTO.getResponse().isEmpty()) { // no providers for the step
				throw new ChoreographerSessionException(sessionId, sessionStep.getId(), "No providers found for step " + fullStepName);
			}
		} catch (final Exception ex) { // problem during orchestration
			throw new ChoreographerSessionException(sessionId, sessionStep.getId(), "Problem occured while orchestration for step " + fullStepName, ex);
		}
		
		//TODO: continue
	
//	
//	  List<ChoreographerStepDetail> preconditionStepDetails = step.getStepDetails().stream().filter(t -> t.getType().equals("PRECONDITION")).collect(Collectors.toList());
//	  ChoreographerStepDetail mainStepDetail = step.getStepDetails().stream().filter(t -> t.getType().equals("MAIN")).collect(Collectors.toList()).get(0);
//	
//	  final List<OrchestrationResponseDTO> preconditionOrchestrationResponses = new ArrayList<>();
//	  OrchestrationResponseDTO mainStepOrchestrationResponse = new OrchestrationResponseDTO();
//	
//	  try {
//	      if (!preconditionStepDetails.isEmpty()) {
//	          for (ChoreographerStepDetail stepDetail : preconditionStepDetails) {
//	              preconditionOrchestrationResponses.add(queryOrchestrator(createOrchestrationFormRequestFromString(stepDetail.getDto())));
//	          }
//	      }
//	      mainStepOrchestrationResponse = queryOrchestrator(createOrchestrationFormRequestFromString(mainStepDetail.getDto()));
//	  } catch (IOException ex) {
//	      ex.printStackTrace();
//	  }
//	
//	  // TODO: Maybe check later that each Orchestration request returned at least one response.
//	
//	  // TODO: Delete the following line.
//	  System.out.println(mainStepOrchestrationResponse.getResponse().get(0).getProvider().toString());
//	
//	  ChoreographerExecutorResponseDTO executor = choreographerDBService.getExecutorEntryByIdResponse(executorId);
//	
//	  UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, executor.getAddress(), executor.getPort(), executor.getBaseUri() + "/start");
//	
//	  ChoreographerSessionRunningStepDataDTO runningStepDataDTO = new ChoreographerSessionRunningStepDataDTO(sessionId, sessionStep.getId(), preconditionOrchestrationResponses, mainStepOrchestrationResponse);
//	
//	  try {
//	      httpService.sendRequest(uri, HttpMethod.POST, Void.class, runningStepDataDTO);
//	  } catch (UnavailableServerException ex) {
//	      //TODO: Needs handling, possibly retrying and / or choosing another viable Executor from the list.
//	  System.out.println("Executor nem online!!");
//	
//	  //TODO: Check how the following code sequence should play out.
//	  //choreographerDBService.setRunningStepStatus(runningStep.getId(),ChoreographerStatusType.ABORTED,
//	  //        "Step aborted because the Orchestrator couldn't find any suitable providers. Rerun the plan when there are suitable providers for all steps!");
//	  //choreographerDBService.setSessionStatus(sessionId, ChoreographerStatusType.ABORTED);
//	  }
    }
    
    //-------------------------------------------------------------------------------------------------
	private List<OrchestrationResultDTO> getOrchestrationResultsForExecutorPreconditions(final ChoreographerStep step, final ChoreographerSessionStep sessionStep) {
    	logger.debug("getOrchestrationResultsForExecutorPreconditions started...");
    	
    	final List<OrchestrationResultDTO> result = new ArrayList<>();
    	final SessionExecutorCache cache = sessionDataStorage.get(sessionStep.getSession().getId());
    	final String serviceDefinition = step.getServiceDefinition();
		final int minVersion = step.getMinVersion() == null ? Defaults.DEFAULT_VERSION : step.getMinVersion(); 
		final int maxVersion = step.getMaxVersion() == null ? Integer.MAX_VALUE : step.getMaxVersion();
    	
		ChoreographerExecutor executor = cache.get(serviceDefinition, minVersion, maxVersion);
    	while (executor != null) {
    		try {
    			//TODO: maybe store this somehow when using executor selector
    			final ChoreographerExecutorServiceInfoResponseDTO serviceInfo = driver.queryExecutorServiceInfo(executor.getAddress(), executor.getPort(), executor.getBaseUri(), serviceDefinition, minVersion, maxVersion);
    			for (final ServiceQueryFormDTO form : serviceInfo.getDependencies()) {
					final OrchestrationFormRequestDTO orchestrationForm = createOrchestrationFormRequestFromServiceQueryForm(form);
					final OrchestrationResponseDTO response = driver.queryOrchestrator(orchestrationForm);
					if (response.getResponse().isEmpty()) {
						// no provider for a dependency
						
						// TODO: maybe worklog
		        		cache.remove(serviceDefinition, minVersion, maxVersion);
		        		cache.getExclusions().add(executor.getId());
		        		break;
					}
					result.add(response.getResponse().get(0));
				}
    			
    			return result;
    		} catch (final Exception ex) {
    	   		logger.warn("Unable to send notification - " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        		logger.debug(ex);
        		
				// TODO: maybe worklog
        		cache.remove(serviceDefinition, minVersion, maxVersion);
        		cache.getExclusions().add(executor.getId());
    		}
    		
    		//TODO: find another executor
    		//TODO: continue
    	}
		
    	//TODO: throw exception
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationFormRequestDTO createOrchestrationFormRequestFromServiceQueryForm(final ServiceQueryFormDTO form) {
    	logger.debug("createOrchestrationFormRequestFromServiceQueryForm started...");

	    final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();
	    orchestrationForm.setRequestedService(form);
	    orchestrationForm.setRequesterSystem(requesterSystem);
	    
	    final OrchestrationFlags orchestrationFlags = orchestrationForm.getOrchestrationFlags();
	    orchestrationFlags.put(OrchestrationFlags.Flag.EXTERNAL_SERVICE_REQUEST, true);
	    orchestrationFlags.put(OrchestrationFlags.Flag.MATCHMAKING, true);
	    orchestrationFlags.put(OrchestrationFlags.Flag.OVERRIDE_STORE, true);
	
	    return orchestrationForm;
	}
}