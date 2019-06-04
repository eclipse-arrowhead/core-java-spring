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
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.database.entity.System;

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
	public void removeBulkOfServiceRegistryEntries(Iterable<ServiceRegistry> entities) {
		serviceRegistryRepository.deleteInBatch(entities);
		serviceRegistryRepository.flush();
	}
}
