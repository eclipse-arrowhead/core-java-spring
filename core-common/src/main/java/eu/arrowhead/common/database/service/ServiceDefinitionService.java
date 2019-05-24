package eu.arrowhead.common.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;

@Service
public class ServiceDefinitionService {
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;

}
