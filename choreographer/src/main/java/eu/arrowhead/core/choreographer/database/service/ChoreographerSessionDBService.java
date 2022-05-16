/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.choreographer.database.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerWorklogRepository;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerSessionListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStepListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerWorklogListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class ChoreographerSessionDBService {

	//=================================================================================================
	// members
	
	@Autowired
	private ChoreographerPlanRepository planRepository;
	
	@Autowired
	private ChoreographerActionRepository actionRepository;
	
	@Autowired
	private ChoreographerSessionRepository sessionRepository;
	
	@Autowired
	private ChoreographerStepRepository stepRepository;
	
	@Autowired
	private ChoreographerSessionStepRepository sessionStepRepository;
	
	@Autowired
	private ChoreographerExecutorRepository executorRepository;
	
	@Autowired
	private ChoreographerWorklogRepository worklogRepository;
	
	@Value(CoreCommonConstants.$CHOREOGRAPHER_MAX_PLAN_ITERATION_WD)
	private Long maxIteration;
	
    private final Logger logger = LogManager.getLogger(ChoreographerSessionDBService.class);

	
	//=================================================================================================
	// methods
	
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession initiateSession(final long planId, final long quantity, final String notifyUri) {
        logger.debug("initiateSession started...");
        Assert.isTrue(quantity > 0, "quantity must be positive");
        
        try {
      	  final Optional<ChoreographerPlan> planOpt = planRepository.findById(planId);
      	  if (planOpt.isEmpty()) {
      		  worklogAndThrow("Initiating plan has been failed", new InvalidParameterException("Plan with id " + planId + " not exists"));
      	  }
      	  final ChoreographerPlan plan = planOpt.get();
      	  
      	  if (quantity > maxIteration) {
      		worklogAndThrow(plan.getName(), null, null, "Initiating plan has been failed", new InvalidParameterException("Requested quantity (" + quantity + ") is more than allowed (" + maxIteration + ")"));
      	  }
      	  
      	  final String _notifyUri = Utilities.isEmpty(notifyUri) ? null : UriComponentsBuilder.fromUriString(notifyUri).build().toUriString();
      	  final ChoreographerSession session = sessionRepository.saveAndFlush(new ChoreographerSession(plan, ChoreographerSessionStatus.INITIATED, quantity, _notifyUri));
      	  
      	  worklog(plan.getName(), session.getId(), session.getExecutionNumber(), "New session has been initiated", null);
      	  return session;
      
        } catch (final InvalidParameterException ex) {
      	  throw ex;
      	  
        } catch (final Exception ex) {
      	  logger.debug(ex.getMessage(), ex);
  	      throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession changeSessionStatus(final long sessionId, final ChoreographerSessionStatus status, final String errorMessage) {
        logger.debug("changeSessionStatus started...");
        Assert.notNull(status, "ChoreographerSessionStatus is null");

        try {
      	  final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
      	  if (sessionOpt.isEmpty()) {
      		  worklogAndThrow("Session status change has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
      	  }
      	  
      	  ChoreographerSession session = sessionOpt.get();
      	  if (status != session.getStatus()) {
      		  session.setStatus(status);
      		  session = sessionRepository.saveAndFlush(session);
      		  
      		  final String exception = status == ChoreographerSessionStatus.ABORTED ? errorMessage : null;
      		  worklog(session.getPlan().getName(), session.getId(), session.getExecutionNumber(), "Session status has been changed to " + status, exception);
      	  }

      	  return session;
        } catch (final InvalidParameterException ex) {
            throw ex;
            
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession increaseSessionQuantityDone(final long sessionId) {
    	logger.debug("increaseSessionQuantityDone started...");
    	
    	try {
    		final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
    		if (sessionOpt.isEmpty()) {
    			worklogAndThrow("Session quantityDone change has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
        	}
			
    		ChoreographerSession session = sessionOpt.get();
    		final long done = session.getQuantityDone() + 1;
    		if (done > session.getQuantityGoal()) {
    			worklogAndThrow(session.getPlan().getName(), session.getId(), session.getExecutionNumber(), "Session quantityDone is invalid", new InvalidParameterException("Session quantityDone is greater than quantityGoal"));
			}
    		session.setQuantityDone(done);
    		session = sessionRepository.saveAndFlush(session);
    		
    		worklog(session.getPlan().getName(), session.getId(), session.getExecutionNumber(), "Session quantityDone has been changed to " + done, null);
    		return session;
    		
    	} catch (final InvalidParameterException ex) {
            throw ex;
            
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession increaseExecutionNumber(final long sessionId) {
    	logger.debug("increaseExecutionNumber started...");
    	
    	try {
    		final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
    		if (sessionOpt.isEmpty()) {
    			worklogAndThrow("Session executionNumber change has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
        	}
			
    		ChoreographerSession session = sessionOpt.get();
    		final long executionNum = session.getExecutionNumber() + 1;
    		if (executionNum > session.getQuantityGoal()) {
    			worklogAndThrow(session.getPlan().getName(), session.getId(), session.getExecutionNumber(), "Session executionNumber is invalid", new InvalidParameterException("Session executionNumber is greater than quantityGoal"));
			}
    		session.setExecutionNumber(executionNum);
    		session = sessionRepository.saveAndFlush(session);
    		
    		worklog(session.getPlan().getName(), session.getId(), session.getExecutionNumber(), "Session executionNumber has been changed to " + executionNum, null);
    		return session;
    		
    	} catch (final InvalidParameterException ex) {
            throw ex;
            
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public List<ChoreographerSessionStep> abortSession(final long sessionId, final String message) {
        logger.debug("abortSession started...");

        try {
        	final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
      	  	if (sessionOpt.isEmpty()) {
      	  		worklogAndThrow("Session abortion has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
      	  	}
      	  
      	  	final ChoreographerSession session = sessionOpt.get();
      	  	worklog(session.getPlan().getName(), sessionId, session.getExecutionNumber(), "Session is aborting.", message);
      	  	changeSessionStatus(sessionId, ChoreographerSessionStatus.ABORTED, message);
      	  	
      	  	final List<ChoreographerSessionStep> result = new ArrayList<>();
      	  	final List<ChoreographerSessionStep> sessionSteps = sessionStepRepository.findAllBySession(session);
      	  	for (final ChoreographerSessionStep sessionStep : sessionSteps) {
      	  		if (ChoreographerSessionStepStatus.RUNNING == sessionStep.getStatus()) {
      	  			result.add(sessionStep);
      	  		}
      	  		changeSessionStepStatus(sessionStep.getId(), ChoreographerSessionStepStatus.ABORTED, message);
      	  	}
      	  
      	  	return result;
        } catch (final InvalidParameterException ex) {
        	throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
    
	//-------------------------------------------------------------------------------------------------
	public ChoreographerSession getSessionById(final long id) {
		logger.debug("getSessionById started...");
	
		try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(id);
			if (sessionOpt.isEmpty()) {
				throw new InvalidParameterException("Session with id " + id + " not exists");
			}
			return sessionOpt.get();
			
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
    
    //-------------------------------------------------------------------------------------------------
    public Page<ChoreographerSession> getSessions(final int page, final int size, final Direction direction, final String sortField, final Long planId, final ChoreographerSessionStatus status) {
    	logger.debug("getSessions started...");
    	
    	final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		final PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
		
		try {
			final ChoreographerSession schema = new ChoreographerSession();
			schema.setStatus(status);
			if (planId != null) {
				final Optional<ChoreographerPlan> planOpt = planRepository.findById(planId);
				if (planOpt.isEmpty()) {
					throw new InvalidParameterException("Plan with id " + planId + " not exists"); 
				}
				schema.setPlan(planOpt.get());
			}
			
			final ExampleMatcher matcher = ExampleMatcher.matching()
														 .withIgnorePaths(CommonConstants.COMMON_FIELD_NAME_ID)
														 .withStringMatcher(StringMatcher.EXACT)
														 .withIgnoreNullValues();
		
			return sessionRepository.findAll(Example.of(schema, matcher), pageRequest);	
			
		} catch (final InvalidParameterException ex) {
			throw ex;
			
		} catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
    
    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionListResponseDTO getSessionsResponse(final int page, final int size, final Direction direction, final String sortField, final Long planId, final String status) {
    	logger.debug("getSessionsResponse started...");
    	
    	final ChoreographerSessionStatus _status = Utilities.isEmpty(status) ? null : Utilities.convertStringToChoreographerSessionStatus(status);
    	
    	final Page<ChoreographerSession> data = getSessions(page, size, direction, sortField, planId, _status);
    	return DTOConverter.convertSessionListToSessionListResponseDTO(data, data.getTotalElements());
    }
    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSessionStep registerSessionStep(final long sessionId, final long stepId, final long executorId) {
    	logger.debug("registerSessionStep started...");
        
        try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				worklogAndThrow("Session step registration has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
			}
			final ChoreographerSession session = sessionOpt.get();
			final ChoreographerPlan plan = session.getPlan();
			
			final Optional<ChoreographerStep> stepOpt = stepRepository.findById(stepId);
			if (stepOpt.isEmpty()) {
				worklogAndThrow(plan.getName(), session.getId(), session.getExecutionNumber(), "Session step registration has been failed", new InvalidParameterException("Step with id " + stepId + " not exists"));
			}
			final ChoreographerStep step = stepOpt.get();
			
			final Optional<ChoreographerExecutor> executorOpt = executorRepository.findById(executorId);
			if (executorOpt.isEmpty()) {
				worklogAndThrow(plan.getName(), session.getId(), session.getExecutionNumber(), "Session step registration has been failed", new InvalidParameterException("Executor with id " + executorId + " not exists"));
			}
			final ChoreographerExecutor executor = executorOpt.get();
			
			final ChoreographerSessionStep sessionStep = sessionStepRepository.saveAndFlush(new ChoreographerSessionStep(session, step, executor, ChoreographerSessionStepStatus.WAITING,
																							session.getExecutionNumber(), "New session step has been registrated"));
			worklog(plan.getName(), step.getAction().getName(), step.getName(), session.getId(), session.getExecutionNumber(), "New session step has been registrated with id " + sessionStep.getId(), null);
			return sessionStep;
        	
        } catch (final InvalidParameterException ex) {
        	throw ex;
        } catch (final Exception ex) {
        	logger.debug(ex.getMessage(), ex);
        	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSessionStep changeSessionStepStatus(final long sessionId, final ChoreographerStep step, final ChoreographerSessionStepStatus status, final String message) {
    	logger.debug("changeSessionStepStatus started...");
    	Assert.notNull(step, "Step is null");
    	try {
    		final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
    		if (sessionOpt.isEmpty()) {
    			worklogAndThrow("Session step status change has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
    		}
    		
    		final Optional<ChoreographerSessionStep> sessionStepOpt = sessionStepRepository.findBySessionAndStepAndExecutionNumber(sessionOpt.get(), step, sessionOpt.get().getExecutionNumber());
			if (sessionStepOpt.isEmpty()) {
				worklogAndThrow("Session step status change has been failed", new InvalidParameterException("Session step with session id " + sessionId +
																											" and step id " + step.getId() +
																											" and execution number " + sessionOpt.get().getExecutionNumber() +
																											" not exists"));
			}
    		
			return changeSessionStepStatus(sessionStepOpt.get().getId(), status, message);
        } catch (final InvalidParameterException ex) {
        	throw ex;
        } catch (final Exception ex) {
        	logger.debug(ex.getMessage(), ex);
        	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }		
    }
    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSessionStep changeSessionStepStatus(final long sessionStepId, final ChoreographerSessionStepStatus status, final String message) {
    	logger.debug("changeSessionStepStatus started...");
        Assert.notNull(status, "ChoreographerSessionStepStatus is null");
        Assert.isTrue(!Utilities.isEmpty(message), "message is empty");
        
        try {
			final Optional<ChoreographerSessionStep> sessionStepOpt = sessionStepRepository.findById(sessionStepId);
			if (sessionStepOpt.isEmpty()) {
				worklogAndThrow("Session step status change has been failed", new InvalidParameterException("Session step with id " + sessionStepId + " not exists"));
			}
			ChoreographerSessionStep sessionStep = sessionStepOpt.get();
			
			if (status != sessionStep.getStatus()) {
				sessionStep.setStatus(status);
				sessionStep.setMessage(message.trim());
				
				sessionStep = sessionStepRepository.saveAndFlush(sessionStep);
				final ChoreographerSession session = sessionStep.getSession();
				final ChoreographerStep step = sessionStep.getStep();
				
				final String exception = status == ChoreographerSessionStepStatus.ABORTED ? message : null;
				worklog(session.getPlan().getName(), step.getAction().getName(), step.getName(), session.getId(), session.getExecutionNumber(),
						"Session step (id: " + sessionStepId + ") status has been changed to " + status, exception);
			}
			
			return sessionStep;
        } catch (final InvalidParameterException ex) {
        	throw ex;
        } catch (final Exception ex) {
        	logger.debug(ex.getMessage(), ex);
        	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
    
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSessionStep changeSessionStepExecutor(final long sessionStepId, final long executorId) {
    	logger.debug("changeSessionStepExecutor started...");
        
        try {
			final Optional<ChoreographerSessionStep> sessionStepOpt = sessionStepRepository.findById(sessionStepId);
			if (sessionStepOpt.isEmpty()) {
				worklogAndThrow("Session step executor change has been failed", new InvalidParameterException("Session step with id " + sessionStepId + " not exists"));
			}
			
			final Optional<ChoreographerExecutor> executorOpt = executorRepository.findById(executorId);
			if (executorOpt.isEmpty()) {
				worklogAndThrow("Session step executor change has been failed", new InvalidParameterException("Executor with id " + executorId + " not exists"));
			}
			
			ChoreographerSessionStep sessionStep = sessionStepOpt.get();
			sessionStep.setExecutor(executorOpt.get());
			sessionStep = sessionStepRepository.saveAndFlush(sessionStep);
			final ChoreographerSession session = sessionStep.getSession();
			final ChoreographerStep step = sessionStep.getStep();
			
			worklog(session.getPlan().getName(), step.getAction().getName(), step.getName(), step.getId(), session.getExecutionNumber(),
					"The executor of session step (id: " + sessionStepId + ") has been changed to executor with id " + executorId, null);
			
			return sessionStep;
        } catch (final InvalidParameterException ex) {
        	throw ex;
        } catch (final Exception ex) {
        	logger.debug(ex.getMessage(), ex);
        	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStep getSessionStepBySessionIdAndStepId(final long sessionId, final long stepId) {
		logger.debug("getSessionStepBySessionIdAndStepId started...");
	
		try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				throw new InvalidParameterException("Session with id " + sessionId + " not exists");
			}			
			final Optional<ChoreographerStep> stepOpt = stepRepository.findById(stepId);
			if (stepOpt.isEmpty()) {
				throw new InvalidParameterException("Step with id " + stepId + " not exists");
			}
			
			final Optional<ChoreographerSessionStep> sessionStepOpt = sessionStepRepository.findBySessionAndStepAndExecutionNumber(sessionOpt.get(), stepOpt.get(), sessionOpt.get().getExecutionNumber());
			if (sessionStepOpt.isEmpty()) {
				throw new InvalidParameterException("Session step with session id " + sessionId + " and step id " + stepId + " and execution number " + sessionOpt.get().getExecutionNumber() + " not exists");
			}

			return sessionStepOpt.get();
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerSessionStep> getSessionStepBySessionIdAndSteps(final long sessionId, final Set<ChoreographerStep> steps) {
		logger.debug("getSessionStepBySessionIdAndStepId started...");
		Assert.isTrue(steps != null && steps.size() > 0, "Step list is null or empty");
	
		try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				throw new InvalidParameterException("Session with id " + sessionId + " not exists");
			}
			
			return sessionStepRepository.findBySessionAndStepIn(sessionOpt.get(), steps);
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStep getSessionStepById(final long id) {
		logger.debug("getSessionStepById started...");
	
		try {
			final Optional<ChoreographerSessionStep> sessionStepOpt = sessionStepRepository.findById(id);
			if (sessionStepOpt.isEmpty()) {
				throw new InvalidParameterException("Session step with id " + id + " not exists");
			}

			return sessionStepOpt.get();
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerSessionStep> getAllSessionStepBySessionId(final long sessionId) {
		logger.debug("getAllSessionStepBySessionId started...");
	
		try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				throw new InvalidParameterException("Session with id " + sessionId + " not exists");
			}
			
			return sessionStepRepository.findAllBySession(sessionOpt.get());
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerSessionStep> getAllSessionStepBySessionIdAndActionId(final long sessionId, final long actionId) {
		logger.debug("getAllSessionStepBySessionIdAndActionId started...");
		
		try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				throw new InvalidParameterException("Session with id " + sessionId + " not exists");
			}
			
			final Optional<ChoreographerAction> actionOpt = actionRepository.findById(actionId);
			if (actionOpt.isEmpty()) {
				throw new InvalidParameterException("Action with id " + actionId + " not exists");
			}
			
			return sessionStepRepository.findAllBySessionAndStep_Action(sessionOpt.get(), actionOpt.get());
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<ChoreographerSessionStep> getSessionSteps(final int page, final int size, final Direction direction, final String sortField, final Long sessionId,
														  final ChoreographerSessionStepStatus status) {
		logger.debug("getSessionSteps started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		final PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
		
		try {
			final ChoreographerSessionStep schema = new ChoreographerSessionStep();
			schema.setStatus(status);
			if (sessionId != null) {
				final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
				if (sessionOpt.isEmpty()) {
					throw new InvalidParameterException("Session with id " + sessionId + " not exists"); 
				}
				schema.setSession(sessionOpt.get());
			}
			
			final ExampleMatcher matcher = ExampleMatcher.matching()
														 .withIgnorePaths(CommonConstants.COMMON_FIELD_NAME_ID)
														 .withStringMatcher(StringMatcher.EXACT)
														 .withIgnoreNullValues();
			
			return sessionStepRepository.findAll(Example.of(schema, matcher), pageRequest);
		
		} catch (final InvalidParameterException ex) {
			throw ex;
			
		} catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStepListResponseDTO getSessionStepsResponse(final int page, final int size, final Direction direction, final String sortField, final Long sessionId,
			  															   final String status) {
		logger.debug("getSessionStepsResponse started...");
		
		final ChoreographerSessionStepStatus _status = Utilities.isEmpty(status) ? null : Utilities.convertStringToChoreographerSessionStepStatus(status);
		final Page<ChoreographerSessionStep> data = getSessionSteps(page, size, direction, sortField, sessionId, _status);
		
		return DTOConverter.convertSessionStepListToSessionStepListResponseDTO(data, data.getTotalElements());
	}
  
	//-------------------------------------------------------------------------------------------------
	public Page<ChoreographerWorklog> getWorklogs(final int page, final int size, final Direction direction, final String sortField,
												  final Long sessionId, final String planName, final String actionName, final String stepName) {
		logger.debug("getWorklogs started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		final PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
		
		final ChoreographerWorklog schema = new ChoreographerWorklog();
		schema.setSessionId(sessionId);
		schema.setPlanName(Utilities.isEmpty(planName) ? null : planName.trim());
		schema.setActionName(Utilities.isEmpty(actionName) ? null : actionName.trim());
		schema.setStepName(Utilities.isEmpty(stepName) ? null : stepName.trim());
		
		final ExampleMatcher matcher = ExampleMatcher.matching()
													 .withIgnorePaths(CommonConstants.COMMON_FIELD_NAME_ID)
													 .withStringMatcher(StringMatcher.EXACT)
													 .withIgnoreNullValues();
		
		try {
			return worklogRepository.findAll(Example.of(schema, matcher), pageRequest);
		} catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerWorklogListResponseDTO getWorklogsResponse(final int page, final int size, final Direction direction, final String sortField,
			  													   final Long sessionId, final String planName, final String actionName, final String stepName) {
		logger.debug("getWorklogsResponse started...");
		
		final Page<ChoreographerWorklog> data = getWorklogs(page, size, direction, sortField, sessionId, planName, actionName, stepName);
		
		return DTOConverter.convertWorklogListToWorklogListResponseDTO(data, data.getTotalElements());
	}
	
   	//-------------------------------------------------------------------------------------------------  
  	public void worklog(final String planName, final Long sessionId, final Long executionNumber, final String message, final String exception) {
		worklog(planName, null, null, sessionId, executionNumber, message, exception);
	}
  
  	//-------------------------------------------------------------------------------------------------  
    @Transactional(propagation = Propagation.REQUIRES_NEW)
  	public void worklog(final String planName, final String actionName, final String stepName, final Long sessionId, final Long executionNumber, final String message, final String exception) {
  		logger.debug("worklog started...");
	  	try {
	  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, sessionId, executionNumber, message, exception));
		} catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    }
  	}
  	
  	//-------------------------------------------------------------------------------------------------  
    public void worklogAndThrow(final String message, final Exception ex) throws Exception {
		worklogAndThrow(null, null, null, null, null, message, ex);
	}
  	
  	//-------------------------------------------------------------------------------------------------  
    public void worklogAndThrow(final String planName, final Long sessionId, final Long executionNumber, final String message, final Exception ex) throws Exception {
		worklogAndThrow(planName, null, null, sessionId, executionNumber, message, ex);
	}
  	
  	//-------------------------------------------------------------------------------------------------
    public void worklogAndThrow(final String planName, final String actionName, final String stepName, final Long sessionId, final Long executionNumber, final String message, final Exception originalException) throws Exception {
  		logger.debug("worklogAndThrow started...");
  		worklogException(planName, actionName, stepName, sessionId, executionNumber, message, originalException);
  		
  		throw originalException;
  	}
    
  	//-------------------------------------------------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void worklogException(final String planName, final String actionName, final String stepName, final Long sessionId, final Long executionNumber, final String message, final Exception originalException) throws Exception {
  		logger.debug("worklogException started...");
  	  	try {
  	  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, sessionId, executionNumber, message, originalException.getClass().getSimpleName() + ": " + originalException.getMessage()));
  		} catch (final Exception ex) {
  	    	logger.debug(ex.getMessage(), ex); 
  	    }
  	}
}