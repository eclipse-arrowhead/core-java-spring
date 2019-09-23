package eu.arrowhead.core.eventhandler.service;

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class EventHandlerDriver {	

	//=================================================================================================
	// members
	
	private static final String AUTH_SUBSCRIPTION_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	private static final Logger logger = LogManager.getLogger(EventHandlerDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public Set<SystemResponseDTO> getAuthorizedPublishers(final SystemRequestDTO subscriberSystem) {
		logger.debug("getAuthorizedPublishers started...");
		
		Assert.notNull(subscriberSystem, "subscriberSystem is null.");
		
		final UriComponents checkUri = getAuthSubscriptionCheckUri();
		final AuthorizationSubscriptionCheckRequestDTO payload = new AuthorizationSubscriptionCheckRequestDTO(subscriberSystem, null);
		final ResponseEntity<AuthorizationSubscriptionCheckResponseDTO> response = httpService.sendRequest(checkUri, HttpMethod.POST, AuthorizationSubscriptionCheckResponseDTO.class, payload);		
		
		return response.getBody().getPublishers();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void publishEvent(final EventPublishRequestDTO request, final Set<Subscription> involvedSubscriptions) {
		logger.debug("publishEvent started...");
		
		try {
			final PublishRequestExecutor publishRequestExecutor = new PublishRequestExecutor( request, involvedSubscriptions, httpService);
			
			publishRequestExecutor.execute();
		} catch (final Exception ex) {
			
			logger.debug("publishEvent finished with exception : " + ex);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthSubscriptionCheckUri() {
		logger.debug("getAuthSubscriptionCheckUri started...");
		
		if (arrowheadContext.containsKey(AUTH_SUBSCRIPTION_CHECK_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_SUBSCRIPTION_CHECK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
			}
		}
		
		throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
	}

}
