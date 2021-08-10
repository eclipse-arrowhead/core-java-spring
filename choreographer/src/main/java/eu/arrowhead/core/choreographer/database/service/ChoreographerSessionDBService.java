package eu.arrowhead.core.choreographer.database.service;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.exception.ArrowheadException;

@Service
public class ChoreographerSessionDBService {

	//=================================================================================================
	// members
	
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
      
      //TODO: delete this
      final ChoreographerSession dummySession = new ChoreographerSession(null, ChoreographerSessionStatus.INITIATED, notifyUri);
      dummySession.setId(1);
      return dummySession;

      //TODO: implement this
//      try {
//          final Optional<ChoreographerPlan> planOptional = choreographerPlanRepository.findById(planId);
//          if (planOptional.isPresent()) {
//              ChoreographerSession sessionEntry = choreographerSessionRepository.saveAndFlush(new ChoreographerSession(planOptional.get(), ChoreographerStatusType.INITIATED));
//              String worklogMessage = "Plan with ID of " + planId + " started running with session ID of " + sessionEntry.getId() + ".";
//              createWorklog(worklogMessage, "");
//              return sessionEntry;
//          } else {
//              throw new InvalidParameterException("Can't initiate session because the plan with the given ID doesn't exist.");
//          }
//      } catch (final InvalidParameterException ex) {
//          throw ex;
//      } catch (final Exception ex) {
//          logger.debug(ex.getMessage(), ex);
//          throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//      }
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
}