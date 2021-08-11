package eu.arrowhead.core.choreographer.database.service;


import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

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
      	  
      	  worklog(plan.getName(), "New session has been initiated with id " + session.getId(), null);
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
      	  
      	  worklog(session.getPlan().getName(), "Session (id: " + session.getId() + ") status has been changed to " + status, null);
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
    public ChoreographerSessionStep registerSessionStep(final long sessionId, final long stepId, final long executorId, final ChoreographerSessionStepStatus status, final String message) {
    	logger.debug("registerSessionStep started...");
        Assert.notNull(status, "ChoreographerSessionStepStatus is null");
        Assert.isTrue(!Utilities.isEmpty(message), "message is empty");
        
        try {
			final Optional<ChoreographerSession> sessionOpt = sessionRepository.findById(sessionId);
			if (sessionOpt.isEmpty()) {
				worklogAndThrow("Session step registration has been failed", new InvalidParameterException("Session with id " + sessionId + " not exists"));
			}
			final ChoreographerSession session = sessionOpt.get();
			
			final Optional<ChoreographerStep> stepOpt = stepRepository.findById(stepId);
			if (stepOpt.isEmpty()) {
				worklogAndThrow(session.getPlan().getName(), "Session step registration has been failed", new InvalidParameterException("Step with id " + stepId + " not exists"));
			}
			final ChoreographerStep step = stepOpt.get();
			
			final Optional<ChoreographerExecutor> executorOpt = executorRepository.findById(executorId);
			if (executorOpt.isEmpty()) {
				worklogAndThrow(session.getPlan().getName(), "Session step registration has been failed", new InvalidParameterException("Executor with id " + executorId + " not exists"));
			}
			final ChoreographerExecutor executor = executorOpt.get();
			
			final ChoreographerSessionStep sessionStep = sessionStepRepository.saveAndFlush(new ChoreographerSessionStep(session, step, executor, status, message.trim()))	;
			worklog(session.getPlan().getName(), step.getAction().getName(), step.getName(), "New session step has been registrated with id " + sessionStep.getId(), null);
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
			worklog(sessionStep.getSession().getPlan().getName(), sessionStep.getStep().getAction().getName(), sessionStep.getStep().getName(),
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
  
  	//=================================================================================================
	// assistant methods
  
  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final String message, final String exception) {
		worklog(planName, null, null, message, exception);
	}
  
  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final String actionName, final String stepName, final String message, final String exception) {
  		logger.debug("worklog started...");
  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, message, exception));
  	}
  	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklogAndThrow(final String message, final Exception ex) throws Exception {
		worklogAndThrow(null, null, null, message, ex);
	}
  	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklogAndThrow(final String planName, final String message, final Exception ex) throws Exception {
		worklogAndThrow(planName, null, null, message, ex);
	}
  	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklogAndThrow(final String planName, final String actionName, final String stepName, final String message, final Exception ex) {
  		logger.debug("worklogAndThrow started...");
  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, message, ex.getClass().getSimpleName() + ": " + ex.getMessage()));
  	}
}