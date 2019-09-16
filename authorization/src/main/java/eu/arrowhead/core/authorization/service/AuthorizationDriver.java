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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;

@Component
public class AuthorizationDriver {
	//=================================================================================================
	// members
	private static final String EVENT_HANDLER_AUTH_UPDATE_URI_KEY = CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
	
	private static final Logger logger = LogManager.getLogger(AuthorizationDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_ADDRESS)
	private String address;
	
	@Value(CommonConstants.$SERVER_PORT)
	private int port;
	
	@Value(CommonConstants.$CORE_SYSTEM_NAME)
	private String systemName;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	public void publishAuthUpdate(final long updatedConsumerSystemId) {
		logger.debug("publishAuthUpdate started...");
		
		Assert.isTrue(updatedConsumerSystemId > 0, "ConsumerSystemId could not be less then one.");
		
		final ZonedDateTime timeStamp = ZonedDateTime.now();

		final EventPublishRequestDTO eventPublishRequestDTO = new EventPublishRequestDTO();
		eventPublishRequestDTO.setEventType( CommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE );
		eventPublishRequestDTO.setPayload( Long.toString( updatedConsumerSystemId ) );
		eventPublishRequestDTO.setTimeStamp( Utilities.convertZonedDateTimeToUTCString( timeStamp ) );
		
		
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setAddress( address );
		systemRequestDTO.setPort( port );
		systemRequestDTO.setSystemName( systemName );
		
		if ( sslEnabled ) {
			
			final PublicKey publicKey = (PublicKey) arrowheadContext.get( CommonConstants.SERVER_PUBLIC_KEY );
			final String authInfo = Base64.getEncoder().encodeToString(publicKey.getEncoded());
			
			systemRequestDTO.setAuthenticationInfo( authInfo );
		}
		
		eventPublishRequestDTO.setSource( systemRequestDTO );
		
		final UriComponents eventHandlerAuthUpdateUri = getEventHandlerAuthUpdateUri();
		final ResponseEntity response;
		
		try {
			
			response = httpService.sendRequest( eventHandlerAuthUpdateUri , HttpMethod.POST, ResponseEntity.class, eventPublishRequestDTO);
		
			if ( response == null ) {
				
				throw new TimeoutException(" PublishAuthUpdateEventRequest timeout");
			}
			
			if ( !response.getStatusCode().is2xxSuccessful() ) {
				
				logger.debug(" Authorization Update publishing was unsuccessful : " + response.getStatusCode());
			}
			
		} catch (Exception ex) {
						
			logger.debug(" Authorization Update publishing was unsuccessful : " + ex);
		}

	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getEventHandlerAuthUpdateUri() {
		logger.debug("getGatekeeperGSDUri started...");
		
		if (arrowheadContext.containsKey(EVENT_HANDLER_AUTH_UPDATE_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(EVENT_HANDLER_AUTH_UPDATE_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Authorization can't find eventhandler authorization_update URI.");
			}
		}
		
		throw new ArrowheadException("Authorization can't find eventhandler authorization_update URI.");
	}

}
