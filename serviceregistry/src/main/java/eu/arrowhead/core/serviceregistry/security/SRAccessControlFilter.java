package eu.arrowhead.core.serviceregistry.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class SRAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForQuery = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER, CoreSystem.CERTIFICATE_AUTHORITY };
	private static final CoreSystem[] allowedCoreSystemsForQueryBySystemId = { CoreSystem.ORCHESTRATOR };
	private static final CoreSystem[] allowedCoreSystemsForQueryBySystemDTO = { CoreSystem.ORCHESTRATOR };
	private static final CoreSystemService[] publicCoreSystemServices = {CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
																		 CoreSystemService.EVENT_PUBLISH, CoreSystemService.EVENT_SUBSCRIBE};
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		if (requestTarget.contains(CommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI)) {
			// A provider system can only register its own services!
			checkProviderAccessToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI)) {
			// A provider system can only unregister its own services!
			checkProviderAccessToDeregister(clientCN, queryParams, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI)) {
			if (isClientACoreSystem(clientCN, cloudCN)) {
				// Only dedicated core systems can use this service
				checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQuery, requestTarget);				
			} else {
				// Public core system services are allowed to query directly by the local systems
				checkIfRequestedServiceIsAPublicCoreSystemService(requestJSON);
			}			
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryBySystemId, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryBySystemDTO, requestTarget);
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
	private void checkIfRequestedServiceIsAPublicCoreSystemService(final String requestJSON) {
		final ServiceQueryFormDTO requestBody = Utilities.fromJson(requestJSON, ServiceQueryFormDTO.class);
		
		for (final CoreSystemService service : publicCoreSystemServices) {
			if (service.getServiceDefinition().equalsIgnoreCase(requestBody.getServiceDefinitionRequirement().trim())) {
				return;
			}
		}
		
		throw new AuthException("Only public core system services are allowed to query directly. Requested service(" + requestBody.getServiceDefinitionRequirement() + ") is not!", HttpStatus.UNAUTHORIZED.value());
	}
	
	//-------------------------------------------------------------------------------------------------
	protected boolean  isClientACoreSystem(final String clientCN, final String cloudCN) {
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			final String coreSystemCN = coreSystem.name().toLowerCase() + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return true;
			}
		}		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}