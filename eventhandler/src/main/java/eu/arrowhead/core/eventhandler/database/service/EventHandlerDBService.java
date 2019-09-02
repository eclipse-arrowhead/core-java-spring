package eu.arrowhead.core.eventhandler.database.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.entity.EventFilter;
import eu.arrowhead.common.database.repository.EventFilterRepository;
import eu.arrowhead.common.database.repository.EventTypeRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.EventFilterListResponseDTO;
import eu.arrowhead.common.dto.EventFilterRequestDTO;
import eu.arrowhead.common.dto.EventFilterResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;

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
	public EventFilterListResponseDTO getEventHandlersRequest(final int validatedPage, final int validatedSize,
			final Direction validatedDirecion, final String sortField) {
		logger.debug("getEventHandlersRequest started ...");
		
		// TODO implement additional method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public EventFilterResponseDTO getEventFilterByIdRequest(final long id) {
		logger.debug("getEventFilterByIdRequest started ...");
		
		// TODO implement additional method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public void deleteEventFilterRequest(final long id) {
		logger.debug("deleteEventFilterRequest started ...");
		
		// TODO implement additional method logic
		return;
	}

	//-------------------------------------------------------------------------------------------------
	public EventFilterResponseDTO updateEventFilterRequest(final long id, final EventFilterRequestDTO eventFilterRequestDTO) {
		logger.debug("updateEventFilterRequest started ...");
		
		// TODO implement additional method logic
		return null;
	}	

	//-------------------------------------------------------------------------------------------------
	public EventFilter subscription(final EventFilterRequestDTO eventFilterRequestDTO) {
		logger.debug("subscription started ...");
		
		final EventFilter validEventFilter = validateEventFilterRequestDTO(eventFilterRequestDTO);		
		
		return eventFilterRepository.saveAndFlush(validEventFilter);

	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------
	private EventFilter validateEventFilterRequestDTO(final EventFilterRequestDTO eventFilterRequestDTO) {
		logger.debug("validateEventFilterRequestDTO started ...");
		
		final EventFilter eventFilter = new EventFilter();
		
		final Set<String> systemResponseDTOJsonSet = new HashSet();
		for (final SystemRequestDTO systemRequestDTO : eventFilterRequestDTO.getSources()) {
			//TODO checkIf system is valid and is in db, then convert to system response then convert to Json then add to SET
		}
		// TODO implement additional method logic
		
		return null;
	}
}
