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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.ChoreographerAbortStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecuteStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionException;
import eu.arrowhead.core.choreographer.executor.ExecutorData;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;

@Service
public class ChoreographerService {

    //=================================================================================================
    // members
	
	public static final String START_SESSION_DESTINATION = "start-session";
	public static final String SESSION_STEP_DONE_DESTINATION = "session-step-done";
	
	private static final String START_SESSION_MSG = "Plan execution is started.";
	private static final String ABORT_SESSION_MSG = "Plan execution is aborted.";
	private static final String FINISH_SESSION_MSG = "Plan execution is finished.";
	
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

    private final Logger logger = LogManager.getLogger(ChoreographerService.class);

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @JmsListener(destination = START_SESSION_DESTINATION)
    public void receiveStartSessionMessage(final ChoreographerStartSessionDTO startSessionDTO) {  //TODO: test this
    	logger.debug("receiveStartSessionMessage started...");
    	Assert.notNull(startSessionDTO, "Payload is null.");

        final long sessionId = startSessionDTO.getSessionId();

        try {
	        final ChoreographerPlan plan = planDBService.getPlanById(startSessionDTO.getPlanId());
	        sessionDBService.worklog(plan.getName(), sessionId, START_SESSION_MSG, null);
	        final ChoreographerSession session = sessionDBService.changeSessionStatus(sessionId, ChoreographerSessionStatus.RUNNING, null);
	        sendNotification(session, START_SESSION_MSG, null);
	        
	        selectExecutorsForPlan(sessionId, plan, startSessionDTO.isAllowInterCloud(), startSessionDTO.getChooseOptimalExecutor());
	        
	        final ChoreographerAction firstAction = plan.getFirstAction();
	        executeAction(sessionId, firstAction);
        } catch (final ChoreographerSessionException ex) {
        	throw ex;
        } catch (final Throwable t) {
        	throw new ChoreographerSessionException(sessionId, t);
        }
    }

	//-------------------------------------------------------------------------------------------------
    @JmsListener(destination = SESSION_STEP_DONE_DESTINATION)
    public void receiveSessionStepDoneMessage(final ChoreographerExecutedStepResultDTO payload) {
    	logger.debug("receiveSessionStepDoneMessage started...");
    	
    	validateNotifyPayload(payload);
    	
    	switch (payload.getStatus()) {
    	case SUCCESS: handleSessionStepSuccess(payload); 
    				  break;
    	case ABORTED: handleSessionStepAborted(payload);
    				  break;
    	case ERROR:
    	case FATAL_ERROR: handleSessionStepError(payload);
    					  break;
    	default:
    		throw new IllegalArgumentException("Invalid status: " + payload.getStatus());
    	}
    }
    
