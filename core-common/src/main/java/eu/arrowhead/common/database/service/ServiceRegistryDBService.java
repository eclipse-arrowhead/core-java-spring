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
import eu.arrowhead.common.exception.BadPayloadException;

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
	
	public Page<ServiceRegistry> getAllServiceReqistryEntries(int page, int size, Direction direction, final String sortField) {
		if (page < 0) {
			page = 0;
		}
		if (size < 0) {
			size = Integer.MAX_VALUE;
		}
		if (direction == null) {
			direction = Direction.ASC;
		}
		if (! ServiceRegistry.SORTABLE_FIELDS_BY.contains(sortField)) {
			throw new IllegalArgumentException("Sortable field with reference '" + sortField + "' is not available");
		}
		return serviceRegistryRepository.findAll(PageRequest.of(page, size, direction, sortField));
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
	
	@Transactional (rollbackFor = Exception.class)
	public void removeServiceRegistryEntryById(final long id) {
		serviceRegistryRepository.deleteById(id);
	}
	
}
