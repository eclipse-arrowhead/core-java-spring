/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.authorization.service;

import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class AuthorizationDriver {
	
	//=================================================================================================
	// members
	
	private static final String EVENTHANDLER_AUTH_UPDATE_URI_KEY = CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	private static final Logger logger = LogManager.getLogger(AuthorizationDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String address;
	
	@Value(CoreCommonConstants.$SERVER_PORT)
	private int port;
	
	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String systemName;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Value(CoreCommonConstants.$AUTHORIZATION_IS_EVENTHANDLER_PRESENT_WD)
	private boolean eventhandlerIsPresent;
	
	private SystemRequestDTO systemRequestDTO;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public void publishAuthUpdate(final long updatedConsumerSystemId) {
		logger.debug("publishAuthUpdate started...");
		
		if (!eventhandlerIsPresent) {
			return;
		}
		
		if (systemRequestDTO == null) {
			initializeSystemRequestDTO();
		}
		
		Assert.isTrue(updatedConsumerSystemId > 0, "ConsumerSystemId could not be less than one.");
		
		final ZonedDateTime timeStamp = ZonedDateTime.now();

		final EventPublishRequestDTO eventPublishRequestDTO = new EventPublishRequestDTO();
		eventPublishRequestDTO.setEventType(CoreCommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE);
		eventPublishRequestDTO.setPayload(String.valueOf(updatedConsumerSystemId));
		eventPublishRequestDTO.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(timeStamp));	
		eventPublishRequestDTO.setSource(systemRequestDTO);
		
		final UriComponents eventHandlerAuthUpdateUri = getEventHandlerAuthUpdateUri();
		
		try {
			httpService.sendRequest(eventHandlerAuthUpdateUri , HttpMethod.POST, Void.class, eventPublishRequestDTO);
		} catch (final Exception ex) {
			logger.debug("Authorization Update publishing was unsuccessful : " + ex);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void initializeSystemRequestDTO() {
		logger.debug("initializeSystemRequestDTO started...");
		
		systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setSystemName(systemName);
		
		if (sslEnabled) {
			final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
			final String authInfo = Base64.getEncoder().encodeToString(publicKey.getEncoded());
			
			systemRequestDTO.setAuthenticationInfo(authInfo);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getEventHandlerAuthUpdateUri() {
		logger.debug("getGatekeeperGSDUri started...");
		
		if (arrowheadContext.containsKey(EVENTHANDLER_AUTH_UPDATE_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(EVENTHANDLER_AUTH_UPDATE_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Authorization can't find Event Handler's authorization_update URI.");
			}
		}
		
		throw new ArrowheadException("Authorization can't find Event Handler's authorization_update URI.");
	}
}