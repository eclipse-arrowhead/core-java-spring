package eu.arrowhead.core.serviceregistry.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.AccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class SRAccessControlFilter extends AccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final String[] allowedCoreSystemsForQuery = { CommonConstants.CORE_SYSTEM_ORCHESTRATOR_NAME, CommonConstants.CORE_SYSTEM_GATEKEEPER_NAME, CommonConstants.CORE_SYSTEM_CA_NAME,
																 CommonConstants.CORE_SYSTEM_CA_NAME_2 };
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		if (requestTarget.contains(CommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, requestTarget, cloudCN);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI)) {
			// A provider system can only register its own services!
			checkProviderAccessToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI)) {
			// A provider system can only unregister its own services!
			checkProviderAccessToDeregister(clientCN, queryParams, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAllowed(clientCN, requestTarget, cloudCN);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkIfLocalSystemOperator(final String clientCN, final String requestTarget, final String cloudCN) {
		final String sysopCN = CommonConstants.LOCAL_SYSTEM_OPERATOR_NAME + "." + cloudCN;
		if (!clientCN.equalsIgnoreCase(sysopCN)) {
			log.debug("Only the local system operator can use {}, access denied!", requestTarget);
		    throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkProviderAccessToRegister(final String clientCN, final String requestJSON, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		final ServiceRegistryRequestDTO requestBody = Utilities.fromJson(requestJSON, ServiceRegistryRequestDTO.class);
		final String providerName = requestBody.getProviderSystem() != null ? requestBody.getProviderSystem().getSystemName() : "";
		if (Utilities.isEmpty(providerName)) {
			log.debug("Provider name is not set in the body when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!providerName.equalsIgnoreCase(clientName) && !providerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name(" + providerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkProviderAccessToDeregister(final String clientCN, final Map<String,String[]> queryParams, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		
		final String providerName = queryParams.getOrDefault(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, new String[] { "" })[0];
		if (Utilities.isEmpty(providerName)) {
			log.debug("Provider name is not set in the query parameters when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!providerName.equalsIgnoreCase(clientName) && !providerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name(" + providerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkIfClientIsAllowed(final String clientCN, final String requestTarget, final String cloudCN) {
		for (final String coreSystemName : allowedCoreSystemsForQuery) {
			final String coreSystemCN = coreSystemName + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return;
			}
		}
		
		// client is not an allowed core system
		log.debug("Only dedicated core systems can use {}, access denied!", requestTarget);
	    throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}