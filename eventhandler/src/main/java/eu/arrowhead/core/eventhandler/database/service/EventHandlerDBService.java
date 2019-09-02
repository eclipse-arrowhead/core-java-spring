package eu.arrowhead.core.eventhandler.database.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.repository.EventFilterRepository;
import eu.arrowhead.common.database.repository.EventTypeRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.EventFilterListResponseDTO;
import eu.arrowhead.common.dto.EventFilterResponseDTO;

@Service
public class EventHandlerDBService {
	//=================================================================================================
	// members
	
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = "The following sortable field  is not available : ";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";
	private static final String NOT_VALID_ERROR_MESSAGE = " is not valid.";
	private static final String NOT_FOREIGN_ERROR_MESSAGE = " is not foreign";
	
	private static final Logger logger = LogManager.getLogger(EventHandlerDBService.class);
	
	@Autowired
	private EventFilterRepository eventFilterRepository;
	
	@Autowired
	private EventTypeRepository eventTypeRepository;
	
	@Autowired
	private SystemRepository systemRepository;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public EventFilterListResponseDTO getEventHandlersResponse(final int validatedPage, final int validatedSize,
			final Direction validatedDirecion, final String sortField) {
		logger.debug("getEventHandlersResponse started ...");
		
		// TODO implement additional method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public EventFilterResponseDTO getEventFilterByIdResponse(final long id) {
		logger.debug("getEventFilterByIdResponse started ...");
		
		// TODO implement additional method logic
		return null;
	}
}
