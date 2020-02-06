package eu.arrowhead.core.datamanager.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;

@Component
public class DataManagerDriver {	

	//=================================================================================================
	// members
	private static final Logger logger = LogManager.getLogger(DataManagerDriver.class);
	
	
	//=================================================================================================
	// methods

	
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	
	
}
