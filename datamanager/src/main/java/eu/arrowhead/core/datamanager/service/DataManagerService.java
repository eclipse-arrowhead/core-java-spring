package eu.arrowhead.core.datamanager.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;

@Service
public class DataManagerService {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DataManagerService.class);
	
	@Autowired
	private DataManagerDriver dataManagerDriver;
	
	@Autowired
	private DataManagerDBService dataManagerDBService;
	
	//=================================================================================================
	// methods
}
