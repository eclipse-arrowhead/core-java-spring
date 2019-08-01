package eu.arrowhead.core.gatekeeper.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.repository.RelayRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayResponseDTO;
import eu.arrowhead.common.dto.RelayResponseListDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class GatekeeperDBService {

	//=================================================================================================
	// members
	
	@Autowired
	private RelayRepository relayRepository;
	
	private final Logger logger = LogManager.getLogger(GatekeeperDBService.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public RelayResponseListDTO getRelaysResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getRelaysResponse started...");
		
		final Page<Relay> entries = getRelays(page, size, direction, sortField);
		return DTOConverter.convertRelayListToRelayResponseListDTO(entries);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public Page<Relay> getRelays (final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getRelays started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!Relay.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			
			return relayRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public RelayResponseDTO getRelayByIdResponse(final long id) {
		logger.debug("getRelayByIdResponse started...");
		
		final Relay entry = getRelayById(id);
		return DTOConverter.convertRelayToRelayResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public Relay getRelayById(final long id) {
		logger.debug("updateRelayById started...");
		
		try {
				
			final Optional<Relay> relayOpt = relayRepository.findById(id);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with id '" + id + "' not exists");
			}
			
			return relayOpt.get();			
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public RelayResponseDTO getRelayByAddressAndPortResponse(final String address, final int port) {
		logger.debug("getRelayByAddressAndPortResponse started...");
		
		final Relay entry = getRelayByAddressAndPort(address, port);
		return DTOConverter.convertRelayToRelayResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public Relay getRelayByAddressAndPort(String address, final int port) {
		logger.debug("getRelayByAddressAndPort started...");
		
		try {
			
			if (Utilities.isEmpty(address)) {
				throw new InvalidParameterException("Address is empty");
			}
			address = address.toLowerCase().trim();
				
			final Optional<Relay> relayOpt = relayRepository.findByAddressAndPort(address, port);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with the following address and port not exists: " + address + ", " + port);
			}
			
			return relayOpt.get();			
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
 	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public RelayResponseListDTO registerBulkRelaysResponse(final List<RelayRequestDTO> dtoList) {
		logger.debug("registerBulkRelaysResponse started...");
		
		final List<Relay> entries = registerBulkRelays(dtoList);
		return DTOConverter.convertRelayListToRelayResponseListDTO(new PageImpl<Relay>(entries));
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<Relay> registerBulkRelays(final List<RelayRequestDTO> dtoList) {
		logger.debug("registerBulkRelays started...");
		
		try {
			final List<Relay> relaysToSave = new ArrayList<>(dtoList.size());
			
			if (dtoList == null || dtoList.isEmpty()) {
				throw new InvalidParameterException("List of RelayRequestDTO is null or empty");
			}
			
			for (final RelayRequestDTO dto : dtoList) {
				
				if (dto == null) {
					throw new InvalidParameterException("List of RelayRequestDTO contains null element");
				}				
				validateRelayParameters(true, dto.getAddress(), dto.getPort(), dto.isSecure(), dto.isExclusive(), dto.getType());
				
				relaysToSave.add(new Relay(dto.getAddress(), dto.getPort(), dto.isSecure(), dto.isExclusive(), Utilities.convertStringToRelayType(dto.getType())));
			}
			
			final List<Relay> savedRelays = relayRepository.saveAll(relaysToSave);
			relayRepository.flush();
			return savedRelays;
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public RelayResponseDTO updateRelayByIdResponse(final long id, final String address, final int port, final boolean isSecure, final boolean isExclusive, final RelayType type) {
		logger.debug("updateRelayByIdResponse started...");
		
		final Relay entry = updateRelayById(id, address, port, isSecure, isExclusive, type);
		return DTOConverter.convertRelayToRelayResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public Relay updateRelayById(final long id, final String address, final int port, final boolean isSecure, final boolean isExclusive, final RelayType type) {
		logger.debug("updateRelayById started...");
		
		try {
				
			final Optional<Relay> relayOpt = relayRepository.findById(id);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with id '" + id + "' not exists");
			}
			final Relay relay = relayOpt.get();
			
			validateRelayParameters(false, address, port, isSecure, isExclusive, type.toString());
			if (!relay.getAddress().equalsIgnoreCase(address) || relay.getPort() != port) {
				checkUniqueConstarintOfRelayTable(address, port);
			}
			
			relay.setAddress(address);
			relay.setPort(port);
			relay.setSecure(isSecure);
			relay.setExclusive(isExclusive);
			relay.setType(type);
			
			return relayRepository.saveAndFlush(relay);			
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeRelayById(final long id) {
		logger.debug("removeRelayById started...");
		
		try {
			
			if (relayRepository.existsById(id)) {
				relayRepository.deleteById(id);
			}
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private void validateRelayParameters(final boolean withUniqueConstarintCheck, String address, final Integer port, Boolean secure, Boolean exclusive, final String type) {
		logger.debug("validateRelayParameters started...");
		
		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address is empty");
		}
		address = address.toLowerCase().trim();
		
		if (port == null) {
			throw new InvalidParameterException("Port is null");
		} else if (isPortOutOfValidRange(port)) {
			throw new InvalidParameterException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX);
		}
		
		secure = secure == null ? false : secure;
		
		exclusive = exclusive == null ? false : secure;
		
		if (Utilities.convertStringToRelayType(type) == null) {
			throw new InvalidParameterException(type + " type is invalid");
		}
		
		if (withUniqueConstarintCheck) {
			checkUniqueConstarintOfRelayTable(address, port);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkUniqueConstarintOfRelayTable(final String address, final int port) {
			if (relayRepository.existsByAddressAndPort(address, port)) {
				throw new InvalidParameterException("Relay with the following address and port already exists: " + address + ", " + port);
			}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private boolean isPortOutOfValidRange(final int port) {
		logger.debug("isPortOutOfValidRange started...");
		return port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX;
	}
}
