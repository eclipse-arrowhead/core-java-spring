package eu.arrowhead.core.choreographer.database.service;


import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerWorklogRepository;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
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
	private ChoreographerWorklogRepository worklogRepository;
	
    private final Logger logger = LogManager.getLogger(ChoreographerSessionDBService.class);

	
	//=================================================================================================
	// methods
	
//  //-------------------------------------------------------------------------------------------------
//  @Transactional(rollbackFor = ArrowheadException.class)
//  public ChoreographerSessionStep registerRunningStep(final long stepId, final long sessionId, final ChoreographerStatusType status, final String message) {
//      try {
//          if (status == null) {
//              throw new InvalidParameterException("Status is null or blank.");
//          }
//
//          if (Utilities.isEmpty(message)) {
//              throw new InvalidParameterException("Message is null or blank.");
//          }
//
//          final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findById(stepId);
//          final Optional<ChoreographerSession> sessionOptional = choreographerSessionRepository.findById(sessionId);
//
//          if (stepOptional.isPresent() && sessionOptional.isPresent()) {
//              return choreographerRunningStepRepository.saveAndFlush(new ChoreographerSessionStep(status, message, stepOptional.get(), sessionOptional.get()));
//          } else {
//              throw new InvalidParameterException("Step or Session with given id(s) not found!");
//          }
//      } catch (final InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }

//  //-------------------------------------------------------------------------------------------------
//  @Transactional(rollbackFor = ArrowheadException.class)
//  public ChoreographerSessionStep setRunningStepStatus(final long runningStepId, final ChoreographerStatusType status, final String message) {
//      try {
//          if (status == null) {
//              throw new InvalidParameterException("Status is null or blank.");
//          }
//
//          if (Utilities.isEmpty(message)) {
//              throw new InvalidParameterException("Message is null or blank.");
//          }
//
//          final Optional<ChoreographerSessionStep> runningStepOptional = choreographerRunningStepRepository.findById(runningStepId);
//
//          if (runningStepOptional.isPresent()) {
//              ChoreographerSessionStep runningStepToChange = runningStepOptional.get();
//              runningStepToChange.setStatus(status);
//              runningStepToChange.setMessage(message);
//              return choreographerRunningStepRepository.saveAndFlush(runningStepToChange);
//          } else {
//              throw new InvalidParameterException("Running step with given ID doesn't exist.");
//          }
//      } catch (final InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }

  //-------------------------------------------------------------------------------------------------
  @Transactional(rollbackFor = ArrowheadException.class)
  public ChoreographerSession initiateSession(final long planId, final String notifyUri) {
      logger.debug("initiateSession started...");
      
      try {
    	  final Optional<ChoreographerPlan> optional = planRepository.findById(planId);
    	  if (optional.isEmpty()) {
    		  throw new InvalidParameterException("Can't initiate session because the plan with the given ID doesn't exist.");
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

//  //-------------------------------------------------------------------------------------------------
//  @Transactional(rollbackFor = ArrowheadException.class)
//  public ChoreographerSession finalizeSession(final long sessionId) {
//      logger.debug("finalizeSession started...");
//
//      try {
//          Optional<ChoreographerSession> sessionOptional = choreographerSessionRepository.findById(sessionId);
//          if (sessionOptional.isPresent()) {
//              ChoreographerSession session = sessionOptional.get();
//              session.setStatus(ChoreographerStatusType.DONE);
//              createWorklog("Session with ID of " + sessionId + " finished successfully.", "");
//              return choreographerSessionRepository.saveAndFlush(session);
//          } else {
//              throw new InvalidParameterException("Session with given ID doesn't exist.");
//          }
//      } catch (InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }

//  //-------------------------------------------------------------------------------------------------
//  @Transactional(rollbackFor = ArrowheadException.class)
//  public ChoreographerSession setSessionStatus(final long sessionId, final ChoreographerStatusType state) {
//      logger.debug("changeSessionState started...");
//
//      if (state == null) {
//          throw new InvalidParameterException("State is null or blank.");
//      }
//
//      try {
//          Optional<ChoreographerSession> sessionOptional = choreographerSessionRepository.findById(sessionId);
//          if (sessionOptional.isPresent()) {
//              ChoreographerSession session = sessionOptional.get();
//              session.setStatus(state);
//              createWorklog("New status of session with ID of " + sessionId + ": " + state, "");
//              return choreographerSessionRepository.saveAndFlush(session);
//          } else {
//              throw new InvalidParameterException("Session with given ID doesn't exist.");
//          }
//      } catch (InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }

//  //-------------------------------------------------------------------------------------------------
//  @Transactional(rollbackFor = ArrowheadException.class)
//  public ChoreographerWorklog createWorklog(final String message, final String exception) {
//      logger.debug("createWorklog started...");
//
//      try {
//          if (Utilities.isEmpty(message)) {
//              throw new InvalidParameterException("Message is null or blank.");
//          }
//
//          return choreographerWorklogRepository.saveAndFlush(new ChoreographerWorklog(message, exception));
//      } catch (InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }

//  //-------------------------------------------------------------------------------------------------
//  public ChoreographerSessionStep getRunningStepBySessionIdAndStepId(final long sessionId, final long stepId) {
//      logger.debug("getRunningStepBySessionIdAndStepId started...");
//
//      try {
//          final Optional<ChoreographerSessionStep> runningStepOpt = choreographerRunningStepRepository.findByStepIdAndSessionId(stepId, sessionId);
//          if (runningStepOpt.isPresent()) {
//              return runningStepOpt.get();
//          } else {
//              throw new InvalidParameterException("Running step with session id of '" + sessionId + "' and step id of '" + stepId + "' doesn't exist!");
//          }
//      } catch (final InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }

//  //-------------------------------------------------------------------------------------------------
//  public ChoreographerSessionStep getRunningStepById(final long id) {
//      logger.debug("getRunningStepById started...");
//
//      try {
//          final Optional<ChoreographerSessionStep> runningStepOpt = choreographerRunningStepRepository.findById(id);
//          if (runningStepOpt.isPresent()) {
//              return runningStepOpt.get();
//          } else {
//              throw new InvalidParameterException("Running step with id of '" + id + "' doesn't exist!");
//          }
//      } catch (final InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }
	
//  //-------------------------------------------------------------------------------------------------
//  public List<ChoreographerSessionStep> getAllRunningStepsBySessionId(final long sessionId) {
//      logger.debug("getAllRunningStepsBySessionId started...");
//
//      try {
//          final List<ChoreographerSessionStep> runningSteps = choreographerRunningStepRepository.findAllBySessionId(sessionId);
//          if (!runningSteps.isEmpty()) {
//              return  runningSteps;
//          } else {
//              throw new InvalidParameterException("There are no running steps associated with id of '" + sessionId + "'.");
//          }
//      } catch (final InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
//  }
  
  	//=================================================================================================
	// assistant methods
	
  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final String message, final String exception) {
		worklog(planName, null, null, message, exception);
	}
  
  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final String actionName, final String message, final String exception) {
		worklog(planName, actionName, null, message, exception);
	}

  	//-------------------------------------------------------------------------------------------------  
  	private void worklog(final String planName, final String actionName, final String stepName, final String message, final String exception) {
  		logger.debug("worklog started...");
  		worklogRepository.saveAndFlush(new ChoreographerWorklog(planName, actionName, stepName, message, exception));
  	}
}