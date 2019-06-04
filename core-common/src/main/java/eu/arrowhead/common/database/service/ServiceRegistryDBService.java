package eu.arrowhead.common.database.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;

@Service
public class ServiceRegistryDBService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ServiceRegistryRepository serviceRegistryRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Autowired
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Autowired
	private ServiceRegistryInterfaceConnectionRepository serviceRegistryInterfaceConnectionRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	public System getSystemById(final long systemId) {
		final Optional<System> systemOption = systemRepository.findById(systemId);
		
		if (!systemOption.isPresent()){
			throw new NoSuchElementException();		
		}
		
		return systemOption.get();			
	}

	//-------------------------------------------------------------------------------------------------
	
	public Page<System> getSystemEntries(final int page, final int size, final String direction, final String sortField) {
		final int validatedPage;
		final int validatedSize;
		final Direction validatedDirection;
		final String validatedSortField;
		
		if (page < 0) {
			validatedPage = 0;
		}else {
			validatedPage = page;
		}
		
		if (size < 0) {
			validatedSize = Integer.MAX_VALUE;
		}else {
			validatedSize = size;
		}
		
		if (direction == null) {
			validatedDirection = Direction.ASC;
		}else {
			try {
				validatedDirection = Direction.fromString(direction);
			}catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException("Direction field with reference '" + direction + "' is not available");
			}
			
		}
		
		if(sortField==null || "".equalsIgnoreCase(sortField)) {
			validatedSortField = CommonConstants.COMMON_FIELD_NAME_ID;
		}else {
			if (! System.SORTABLE_FIELDS_BY.contains(sortField)) {
				throw new IllegalArgumentException("Sortable field with reference '" + sortField + "' is not available");
			}else {
				validatedSortField = sortField;
			}
		}
		
		return systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Transactional (rollbackFor = Exception.class)
	public System createSystem(final SystemRequestDTO systemRequestDTO) {
		
		final Integer port = systemRequestDTO.getPort();
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX ) {
			throw new IllegalArgumentException("Port number  '" + port + "' is out of valid port range");
		}
			
		final System system = DTOConverter.convertSystemRequestDTOToSystem(systemRequestDTO);
		
		try {
			return systemRepository.saveAndFlush(system);
		} catch ( final Exception e) {
		  throw new BadPayloadException("Could not crate System, with given parameters", e);
		}
		
			
	}
		
	
	//-------------------------------------------------------------------------------------------------
	
	public Page<ServiceRegistry> getAllServiceReqistryEntries(final int page, final int size, final Direction direction, final String sortField) {
		final int page_ = page < 0 ? 0 : page;
		final int size_ = size < 0 ? Integer.MAX_VALUE : size; 		
		final Direction direction_ = direction == null ? Direction.ASC : direction;
		final String sortField_ = sortField == null ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (! ServiceRegistry.SORTABLE_FIELDS_BY.contains(sortField_)) {
			throw new IllegalArgumentException("Sortable field with reference '" + sortField_ + "' is not available");
		}
		return serviceRegistryRepository.findAll(PageRequest.of(page_, size_, direction_, sortField_));
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Transactional (rollbackFor = Exception.class)
	public void removeServiceRegistryEntryById(final long id) {
		serviceRegistryRepository.deleteById(id);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Transactional (rollbackFor = Exception.class)
	public void removeBulkOfServiceRegistryEntries(final Iterable<ServiceRegistry> entities) {
		serviceRegistryRepository.deleteInBatch(entities);
		serviceRegistryRepository.flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	public System updateSystem(final SystemRequestDTO systemRequestDTO, Long systemId) {
		
		if (systemRepository.existsById(systemId)) {
			throw new BadPayloadException("Could not update System, no System with id : "+ systemId);
		}
		
		final Integer port = systemRequestDTO.getPort();
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX ) {
			throw new IllegalArgumentException("Port number  '" + port + "' is out of valid port range");
		}
			
		final System system = DTOConverter.convertSystemRequestDTOToSystem(systemRequestDTO);
		system.setId(systemId);
			
		try {			
			return systemRepository.saveAndFlush(system);
		} catch ( final Exception e) {
		  throw new BadPayloadException("Could not crate System, with given parameters", e);
		}
	}

	public SystemResponseDTO updateSystem(long validatedSystemId, String validatedSystemName, String validatedAddress,
			int validatedPort, String validatedAuthenticationInfo) {
		
		
		try {			
			
			return DTOConverter.convertSystemToSystemResponseDTO(updateSystemById(validatedSystemId,
					validatedSystemName,
					validatedAddress,
					validatedPort,
					validatedAuthenticationInfo));
		} catch ( final Exception e) {
		  throw new BadPayloadException("Could not crate System, with given parameters", e);
		}
	}

	@Transactional (rollbackFor = Exception.class)
	public System updateSystemById(long validatedSystemId, String validatedSystemName, String validatedAddress,
			int validatedPort, String validatedAuthenticationInfo) {
		
		Optional<System> systemOptional = systemRepository.findById(validatedSystemId);
		if (!systemOptional.isPresent()) {
			throw new DataNotFoundException("No system with id : "+ validatedSystemId);
		}
		
		System system = systemOptional.get();
		checkIfUniqValidationNeeded(system, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
		system.setId(validatedSystemId );
		
		
		return null;
		
	}

	private boolean checkIfUniqValidationNeeded(System system, String validatedSystemName, String validatedAddress,
			int validatedPort, String validatedAuthenticationInfo) {
		
		boolean isUniqnessCheckNeeded = false;
		
		final String actualSystemName = system.getSystemName();
		final String actualAddress = system.getAddress();
		final int  actualPort = system.getPort();
		
		if ( actualSystemName != validatedSystemName) {
			isUniqnessCheckNeeded = true;
		}
		if ( actualAddress != validatedAddress) {
			isUniqnessCheckNeeded = true;
		}
		if ( actualPort != validatedPort) {
			isUniqnessCheckNeeded = true;
		}
		
		return isUniqnessCheckNeeded;	
		
	}
}
