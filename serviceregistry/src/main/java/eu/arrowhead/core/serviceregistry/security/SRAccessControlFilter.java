package eu.arrowhead.core.serviceregistry.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.AccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class SRAccessControlFilter extends AccessControlFilter {
	
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON);
		
		final String cloudCN = getServerCloudCN();
		if (requestTarget.contains(CommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, requestTarget, cloudCN);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI)) {
			// A provider system can only register its own services!
			checkProviderAccessToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI)) {
			// A provider system can only unregister its own services!

		}
	}

	private void checkIfLocalSystemOperator(final String clientCN, final String requestTarget, final String cloudCN) {
		final String sysopCN = CommonConstants.LOCAL_SYSTEM_OPERATOR_NAME + "." + cloudCN;
		if (!clientCN.equalsIgnoreCase(sysopCN)) {
			log.debug("Only the local system operator can use {}, access denied!", requestTarget);
		    throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}
	}
	
	private void checkProviderAccessToRegister(final String clientCN, final String requestJSON, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		final ServiceRegistryRequestDTO requestBody = Utilities.fromJson(requestJSON, ServiceRegistryRequestDTO.class);
		final String providerName = requestBody.getProviderSystem() != null ? requestBody.getProviderSystem().getSystemName() : null;
		if (Utilities.isEmpty(providerName)) {
			log.debug("Provider name is not set in the body when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!providerName.equalsIgnoreCase(clientName) && !providerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name(" + providerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}