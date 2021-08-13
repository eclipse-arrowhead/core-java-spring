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


import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerWorklogRepository;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerSessionListResponseDTO;
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
	private ChoreographerSessionRepository sessionRepository;
	
	@Autowired
	private ChoreographerStepRepository stepRepository;
	
	@Autowired
	private ChoreographerSessionStepRepository sessionStepRepository;
	
	@Autowired
	private ChoreographerExecutorRepository executorRepository;
	
	@Autowired
	private ChoreographerWorklogRepository worklogRepository;
	
    private final Logger logger = LogManager.getLogger(ChoreographerSessionDBService.class);

	
	//=================================================================================================
	// methods
	
    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession initiateSession(final long planId, final String notifyUri) {
        logger.debug("initiateSession started...");
        
        try {
      	  final Optional<ChoreographerPlan> optional = planRepository.findById(planId);
      	  if (optional.isEmpty()) {
      		  worklogAndThrow("Initiating plan has been failed", new InvalidParameterException("Plan with id " + planId + " not exists"));
      	  }
      	  
      	  final String _notifyUri = UriComponentsBuilder.fromUriString(notifyUri).build().toUriString();
      	  final ChoreographerPlan plan = optional.get();
      	  final ChoreographerSession session = sessionRepository.saveAndFlush(new ChoreographerSession(plan, ChoreographerSessionStatus.INITIATED, _notifyUri));
      	  
      	  worklog(plan.getName(), session.getId(), "New session has been initiated", null);
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
    public ChoreographerSession changeSessionStatus(final long sessionId, final ChoreographerSessionStatus status) {
        logger.debug("changeSessionStatus started...");
        Assert.notNull(status, "ChoreographerSessionStatus is null");

        try {
      	  final Optional<ChoreographerSession> optional = sessionRepository.findById(sessionId);
      	  if (optional.isEmpty()) {
      		  worklogAndThrow("Session status change has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
      	  }
      	  
      	  ChoreographerSession session = optional.get();
      	  session.setStatus(status);
      	  session = sessionRepository.saveAndFlush(session);
      	  
      	  worklog(session.getPlan().getName(), session.getId(), "Session status has been changed to " + status, null);
      	  return session;
      	  
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
		
		final ChoreographerSession schema = new ChoreographerSession();
		schema.setStatus(status);
		if (planId != null) {
			final Optional<ChoreographerPlan> optional = planRepository.findById(planId);
			if (optional.isEmpty()) {
				throw new InvalidParameterException("Plan with id " + planId + " not exists"); 
			}
			schema.setPlan(optional.get());
		}
		
		final ExampleMatcher matcher = ExampleMatcher.matching()
													 .withIgnorePaths(CommonConstants.COMMON_FIELD_NAME_ID)
													 .withStringMatcher(StringMatcher.EXACT)
													 .withIgnoreNullValues();
		
		try {
			return sessionRepository.findAll(Example.of(schema, matcher), pageRequest);			
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
    public ChoreographerSessionStep registerSessionStep(final long sessionId, final long stepId, final long executorId, final String message) {
    	logger.debug("registerSessionStep started...");
        Assert.isTrue(!Utilities.isEmpty(message), "message is empty");
        
        try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				worklogAndThrow("Session step registration has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
			}
			final ChoreographerSession session = sessionOpt.get();
			final ChoreographerPlan plan = session.getPlan();
			
			final Optional<ChoreographerStep> stepOpt = stepRepository.findById(stepId);
			if (stepOpt.isEmpty()) {
				worklogAndThrow(plan.getName(), session.getId(), "Session step registration has been failed", new InvalidParameterException("Step with id " + stepId + " not exists"));
			}
			final ChoreographerStep step = stepOpt.get();
			
			final Optional<ChoreographerExecutor> executorOpt = executorRepository.findById(executorId);
			if (executorOpt.isEmpty()) {
				worklogAndThrow(plan.getName(), session.getId(), "Session step registration has been failed", new InvalidParameterException("Executor with id " + executorId + " not exists"));
			}
			final ChoreographerExecutor executor = executorOpt.get();
			
			final ChoreographerSessionStep sessionStep = sessionStepRepository.saveAndFlush(new ChoreographerSessionStep(session, step, executor, ChoreographerSessionStepStatus.WAITING,
																							message.trim()));
			worklog(plan.getName(), step.getAction().getName(), step.getName(), session.getId(), "New session step has been registrated with id " + sessionStep.getId(), null);
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
    public ChoreographerSessionStep changeSessionStepStatus(final long sessionStepId, final ChoreographerSessionStepStatus status, final String message) {
    	logger.debug("changeSessionStepStatus started...");
        Assert.notNull(status, "ChoreographerSessionStepStatus is null");
        Assert.isTrue(!Utilities.isEmpty(message), "message is empty");
        
        try {
			final Optional<ChoreographerSessionStep> optional = sessionStepRepository.findById(sessionStepId);
			if (optional.isEmpty()) {
				worklogAndThrow("Session step status change has been failed", new InvalidParameterException("Session step with id " + sessionStepId + " not exists"));
			}
			ChoreographerSessionStep sessionStep = optional.get();
			sessionStep.setStatus(status);
			sessionStep.setMessage(message.trim());
			
			sessionStep = sessionStepRepository.saveAndFlush(sessionStep);
			worklog(sessionStep.getSession().getPlan().getName(), sessionStep.getStep().getAction().getName(), sessionStep.getStep().getName(), sessionStep.getStep().getId(),
					"Session step (id: " + sessionStepId + ") status has been changed to " + status, null);
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
			
			final Optional<ChoreographerSessionStep> sessionStepOpt = sessionStepRepository.findBySessionAndStep(sessionOpt.get(), stepOpt.get());
			if (sessionStepOpt.isEmpty()) {
				throw new InvalidParameterException("Session step with session id " + sessionId + " and step id " + stepId + " not exists");
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
	public ChoreographerSessionStep getSessionStepById(final long id) {
		logger.debug("getSessionStepById started...");
	
		try {
			final Optional<ChoreographerSessionStep> optional = sessionStepRepository.findById(id);
			if (optional.isEmpty()) {
				throw new InvalidParameterException("Session step with id " + id + " not exists");
			}
			return optional.get();
			
	    } catch (final InvalidParameterException ex) {
	    	throw ex;
	      
	    } catch (final Exception ex) {
	    	logger.debug(ex.getMessage(), ex);
	    	throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerSessionStep> getAllSessionStepBySessionId(final long sessionId) {
		logger.debug("getSessionStepById started...");
	
		try {
			final Optional<ChoreographerSession> optional = sessionRepository.findById(sessionId);
			if (optional.isEmpty()) {
				throw new InvalidParameterException("Session with id " + sessionId + " not exists");
			}
			return sessionStepRepository.findAllBySession(optional.get());
			
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
		
		final ChoreographerSessionStep schema = new ChoreographerSessionStep();
		schema.setStatus(status);
		if (sessionId != null) {
			final Optional<ChoreographerSession> optional = sessionRepository.findById(sessionId);
			if (optional.isEmpty()) {
				throw new InvalidParameterException("Session with id " + sessionId + " not exists"); 
			}
			schema.setSession(optional.get());
		}
		
		final ExampleMatcher matcher = ExampleMatcher.matching()
													 .withIgnorePaths(CommonConstants.COMMON_FIELD_NAME_ID)
													 .withStringMatcher(StringMatcher.EXACT)
													 .withIgnoreNullValues();
		try {
			return sessionStepRepository.findAll(Example.of(schema, matcher), pageRequest);
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
	
  	//=================================================================================================
	// assistant methods
  
  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final Long sessionId, final String message, final String exception) {
		worklog(planName, null, null, sessionId, message, exception);
	}
  
  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final String actionName, final String stepName, final Long sessionId, final String message, final String exception) {
  		logger.debug("worklog started...");
  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, sessionId, message, exception));
  	}
  	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklogAndThrow(final String message, final Exception ex) throws Exception {
		worklogAndThrow(null, null, null, null, message, ex);
	}
  	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklogAndThrow(final String planName, final Long sessionId, final String message, final Exception ex) throws Exception {
		worklogAndThrow(planName, null, null, sessionId, message, ex);
	}
  	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklogAndThrow(final String planName, final String actionName, final String stepName, final Long sessionId, final String message, final Exception ex) throws Exception {
  		logger.debug("worklogAndThrow started...");
  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, sessionId, message, ex.getClass().getSimpleName() + ": " + ex.getMessage()));
  		throw ex;
  	}
}