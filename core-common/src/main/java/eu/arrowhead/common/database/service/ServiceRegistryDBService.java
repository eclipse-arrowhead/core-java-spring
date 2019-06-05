package eu.arrowhead.common.database.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
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
	
	private final Logger logger = LogManager.getLogger(ServiceRegistryDBService.class);
	
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
	public ServiceDefinition getServiceDefinitionById(final long id) {
		final Optional<ServiceDefinition> find = serviceDefinitionRepository.findById(id);
		if (find.isPresent()) {
			return find.get();
		} else {
			throw new DataNotFoundException("Service definition with id of '" + id + "' not exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionResponseDTO getServiceDefinitionByIdResponse(final long id) {
		final ServiceDefinition serviceDefinitionEntry = getServiceDefinitionById(id);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<ServiceDefinition> getAllServiceDefinitionEntries(final int page, final int size, final Direction direction, final String sortField) {
		final int page_ = page < 0 ? 0 : page;
		final int size_ = size < 0 ? Integer.MAX_VALUE : size; 		
		final Direction direction_ = direction == null ? Direction.ASC : direction;
		final String sortField_ = sortField == null ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (! ServiceDefinition.SORTABLE_FIELDS_BY.contains(sortField_)) {
			throw new IllegalArgumentException("Sortable field with reference '" + sortField_ + "' is not available");
		}
		return serviceDefinitionRepository.findAll(PageRequest.of(page_, size_, direction_, sortField_));
	}
		
	public ServiceDefinitionsListResponseDTO getAllServiceDefinitionEntriesResponse(final int page, final int size) {
		final List<ServiceDefinition> serviceDefinitionEntries = getAllServiceDefinitionEntries(page, size, null, null).getContent();
		return DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = Exception.class)
	public ServiceDefinition createServiceDefinition (final String serviceDefinition) {
		checkConstraintsOfServiceDefinitionTable(serviceDefinition);
		final ServiceDefinition serviceDefinitionEntry = new ServiceDefinition(serviceDefinition);
		return serviceDefinitionRepository.saveAndFlush(serviceDefinitionEntry);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = Exception.class)
	public ServiceDefinitionResponseDTO createServiceDefinitionResponse (final String serviceDefinition) {
		final ServiceDefinition serviceDefinitionEntry = createServiceDefinition(serviceDefinition);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}
		
	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = Exception.class)
	public ServiceDefinition updateServiceDefinitionById(final long id, final String serviceDefinition) {
		final Optional<ServiceDefinition> find = serviceDefinitionRepository.findById(id);
		ServiceDefinition serviceDefinitionEntry;
		if (find.isPresent()) {
			serviceDefinitionEntry = find.get();
			serviceDefinitionEntry.setServiceDefinition(serviceDefinition);
			return serviceDefinitionRepository.saveAndFlush(serviceDefinitionEntry);
		} else {
			throw new DataNotFoundException("Service definition with id of '" + id + "' not exists");
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = Exception.class)
	public ServiceDefinitionResponseDTO updateServiceDefinitionByIdResponse(final long id, final String serviceDefinition) {
		final ServiceDefinition serviceDefinitionEntry = updateServiceDefinitionById(id, serviceDefinition);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = Exception.class)
	public void removeServiceDefinitionById(final long id) {
		serviceDefinitionRepository.deleteById(id);
		serviceDefinitionRepository.flush();
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
		serviceRegistryRepository.flush();
	}
		
	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = Exception.class)
	public void removeBulkOfServiceRegistryEntries(final Iterable<ServiceRegistry> entities) {
		serviceRegistryRepository.deleteInBatch(entities);
		serviceRegistryRepository.flush();
	}
	
	//=================================================================================================
	// assistant methods
		
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfServiceDefinitionTable(final String serviceDefinition) {
		final Optional<ServiceDefinition> find = serviceDefinitionRepository.findByServiceDefinition(serviceDefinition);
		if (find.isPresent()) {
			throw new BadPayloadException(serviceDefinition + " definition already exists");
		}
	}
}
