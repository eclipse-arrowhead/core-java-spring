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

package eu.arrowhead.core.serviceregistry.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true)
public class SRAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForQuery = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER, CoreSystem.CERTIFICATE_AUTHORITY, CoreSystem.EVENT_HANDLER,
																	 CoreSystem.AUTHORIZATION, CoreSystem.QOS_MONITOR, CoreSystem.ONBOARDING_CONTROLLER, CoreSystem.DEVICE_REGISTRY,
															         CoreSystem.SYSTEM_REGISTRY, };
	private static final CoreSystem[] allowedCoreSystemsForQueryBySystemId = { CoreSystem.ORCHESTRATOR };
	private static final CoreSystem[] allowedCoreSystemsForQueryBySystemDTO = { CoreSystem.ORCHESTRATOR };
	private static final CoreSystem[] allowedCoreSystemsForQueryAll = { CoreSystem.QOS_MONITOR, CoreSystem.GATEKEEPER };
	
	private static final String ID_PATH_VARIABLE = "{" + CommonConstants.COMMON_FIELD_NAME_ID + "}"; 
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		
		if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI)) {
			// A provider system can only register its own services!
			checkProviderAccessAndReservationsToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI)) {
			// A provider system can only unregister its own services!
			checkProviderAccessToDeregister(clientCN, queryParams, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI)) {
			if (isClientACoreSystem(clientCN, cloudCN)) {
				// Only dedicated core systems can use this service without limitation but every core system can query info about its own services
				checkIfClientAnAllowedCoreSystemOrQueryingOwnSystems(clientCN, cloudCN, requestJSON, requestTarget); 
			} else {
				// Public core system services are allowed to query directly by the local systems
				checkIfRequestedServiceIsAPublicCoreSystemService(requestJSON);
			}			
		} else if (requestTarget.contains(CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI.replace(ID_PATH_VARIABLE, ""))) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryBySystemId, requestTarget);
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryBySystemDTO, requestTarget);
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_ALL_SERVICE_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryAll, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_REGISTER_SYSTEM_URI)) {
			// A consumer system can only register its own system!
			checkIfConsumerIsRegisteringOwnSystem(clientCN, cloudCN, requestJSON, requestTarget);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkProviderAccessAndReservationsToRegister(final String clientCN, final String requestJSON, final String requestTarget) {
		final String cloudCN = getServerCloudCN();
		final String clientName = getClientNameFromCN(clientCN);
		final ServiceRegistryRequestDTO requestBody = Utilities.fromJson(requestJSON, ServiceRegistryRequestDTO.class);
		final String providerName = requestBody.getProviderSystem() != null ? requestBody.getProviderSystem().getSystemName() : "";
		final String  serviceDefinition = requestBody.getServiceDefinition() != null ? requestBody.getServiceDefinition().trim() : "";
		if (Utilities.isEmpty(providerName)) {
			log.debug("Provider name is not set in the body when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
	
		// Translator must be able to register external services:
		if (clientCN.startsWith(CommonConstants.CORE_SYSTEM_TRANSLATOR.toLowerCase()+ ".")) {
			return;
		}

		if (!providerName.equalsIgnoreCase(clientName) && !providerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name(" + providerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
		
		if (!isClientACoreSystem(clientCN, cloudCN)) {
			for (final CoreSystemService coreSystemService : CoreSystemService.values()) {
				if (serviceDefinition.equalsIgnoreCase(coreSystemService.getServiceDefinition())) {
					log.debug("ServiceDefinition is not authorized to use. Registering denied!");
					throw new AuthException("ServiceDefinition is not authorized to use. '" + requestBody.getServiceDefinition() + "' is a reserved arrowhead core service definition.", HttpStatus.UNAUTHORIZED.value());
				}
			}
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
		
		if (Utilities.isEmpty(requestBody.getServiceDefinitionRequirement())) {
			throw new AuthException("Service is not defined.", HttpStatus.UNAUTHORIZED.value());
		}
		
		for (final CoreSystemService service : CommonConstants.PUBLIC_CORE_SYSTEM_SERVICES) {
			if (service.getServiceDefinition().equalsIgnoreCase(requestBody.getServiceDefinitionRequirement().trim())) {
				return;
			}
		}
		
		throw new AuthException("Only public core system services are allowed to query directly. Requested service (" + requestBody.getServiceDefinitionRequirement() + ") is not!",
								HttpStatus.UNAUTHORIZED.value());
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isClientACoreSystem(final String clientCN, final String cloudCN) {
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			final String coreSystemCN = coreSystem.name().toLowerCase() + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return true;
			}
		}		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkIfClientAnAllowedCoreSystemOrQueryingOwnSystems(final String clientCN, final String cloudCN, final String requestJSON, final String requestTarget) {
		final boolean firstCheck = checkIfClientIsAnAllowedCoreSystemNoException(clientCN, cloudCN, allowedCoreSystemsForQuery, requestTarget);
		
		if (!firstCheck) { // no privileged core system
			final CoreSystem coreSystem = getClientCoreSystem(clientCN, cloudCN);
			
			if (coreSystem != null) {
				final ServiceQueryFormDTO requestBody = Utilities.fromJson(requestJSON, ServiceQueryFormDTO.class);
				
				if (Utilities.isEmpty(requestBody.getServiceDefinitionRequirement())) {
					throw new AuthException("Service is not defined.", HttpStatus.UNAUTHORIZED.value());
				}
				
				for (final CoreSystemService service : coreSystem.getServices()) {
					if (service.getServiceDefinition().equalsIgnoreCase(requestBody.getServiceDefinitionRequirement().trim())) {
						return;
					}
				}

				// Translator must be able to register external services:
				if (clientCN.startsWith(CommonConstants.CORE_SYSTEM_TRANSLATOR.toLowerCase()+ ".")) {
					return;
				}
			}
			
			throw new AuthException("This core system only query data about its own services.", HttpStatus.UNAUTHORIZED.value());
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkIfConsumerIsRegisteringOwnSystem(final String clientCN, final String cloudCN, final String requestJSON,
			final String requestTarget) {

		final String clientName = getClientNameFromCN(clientCN);
		final SystemRequestDTO requestBody = Utilities.fromJson(requestJSON, SystemRequestDTO.class);
		final String consumerName = requestBody.getSystemName();
		
		if (Utilities.isEmpty(consumerName)) {
			log.debug("Consumer name is not set in the query parameters when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!consumerName.equalsIgnoreCase(clientName) && !consumerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Consumer system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Consumer system name(" + consumerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}

	//-------------------------------------------------------------------------------------------------
	private CoreSystem getClientCoreSystem(final String clientCN, final String cloudCN) {
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			final String coreSystemCN = coreSystem.name().toLowerCase() + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return coreSystem;
			}
		}		
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}