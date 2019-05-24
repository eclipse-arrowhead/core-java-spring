package eu.arrowhead.common.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.repository.CloudRepository;

@Service
public class CloudService {
	
	@Autowired
	private CloudRepository cloudRepository;
	
}
