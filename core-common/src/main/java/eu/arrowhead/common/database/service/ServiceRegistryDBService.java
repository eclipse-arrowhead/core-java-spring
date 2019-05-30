package eu.arrowhead.common.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;

@Service
public class ServiceRegistryDBService {
	
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
	
	@Transactional (rollbackFor = Exception.class)
	public void removeServiceRegistryEntryById(final long id) {
		serviceRegistryRepository.deleteById(id);
	}
	
}