    //-------------------------------------------------------------------------------------------------
	public void abortSession(final long sessionId, final Long sessionStepId, final String message) {
		logger.debug("abortSession started...");
		
		final ChoreographerSession session = sessionDBService.getSessionById(sessionId);
		final List<ChoreographerSessionStep> activeSteps = sessionDBService.abortSession(sessionId, message.trim());
		if (sessionStepId != null) {
			releaseGatewayTunnels(session.getId(), sessionStepId);
		}

		for (final ChoreographerSessionStep sessionStep : activeSteps) {
			if (sessionStepId != null && sessionStepId.longValue() == sessionStep.getId()) {
				// this step causes the abort so there is no need to abort its executor
				continue;
			}
			
			final ChoreographerExecutor executor = sessionStep.getExecutor();
			final ChoreographerAbortStepRequestDTO payload = new ChoreographerAbortStepRequestDTO(sessionId, sessionStep.getId());
			try {
				driver.abortExecutor(executor.getAddress(), executor.getPort(), executor.getBaseUri(), payload);
			} catch (final Exception ex) {
				logger.warn("Unable to send abort message - " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				logger.debug(ex);
				final ChoreographerStep step = sessionStep.getStep();
				sessionDBService.worklog(session.getPlan().getName(), step.getAction().getName(), step.getName(), sessionId, "Unable to send abort message to the executor", ex.getMessage());
			}
		}
		
		sessionDataStorage.remove(sessionId);
		sessionDBService.worklog(session.getPlan().getName(), sessionId, "Session is aborted", null);
		sendNotification(session, ABORT_SESSION_MSG, message.trim());
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
	private void selectExecutorsForPlan(final long sessionId, final ChoreographerPlan plan, final boolean allowInterCloud, final boolean chooseOptimalExecutor) {
		logger.debug("selectExecutorsForPlan started...");
		
		final List<ChoreographerStep> steps = planDBService.collectStepsFromPlan(plan);
		final SessionExecutorCache cache = new SessionExecutorCache(allowInterCloud, chooseOptimalExecutor);
		sessionDataStorage.put(sessionId, cache);
		
		for (final ChoreographerStep step : steps) {
			ExecutorData executorData = cache.get(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion());
			if (executorData == null) {
				executorData = executorSelector.selectAndInit(sessionId, step, cache.getExclusions(), allowInterCloud, chooseOptimalExecutor, true);
				if (executorData == null) { // means we can't execute at least one of the steps currently 
					throw new ChoreographerSessionException(sessionId, "Can't find properly working executor for step: " + createFullyQualifiedStepName(step));
				}
				cache.put(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion(), executorData);
			} else {
				// because we found an executor without the selector, we have to create the session step record manually
				sessionDBService.registerSessionStep(sessionId, step.getId(), executorData.getExecutor().getId());
			}
		}
		sessionDBService.worklog(plan.getName(), sessionId, "Found executors to all steps.", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String createFullyQualifiedStepName(final ChoreographerStep step) {
		logger.debug("createFullyQualifiedStepName started...");
		
		return step.getAction().getPlan().getName() + CommonConstants.DOT + step.getAction().getName() + CommonConstants.DOT + step.getName();
	}
	
	//-------------------------------------------------------------------------------------------------
    private void executeStep(final ChoreographerStep step, final long sessionId) { //TODO: test this (one of the public method), because we change the orch order, remove token regeneration etc
    	logger.debug("executeStep started...");
    	logger.debug("Execution of step with the id of " + step.getId() + " and sessionId of " + sessionId + " started.");
	
    	final String fullStepName = createFullyQualifiedStepName(step);
		final ChoreographerSessionStep sessionStep = sessionDBService.changeSessionStepStatus(sessionId, step, ChoreographerSessionStepStatus.RUNNING, "Running step: " + fullStepName);

		final SessionExecutorCache cache = sessionDataStorage.get(sessionId);
		ExecutorData executorData = cache.get(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion());
		ChoreographerExecutor executor = executorData.getExecutor();	
		
		if (sessionStep.getExecutor().getId() != executor.getId()) {
			sessionDBService.changeSessionStepExecutor(sessionStep.getId(), executor.getId());
		}
		
		final List<OrchestrationResultDTO> executorPreconditions = getOrchestrationResultsForExecutorPreconditions(step, sessionStep);
		
		// executor can change during the precondition orchestrations => refreshing from the cache
		executorData = cache.get(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion());
		executor = executorData.getExecutor();	
		
		OrchestrationResponseDTO mainOrchestrationResponseDTO = null;
		try {
			final ChoreographerServiceQueryFormDTO form = Utilities.fromJson(step.getSrTemplate(), ChoreographerServiceQueryFormDTO.class);
			mainOrchestrationResponseDTO = driver.queryOrchestrator(createOrchestrationFormRequestFromServiceQueryForm(executorData.getExecutorSystem(), form, cache.isAllowInterCloud() && !form.isLocalCloudOnly()));
			if (mainOrchestrationResponseDTO.getResponse().isEmpty()) { // no providers for the step
        		closeGatewayTunnelsIfNecessary(executorPreconditions);

				throw new ChoreographerSessionException(sessionId, sessionStep.getId(), "No providers found for step: " + fullStepName);
			}
		} catch (final ChoreographerSessionException ex) {
			throw ex;
		} catch (final Exception ex) { // problem during orchestration
			closeGatewayTunnelsIfNecessary(executorPreconditions);

			throw new ChoreographerSessionException(sessionId, sessionStep.getId(), "Problem occured while orchestration for step " + fullStepName, ex);
		}		
		
		final List<OrchestrationResultDTO> allOrchestration = new ArrayList<>(executorPreconditions);
		allOrchestration.add(mainOrchestrationResponseDTO.getResponse().get(0));
		final List<Integer> gatewayTunnelPorts = collectGatewayTunnelPorts(allOrchestration);
		cache.getGatewayTunnels().put(sessionStep.getId(), gatewayTunnelPorts == null ? List.of() : gatewayTunnelPorts);
		
		final ChoreographerExecuteStepRequestDTO payload = new ChoreographerExecuteStepRequestDTO(sessionId,
																								  sessionStep.getId(),
																								  executorPreconditions,
																								  mainOrchestrationResponseDTO.getResponse().get(0),
																								  step.getQuantity(),
																								  Utilities.text2Map(step.getStaticParameters()));
		try {
			driver.startExecutor(executor.getAddress(), executor.getPort(), executor.getBaseUri(), payload);
		} catch (final Exception ex) {
			closeGatewayTunnelsIfNecessary(allOrchestration);
			cache.getGatewayTunnels().remove(sessionStep.getId());
			
			throw ex;
		}
    }
    
    //-------------------------------------------------------------------------------------------------
	private List<OrchestrationResultDTO> getOrchestrationResultsForExecutorPreconditions(final ChoreographerStep step, final ChoreographerSessionStep sessionStep) {
    	logger.debug("getOrchestrationResultsForExecutorPreconditions started...");
    	
    	final List<OrchestrationResultDTO> result = new ArrayList<>();
    	final SessionExecutorCache cache = sessionDataStorage.get(sessionStep.getSession().getId());
    	final String serviceDefinition = step.getServiceDefinition();
		final int minVersion = step.getMinVersion() == null ? Defaults.DEFAULT_VERSION : step.getMinVersion(); 
		final int maxVersion = step.getMaxVersion() == null ? Integer.MAX_VALUE : step.getMaxVersion();
    	
		ExecutorData executorData = cache.get(serviceDefinition, minVersion, maxVersion);
    	while (executorData != null) {
    		result.clear();
    		try {
    			for (final ChoreographerServiceQueryFormDTO form : executorData.getDependencyForms()) {
					final OrchestrationFormRequestDTO orchestrationForm = createOrchestrationFormRequestFromServiceQueryForm(executorData.getExecutorSystem(), form, cache.isAllowInterCloud() && !form.isLocalCloudOnly());
					final OrchestrationResponseDTO response = driver.queryOrchestrator(orchestrationForm);
					if (response.getResponse().isEmpty()) {
						// no provider for a dependency
						
		        		cache.remove(serviceDefinition, minVersion, maxVersion);
		        		cache.getExclusions().add(executorData.getExecutor().getId());
		        		closeGatewayTunnelsIfNecessary(result);

		        		break;
					}
					result.add(response.getResponse().get(0));
				}
    			
    			if (result.size() == executorData.getDependencyForms().size()) {
    				return result;
    			}
    		} catch (final Exception ex) {
    	   		logger.warn("Unable to orchestrate precondition service for an executor - " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        		logger.debug(ex);
        		
        		cache.remove(serviceDefinition, minVersion, maxVersion);
        		cache.getExclusions().add(executorData.getExecutor().getId());
        		closeGatewayTunnelsIfNecessary(result);
    		}
    		
    		executorData = executorSelector.selectAndInit(sessionStep.getSession().getId(), step, cache.getExclusions(), cache.isAllowInterCloud(), cache.getChooseOptimalExecutor(), false);
    		if (executorData != null) {
    			cache.put(serviceDefinition, minVersion, maxVersion, executorData);
    			sessionDBService.changeSessionStepExecutor(sessionStep.getId(), executorData.getExecutor().getId());
    		}
    	}
		
    	throw new ChoreographerSessionException(sessionStep.getSession().getId(), sessionStep.getId(), "Can't find properly working executor for step: " + createFullyQualifiedStepName(step));
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationFormRequestDTO createOrchestrationFormRequestFromServiceQueryForm(final SystemRequestDTO executorSystem, final ChoreographerServiceQueryFormDTO form, final boolean allowInterCloud) {
    	logger.debug("createOrchestrationFormRequestFromServiceQueryForm started...");

	    final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();
	    orchestrationForm.setRequestedService(form);
	    orchestrationForm.setRequesterSystem(executorSystem);
	    
	    final OrchestrationFlags orchestrationFlags = orchestrationForm.getOrchestrationFlags();
	    orchestrationFlags.put(OrchestrationFlags.Flag.MATCHMAKING, true);
	    orchestrationFlags.put(OrchestrationFlags.Flag.OVERRIDE_STORE, true);
    	orchestrationFlags.put(OrchestrationFlags.Flag.ENABLE_INTER_CLOUD, allowInterCloud);
	
	    return orchestrationForm;
	}

	//-------------------------------------------------------------------------------------------------
	private void validateNotifyPayload(final ChoreographerExecutedStepResultDTO payload) {
		logger.debug("validatePayload started...");
		
		Assert.notNull(payload, "Payload is null.");
		Assert.isTrue(payload.getSessionId() != null && payload.getSessionId() > 0, "Invalid session id.");
		Assert.isTrue(payload.getSessionStepId() != null && payload.getSessionStepId() > 0, "Invalid session step id.");
		Assert.notNull(payload.getStatus(), "Status is null.");
		
		if (payload.getStatus().isError()) {
			Assert.isTrue(!Utilities.isEmpty(payload.getMessage()), "Message is null or blank.");
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	private void handleSessionStepError(final ChoreographerExecutedStepResultDTO payload) {
		logger.debug("handleSessionStepError started...");
		
		final String exception = payload.getException() == null ? "" : payload.getException();
		if (payload.getStatus().isFatal()) {
			abortSession(payload.getSessionId(), payload.getSessionStepId(), payload.getMessage() + " " + exception);
		} else {
			// error is not fatal, maybe an other executor can able to do the step
			final ChoreographerSessionStep sessionStep = sessionDBService.getSessionStepById(payload.getSessionStepId());
			final ChoreographerStep step = sessionStep.getStep();
			sessionDBService.worklog(sessionStep.getSession().getPlan().getName(), step.getAction().getName(), step.getName(), payload.getSessionId(), payload.getMessage(), payload.getException());
			releaseGatewayTunnels(sessionStep.getSession().getId(), sessionStep.getId());
			
			final SessionExecutorCache cache = sessionDataStorage.get(payload.getSessionId());
			cache.remove(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion());
			cache.getExclusions().add(sessionStep.getExecutor().getId());
			
			final ExecutorData executorData = executorSelector.selectAndInit(payload.getSessionId(), step, cache.getExclusions(), cache.isAllowInterCloud(), cache.getChooseOptimalExecutor(), false);
			if (executorData != null) {
				cache.put(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion(), executorData);
				sessionDBService.changeSessionStepExecutor(sessionStep.getId(), executorData.getExecutor().getId());
				executeStep(step, payload.getSessionId());
			} else {
				// no replacement executor so we have to abort
				abortSession(payload.getSessionId(), payload.getSessionStepId(), payload.getMessage() + " " + exception);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void handleSessionStepAborted(final ChoreographerExecutedStepResultDTO payload) {
		logger.debug("handleSessionStepAborted started...");
		
		final ChoreographerSessionStep sessionStep = sessionDBService.getSessionStepById(payload.getSessionStepId());
		releaseGatewayTunnels(sessionStep.getSession().getId(), sessionStep.getId());
		final ChoreographerPlan plan = sessionStep.getSession().getPlan();
		final ChoreographerStep step = sessionStep.getStep();
		sessionDBService.worklog(plan.getName(), step.getAction().getName(), step.getName(), payload.getSessionId(), "The executor of this step has aborted successfully." , null);
	}

	//-------------------------------------------------------------------------------------------------
	private void handleSessionStepSuccess(final ChoreographerExecutedStepResultDTO payload) {
		logger.debug("handleSessionStepSuccess started...");
		
		final long sessionId = payload.getSessionId();
		final ChoreographerSessionStep finishedSessionStep = sessionDBService.changeSessionStepStatus(payload.getSessionStepId(), ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
		
		releaseGatewayTunnels(sessionId, finishedSessionStep.getId());
		
		final ChoreographerStep finishedStep = finishedSessionStep.getStep();
		final Set<ChoreographerStep> nextSteps = finishedStep.getNextSteps();
		
		if (nextSteps.isEmpty()) {
			// we need to check if we can go to the next action
			final ChoreographerAction currentAction = finishedStep.getAction();
			final boolean canProceedToNextAction = canProceedToNextAction(sessionId, currentAction.getId());
			if (canProceedToNextAction) {
				final ChoreographerAction nextAction = currentAction.getNextAction();
				if (nextAction != null) {
					executeAction(sessionId, nextAction);
				} else {
					// this was the last step of the last action => session is done
					sessionDone(sessionId);
				}
			}
		} else {
			final List<ChoreographerStep> executableSteps = new ArrayList<>();
			
			for (final ChoreographerStep nextStep : nextSteps) {
				// next step is only executable if all of its previous steps are done
				final List<ChoreographerSessionStep> previousSessionSteps = sessionDBService.getSessionStepBySessionIdAndSteps(sessionId, nextStep.getPreviousSteps());
				boolean executable = true;
				for (final ChoreographerSessionStep sessionStep : previousSessionSteps) {
					if (ChoreographerSessionStepStatus.DONE != sessionStep.getStatus()) {
						executable = false;
						break;
					}
				}
				
				if (executable) {
					executableSteps.add(nextStep);
				}
			}
			
			if (!executableSteps.isEmpty()) {
				executableSteps.parallelStream().forEach(step -> {
		            try {
		                executeStep(step, sessionId);
		            } catch (final ChoreographerSessionException ex) {
		            	logger.debug(ex);
		            	throw ex;
		            } catch (final Throwable t) {
		            	logger.debug(t);
		            	throw new ChoreographerSessionException(sessionId, t);
		            }
		        });
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private boolean canProceedToNextAction(final long sessionId, final long actionId) {
		logger.debug("canProceedToNextAction started...");

		final List<ChoreographerSessionStep> sessionSteps = sessionDBService.getAllSessionStepBySessionIdAndActionId(sessionId, actionId);
		for (final ChoreographerSessionStep sessionStep : sessionSteps) {
			if (ChoreographerSessionStepStatus.DONE != sessionStep.getStatus()) {
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void executeAction(final long sessionId, final ChoreographerAction action) {
		logger.debug("executeAction started...");
		
		final Set<ChoreographerStep> firstSteps = new HashSet<>(planDBService.getFirstSteps(action));
		
        firstSteps.parallelStream().forEach(step -> {
            try {
                executeStep(step, sessionId);
            } catch (final ChoreographerSessionException ex) {
            	logger.debug(ex);
            	throw ex;
            } catch (final Throwable t) {
            	logger.debug(t);
            	throw new ChoreographerSessionException(sessionId, t);
            }
        });
	}
	
	//-------------------------------------------------------------------------------------------------
	private void sessionDone(final long sessionId) {
		logger.debug("sessionDone started...");
		
		final ChoreographerSession session = sessionDBService.changeSessionStatus(sessionId, ChoreographerSessionStatus.DONE, null);
		sendNotification(session, FINISH_SESSION_MSG, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Integer> collectGatewayTunnelPorts(final List<OrchestrationResultDTO> orchResults) {
		logger.debug("collectGatewayTunnelPorts started...");
		
		final List<Integer> ports = new ArrayList<>();
		for (final OrchestrationResultDTO result : orchResults) {
			if (result.getWarnings().contains(OrchestratorWarnings.VIA_GATEWAY) && result.getProvider().getSystemName().equalsIgnoreCase(CoreSystem.GATEWAY.name())) {
				ports.add(result.getProvider().getPort());
			}
		}
		
		return ports.isEmpty() ? null : ports;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void closeGatewayTunnelsIfNecessary(List<OrchestrationResultDTO> orchResults) {
		logger.debug("closeTunnelsIfNecessary started...");
		
		final List<Integer> ports = collectGatewayTunnelPorts(orchResults);
		if (ports != null) {
			driver.closeGatewayTunnels(ports);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void releaseGatewayTunnels(final long sessionId, final long sessionStepId) {
		logger.debug("releaseGatewayTunnels started...");
		
		final SessionExecutorCache cache = sessionDataStorage.get(sessionId);
		final List<Integer> ports = cache.getGatewayTunnels().get(sessionStepId);
		
		if (!Utilities.isEmpty(ports)) {
			driver.closeGatewayTunnels(ports);
		}
		
		cache.getGatewayTunnels().remove(sessionStepId);
	}
}