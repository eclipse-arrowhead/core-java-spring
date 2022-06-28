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
	
	private static final CoreSystem[] allowedCoreSystemsForQuery = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER, CoreSystem.CERTIFICATEAUTHORITY, CoreSystem.EVENTHANDLER,
																	 CoreSystem.AUTHORIZATION, CoreSystem.QOSMONITOR, CoreSystem.ONBOARDINGCONTROLLER, CoreSystem.DEVICEREGISTRY,
															         CoreSystem.SYSTEMREGISTRY, CoreSystem.PLANTDESCRIPTIONENGINE, CoreSystem.CHOREOGRAPHER, CoreSystem.HAWKBITCONFIGURATIONMANAGER,
															         CoreSystem.MSCV };
	private static final CoreSystem[] allowedCoreSystemsForQueryBySystemId = { CoreSystem.ORCHESTRATOR };
	private static final CoreSystem[] allowedCoreSystemsForQueryBySystemDTO = { CoreSystem.ORCHESTRATOR, CoreSystem.CHOREOGRAPHER };
	private static final CoreSystem[] allowedCoreSystemsForQueryAll = { CoreSystem.QOSMONITOR, CoreSystem.GATEKEEPER };
	private static final CoreSystem[] allowedCoreSystemsForRegisterSystem = { CoreSystem.PLANTDESCRIPTIONENGINE, CoreSystem.CHOREOGRAPHER};
	private static final CoreSystem[] allowedCoreSystemsForPullSystems = { CoreSystem.PLANTDESCRIPTIONENGINE};
	private static final CoreSystem[] allowedCoreSystemsForQueryServicesBySystemId = { CoreSystem.CHOREOGRAPHER };
	private static final CoreSystem[] allowedCoreSystemsForQueryServicesByServiceDefList = { CoreSystem.CHOREOGRAPHER };
	
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
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI)) {
			// A provider system can only register its own services!
			checkProviderAccessAndReservationsToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI)) {
			// A provider system can only unregister its own services!
			checkApplicationSystemAccessToDeregister(clientCN, queryParams, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_QUERY_URI)) {
			if (isClientACoreSystem(clientCN, cloudCN)) {
				// Only dedicated core systems can use this service without limitation but every core system can query info about its own services
				checkIfClientAnAllowedCoreSystemOrQueryingOwnSystems(clientCN, cloudCN, requestJSON, requestTarget); 
			} else {
				// Public core system services are allowed to query directly by the local systems
				checkIfRequestedServiceIsAPublicCoreSystemService(requestJSON);
			}
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQuery, requestTarget);
		} else if (requestTarget.contains(CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_URI.replace(ID_PATH_VARIABLE, ""))) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryBySystemId, requestTarget);
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryBySystemDTO, requestTarget);
		} else if (requestTarget.contains(CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SYSTEM_ID_URI.replace(ID_PATH_VARIABLE, ""))) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryServicesBySystemId, requestTarget);
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SERVICE_DEFINITION_LIST_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryServicesByServiceDefList, requestTarget);
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQueryAll, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_REGISTER_SYSTEM_URI)) {
			if (isClientACoreSystem(clientCN, cloudCN)) {
				checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForRegisterSystem, requestTarget);
			} else {
				// An application system can only register its own system!
				checkIfApplicationSystemIsRegisteringOwnSystem(clientCN, cloudCN, requestJSON, requestTarget);				
			}
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI)) {
			if (isClientACoreSystem(clientCN, cloudCN)) {
				checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForRegisterSystem, requestTarget);
			} else {
				// An application system can only unregister its own system!
				checkApplicationSystemAccessToDeregister(clientCN, queryParams, requestTarget);	
			}
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI)) {
			// Only dedicated core systems can use this service
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForPullSystems, requestTarget);
		} else if (requestTarget.endsWith(CoreCommonConstants.OP_SERVICEREGISTRY_PULL_CONFIG_URI)) {
			// Only Core Systems are allowed to pull the SR config
			checkIfClientIsACoreSystem(clientCN, cloudCN);
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

		if (!providerName.equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name (" + providerName + ") and certificate common name (" + clientName + ") do not match!", HttpStatus.UNAUTHORIZED.value());
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
	private void checkApplicationSystemAccessToDeregister(final String clientCN, final Map<String,String[]> queryParams, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		
		final String appSysName = queryParams.getOrDefault(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, new String[] { "" })[0];
		if (Utilities.isEmpty(appSysName)) {
			log.debug("Application system name is not set in the query parameters when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!appSysName.equalsIgnoreCase(clientName)) {
			log.debug("Application system name and certificate common name do not match! Unregistering denied!");
			throw new AuthException("Application system name (" + appSysName + ") and certificate common name (" + clientName + ") do not match!", HttpStatus.UNAUTHORIZED.value());
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
	private void checkIfClientIsACoreSystem(final String clientCN, final String cloudCN) {
		if (!isClientACoreSystem(clientCN, cloudCN)) {
			throw new AuthException("Only Core Systems are allowed to use this endpoint", HttpStatus.UNAUTHORIZED.value());
		}
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
	private void checkIfApplicationSystemIsRegisteringOwnSystem(final String clientCN, final String cloudCN, final String requestJSON, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		final SystemRequestDTO requestBody = Utilities.fromJson(requestJSON, SystemRequestDTO.class);
		final String applicationName = requestBody.getSystemName();
		
		if (Utilities.isEmpty(applicationName)) {
			log.debug("Application system name is not set in the query parameters when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!applicationName.equalsIgnoreCase(clientName)) {
			log.debug("Application system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Application system name(" + applicationName + ") and certificate common name (" + clientName + ") do not match!", HttpStatus.UNAUTHORIZED.value());
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