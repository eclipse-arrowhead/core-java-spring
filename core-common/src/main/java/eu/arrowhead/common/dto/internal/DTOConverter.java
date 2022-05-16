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

package eu.arrowhead.common.dto.internal;


import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.AuthorizationInterCloud;
import eu.arrowhead.common.database.entity.AuthorizationInterCloudInterfaceConnection;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloudInterfaceConnection;
import eu.arrowhead.common.database.entity.CaCertificate;
import eu.arrowhead.common.database.entity.CaTrustedKey;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.DeviceRegistry;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.ForeignSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.database.entity.QoSInterDirectMeasurement;
import eu.arrowhead.common.database.entity.QoSInterDirectPingMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSReservation;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.SystemRegistry;
import eu.arrowhead.common.dto.shared.ChoreographerActionResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStepListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStepResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerWorklogListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerWorklogResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceQueryResultDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.EventTypeResponseDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemQueryResultDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class DTOConverter {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertSystemToSystemResponseDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		return new SystemResponseDTO(system.getId(), system.getSystemName(), system.getAddress(), system.getPort(), system.getAuthenticationInfo(), Utilities.text2Map(system.getMetadata()),
									 Utilities.convertZonedDateTimeToUTCString(system.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(system.getUpdatedAt()));		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemListResponseDTO convertSystemEntryListToSystemListResponseDTO(final Page<System> systemEntryList) {
		Assert.notNull(systemEntryList, "systemEntryList is null");
		
		final long count = systemEntryList.getTotalElements();		
		final SystemListResponseDTO systemListResponseDTO = new SystemListResponseDTO();
		systemListResponseDTO.setCount(count);
		systemListResponseDTO.setData(systemEntryListToSystemResponseDTOList(systemEntryList.getContent()));
		
		return systemListResponseDTO;
	}
		
	//-------------------------------------------------------------------------------------------------
	public static ServiceDefinitionResponseDTO convertServiceDefinitionToServiceDefinitionResponseDTO(final ServiceDefinition serviceDefinition) {
		Assert.notNull(serviceDefinition, "ServiceDefinition is null");
		
		return new ServiceDefinitionResponseDTO(serviceDefinition.getId(), serviceDefinition.getServiceDefinition(), Utilities.convertZonedDateTimeToUTCString(serviceDefinition.getCreatedAt()),
												Utilities.convertZonedDateTimeToUTCString(serviceDefinition.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ServiceDefinitionsListResponseDTO convertServiceDefinitionsListToServiceDefinitionListResponseDTO(final Page<ServiceDefinition> serviceDefinitions) {
		Assert.notNull(serviceDefinitions, "List of ServiceDefinition is null");
		
		final List<ServiceDefinitionResponseDTO> serviceDefinitionDTOs = new ArrayList<>(serviceDefinitions.getNumberOfElements());
		for (final ServiceDefinition definition : serviceDefinitions) {
			serviceDefinitionDTOs.add(convertServiceDefinitionToServiceDefinitionResponseDTO(definition));
		}		
		
		return new ServiceDefinitionsListResponseDTO(serviceDefinitionDTOs, serviceDefinitions.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ServiceRegistryResponseDTO convertServiceRegistryToServiceRegistryResponseDTO(final ServiceRegistry entry) {
		Assert.notNull(entry, "SR entry is null.");
		Assert.notNull(entry.getServiceDefinition(), "Related service definition is null.");
		Assert.notNull(entry.getSystem(), "Related system is null.");
		Assert.notNull(entry.getInterfaceConnections(), "Related interface connection set is null.");
		Assert.isTrue(!entry.getInterfaceConnections().isEmpty(), "Related interface connection set is empty.");
		
		final ServiceDefinitionResponseDTO serviceDefinitionDTO = convertServiceDefinitionToServiceDefinitionResponseDTO(entry.getServiceDefinition());
		final SystemResponseDTO systemDTO = convertSystemToSystemResponseDTO(entry.getSystem());
		
		final ServiceRegistryResponseDTO dto = new ServiceRegistryResponseDTO();
		dto.setId(entry.getId());
		dto.setServiceDefinition(serviceDefinitionDTO);
		dto.setProvider(systemDTO);
		dto.setServiceUri(entry.getServiceUri());
		dto.setEndOfValidity(Utilities.convertZonedDateTimeToUTCString(entry.getEndOfValidity()));
		dto.setSecure(entry.getSecure());
		dto.setMetadata(Utilities.text2Map(entry.getMetadata()));
		dto.setVersion(entry.getVersion());
		dto.setInterfaces(collectInterfacesFromServiceRegistry(entry.getInterfaceConnections()));
		dto.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()));
		dto.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
		
		return dto;
	}

	//-------------------------------------------------------------------------------------------------
	public static ServiceRegistryListResponseDTO convertServiceRegistryListToServiceRegistryListResponseDTO(final Page<ServiceRegistry> serviceRegistryEntries) {
		Assert.notNull(serviceRegistryEntries, "List of serviceRegistryEntries is null");
		
		final List<ServiceRegistryResponseDTO> serviceRegistryEntryDTOs = new ArrayList<>(serviceRegistryEntries.getNumberOfElements());
		for (final ServiceRegistry srEntry: serviceRegistryEntries) {
			serviceRegistryEntryDTOs.add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
		}
		
		return new ServiceRegistryListResponseDTO(serviceRegistryEntryDTOs, serviceRegistryEntries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3776")
	public static ServiceRegistryGroupedResponseDTO convertServiceRegistryDataToServiceRegistryGroupedResponseDTO(final Iterable<ServiceDefinition> serviceDefinitionEntries,
																												  final Iterable<System> systemEntries, 
																												  final Iterable<ServiceInterface> interfaceEntries,
																												  final Iterable<ServiceRegistry> serviceRegistryEntries) {
		Assert.notNull(serviceDefinitionEntries, "List of serviceDefinitionEntries is null");
		Assert.notNull(systemEntries, "List of systemEntries is null");
		Assert.notNull(interfaceEntries, "List of interfaceEntries is null");
		Assert.notNull(serviceRegistryEntries, "List of serviceRegistryEntries is null");
		
		final Map<Long,ServicesGroupedBySystemsResponseDTO> servicesBySystemId = new HashMap<>();
		final Map<Long,ServicesGroupedByServiceDefinitionResponseDTO> servicesByServiceDefinition = new HashMap<>();
		final List<IdValueDTO> servicesForAutoComplete = new ArrayList<>();
		final List<SystemResponseDTO> systemsForAutoComplete = new ArrayList<>();
		final List<IdValueDTO> interfacesForAutoComplete = new ArrayList<>();
						
		for (final ServiceRegistry srEntry: serviceRegistryEntries) {
			final long systemId = srEntry.getSystem().getId();
			final String systemName = srEntry.getSystem().getSystemName();
			final String systemAddress = srEntry.getSystem().getAddress();
			final int systemPort = srEntry.getSystem().getPort();
			final Map<String,String> systemMetadata = Utilities.text2Map(srEntry.getSystem().getMetadata());
			final long serviceDefinitionId = srEntry.getServiceDefinition().getId();
			final String serviceDefinition = srEntry.getServiceDefinition().getServiceDefinition();		
			
			// Creating ServicesGroupedBySystemsResponseDTO
			if (servicesBySystemId.containsKey(systemId)) {
				servicesBySystemId.get(systemId).getServices().add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
			} else {
				final ServicesGroupedBySystemsResponseDTO dto = new ServicesGroupedBySystemsResponseDTO(systemId, systemName, systemAddress, systemPort, systemMetadata, new ArrayList<>());
				dto.getServices().add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
				servicesBySystemId.put(systemId, dto);
			}
			
			// Creating ServicesGroupedByServiceDefinitionResponseDTO
			if (servicesByServiceDefinition.containsKey(serviceDefinitionId)) {
				servicesByServiceDefinition.get(serviceDefinitionId).getProviderServices().add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
			} else {
				final ServicesGroupedByServiceDefinitionResponseDTO dto = new ServicesGroupedByServiceDefinitionResponseDTO(serviceDefinitionId, serviceDefinition, new ArrayList<>());
				dto.getProviderServices().add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
				servicesByServiceDefinition.put(serviceDefinitionId, dto);
			}
		}
			
		// Creating AutoCompleteDataResponseDTO	
		for (final ServiceDefinition serviceDefinition : serviceDefinitionEntries) {
			servicesForAutoComplete.add(new IdValueDTO(serviceDefinition.getId(), serviceDefinition.getServiceDefinition()));
		}
		for (final System system : systemEntries) {
			systemsForAutoComplete.add(DTOConverter.convertSystemToSystemResponseDTO(system));
		}
		for (final ServiceInterface serviceInterface : interfaceEntries) {
			interfacesForAutoComplete.add(new IdValueDTO(serviceInterface.getId(), serviceInterface.getInterfaceName()));
		}
		
		final List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystemsResponseDTOList = new ArrayList<>();
		servicesGroupedBySystemsResponseDTOList.addAll(servicesBySystemId.values());
		final List<ServicesGroupedByServiceDefinitionResponseDTO> servicesGroupedByServiceDefinitionResponseDTOList = new ArrayList<>();
		servicesGroupedByServiceDefinitionResponseDTOList.addAll(servicesByServiceDefinition.values());
		final AutoCompleteDataResponseDTO autoCompleteDataResponseDTO = new AutoCompleteDataResponseDTO(servicesForAutoComplete, systemsForAutoComplete, interfacesForAutoComplete);
		
		return new ServiceRegistryGroupedResponseDTO(servicesGroupedBySystemsResponseDTOList, servicesGroupedByServiceDefinitionResponseDTOList, autoCompleteDataResponseDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ServiceInterfaceResponseDTO convertServiceInterfaceToServiceInterfaceResponseDTO(final ServiceInterface intf) {
		Assert.notNull(intf, "Interface entry is null.");
		
		return new ServiceInterfaceResponseDTO(intf.getId(), intf.getInterfaceName(), Utilities.convertZonedDateTimeToUTCString(intf.getCreatedAt()), 
											   Utilities.convertZonedDateTimeToUTCString(intf.getUpdatedAt()));
	}

	//-------------------------------------------------------------------------------------------------
	public static ServiceInterfacesListResponseDTO convertServiceInterfacesListToServiceInterfaceListResponseDTO(final Page<ServiceInterface> serviceInterfaces) {
		Assert.notNull(serviceInterfaces, "List of ServiceInterface is null");

		final List<ServiceInterfaceResponseDTO> serviceInterfaceDTOs = new ArrayList<>(serviceInterfaces.getNumberOfElements());
		for (final ServiceInterface serviceInterface : serviceInterfaces) {
			serviceInterfaceDTOs.add(convertServiceInterfaceToServiceInterfaceResponseDTO(serviceInterface));
		}

		return new ServiceInterfacesListResponseDTO(serviceInterfaceDTOs, serviceInterfaces.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ServiceQueryResultDTO convertListOfServiceRegistryEntriesToServiceQueryResultDTO(final List<ServiceRegistry> entries, final int unfilteredHits) {
		final ServiceQueryResultDTO result = new ServiceQueryResultDTO();
		
		if (entries != null) {
			Assert.isTrue(unfilteredHits >= entries.size(), "Invalid value of unfiltered hits: " + unfilteredHits);
			result.setUnfilteredHits(unfilteredHits);
			for (final ServiceRegistry srEntry : entries) {
				result.getServiceQueryData().add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static AuthorizationIntraCloudResponseDTO convertAuthorizationIntraCloudToAuthorizationIntraCloudResponseDTO(final AuthorizationIntraCloud entry) {
		Assert.notNull(entry, "AuthorizationIntraCloud is null");
		Assert.notNull(entry.getConsumerSystem(), "Consumer is null");
		Assert.notNull(entry.getProviderSystem(), "Provider is null");
		Assert.notNull(entry.getServiceDefinition(), "ServiceDefintion is null");
		Assert.notNull(entry.getInterfaceConnections(), "InterfaceConnections is null");
		
		return new AuthorizationIntraCloudResponseDTO(entry.getId(), convertSystemToSystemResponseDTO(entry.getConsumerSystem()), convertSystemToSystemResponseDTO(entry.getProviderSystem()), 
													  convertServiceDefinitionToServiceDefinitionResponseDTO(entry.getServiceDefinition()),
													  collectInterfacesFromAuthorizationIntraCloud(entry.getInterfaceConnections()),
													  Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static AuthorizationIntraCloudListResponseDTO convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(final Page<AuthorizationIntraCloud> entries) {
		Assert.notNull(entries, "AuthorizationIntraCloudList is null");
		
		final List<AuthorizationIntraCloudResponseDTO> authorizationIntraCloudEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final AuthorizationIntraCloud entry : entries) {
			authorizationIntraCloudEntries.add(convertAuthorizationIntraCloudToAuthorizationIntraCloudResponseDTO(entry));
		}
		
		return new AuthorizationIntraCloudListResponseDTO(authorizationIntraCloudEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static TokenGenerationResponseDTO convertTokenMapToTokenGenerationResponseDTO(final Map<SystemRequestDTO,Map<String,String>> tokenMap) {
		Assert.notNull(tokenMap, "Token map is null.");
		
		final TokenGenerationResponseDTO result = new TokenGenerationResponseDTO();
		for (final Entry<SystemRequestDTO,Map<String,String>> entry : tokenMap.entrySet()) {
			result.getTokenData().add(new TokenDataDTO(entry.getKey(), entry.getValue()));
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------	
	public static CloudListResponseDTO convertCloudToCloudResponseDTO(final Page<Cloud> entries) {
		Assert.notNull(entries, "Cloud list is null" );
		
		final List<CloudResponseDTO> cloudEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final Cloud entry : entries) {
			cloudEntries.add(convertCloudToCloudResponseDTO(entry));
		}
		
		return new CloudListResponseDTO(cloudEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------	
	public static CloudResponseDTO convertCloudToCloudResponseDTO(final Cloud entity) {
		Assert.notNull(entity, "Cloud is null" );
		Assert.notNull(entity.getOperator(), "Cloud.operator is null" );
		Assert.notNull(entity.getName(), "Cloud.name is null" );
		Assert.notNull(entity.getCreatedAt(), "Cloud.createdAt is null" );
		Assert.notNull(entity.getUpdatedAt(), "Cloud.cpdatedAt is null" );
		
		return new CloudResponseDTO(entity.getId(), entity.getOperator(), entity.getName(), entity.getSecure(), entity.getNeighbor(), entity.getOwnCloud(), entity.getAuthenticationInfo(),
								   Utilities.convertZonedDateTimeToUTCString(entity.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entity.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static RelayListResponseDTO convertRelayListToRelayResponseListDTO(final Page<Relay> entries) {
		Assert.notNull(entries, "Relay list is null" );
		
		final List<RelayResponseDTO> relayEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final Relay entry : entries) {
			relayEntries.add(convertRelayToRelayResponseDTO(entry));
		}
		
		return new RelayListResponseDTO(relayEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static RelayResponseDTO convertRelayToRelayResponseDTO(final Relay entry) {
		Assert.notNull(entry, "Relay is null");
		Assert.notNull(entry.getAddress(), "Relay.address is null");
		Assert.notNull(entry.getType(), "Relay.type is null");
		Assert.notNull(entry.getCreatedAt(), "Relay.createdAt is null");
		Assert.notNull(entry.getCreatedAt(), "Relay.updatedAt is null");
		
		return new RelayResponseDTO(entry.getId(), entry.getAddress(), entry.getPort(), entry.getAuthenticationInfo(), entry.getSecure(), entry.getExclusive(), entry.getType(),
									Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static List<RelayRequestDTO> convertRelayListToRelayRequestDTOList(final List<Relay> relays) {
		Assert.notNull(relays, "Relay list is null.");
		
		final List<RelayRequestDTO> result = new ArrayList<>(relays.size());
		for (final Relay relay : relays) {
			result.add(convertRelayToRelayRequestDTO(relay));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static RelayRequestDTO convertRelayToRelayRequestDTO(final Relay relay) {
		Assert.notNull(relay, "relay is null.");
		
		return new RelayRequestDTO(relay.getAddress(), relay.getPort(), relay.getAuthenticationInfo(), relay.getSecure(), relay.getExclusive(), relay.getType().name());
	}
	
	//-------------------------------------------------------------------------------------------------	
	public static CloudWithRelaysListResponseDTO convertCloudToCloudWithRelaysListResponseDTO(final Page<Cloud> entries) {
		Assert.notNull(entries, "Cloud list is null" );
		
		final List<CloudWithRelaysResponseDTO> cloudWithRelaysResponseDTOList = new ArrayList<>(entries.getNumberOfElements());
		for (final Cloud cloud : entries) {
			Assert.notNull(cloud.getGatekeeperRelays(), "CloudGatekeeperRelay set is null");
			Assert.notNull(cloud.getGatewayRelays(), "CloudGatewayRelay set is null");
			
			cloudWithRelaysResponseDTOList.add(convertCloudToCloudWithRelaysResponseDTO(cloud));
		}
		
		return new CloudWithRelaysListResponseDTO(cloudWithRelaysResponseDTOList, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------	
	public static CloudWithRelaysAndPublicRelaysListResponseDTO convertCloudToCloudWithRelaysAndPublicRelaysListResponseDTO(final Page<Cloud> entries, final List<Relay> publicRelays) {
		Assert.notNull(entries, "Cloud list is null" );
		Assert.notNull(publicRelays, "PublicRelay list is null");
		
		final List<CloudWithRelaysAndPublicRelaysResponseDTO> cloudWithRelaysResponseDTOList = new ArrayList<>(entries.getNumberOfElements());
		for (final Cloud cloud : entries) {
			Assert.notNull(cloud.getGatekeeperRelays(), "CloudGatekeeperRelay set is null");
			Assert.notNull(cloud.getGatewayRelays(), "CloudGatewayRelay set is null");
			
			cloudWithRelaysResponseDTOList.add(convertCloudToCloudWithRelaysAndPublicRelaysResponseDTO(cloud, publicRelays));
		}
		
		return new CloudWithRelaysAndPublicRelaysListResponseDTO(cloudWithRelaysResponseDTOList, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static CloudWithRelaysAndPublicRelaysResponseDTO convertCloudToCloudWithRelaysAndPublicRelaysResponseDTO(final Cloud cloud, final List<Relay> publicRelays) {
		Assert.notNull(cloud, "Cloud is null");
		Assert.notNull(cloud.getGatekeeperRelays(), "Gatekeeper relays set is null");
		Assert.notNull(cloud.getGatewayRelays(), "Gateway relays set is null");
		Assert.notNull(publicRelays, "PublicRelay list is null");
		
		final CloudResponseDTO cloudResponseDTO = convertCloudToCloudResponseDTO(cloud);
		
		final Set<Relay> gatekeeperRelays = new HashSet<>();
		for (final CloudGatekeeperRelay conn : cloud.getGatekeeperRelays()) {
			gatekeeperRelays.add(conn.getRelay());
		}		
		
		final Set<Relay> gatewayRelays = new HashSet<>();
		for (final CloudGatewayRelay conn : cloud.getGatewayRelays()) {
			gatewayRelays.add(conn.getRelay());
		}
		
		final List<RelayResponseDTO> gatekeeperRelayListDTO = new ArrayList<>(gatekeeperRelays.size());
		for (final Relay gatekeeperRelay : gatekeeperRelays) {
			gatekeeperRelayListDTO.add(convertRelayToRelayResponseDTO(gatekeeperRelay));
		}
		
		final List<RelayResponseDTO> gatewayRelayListDTO = new ArrayList<>(gatewayRelays.size());
		for (final Relay gatewayRelay : gatewayRelays) {
			gatewayRelayListDTO.add(convertRelayToRelayResponseDTO(gatewayRelay));
		}
		
		final List<RelayResponseDTO> publicRelayListDTO = new ArrayList<>(publicRelays.size());
		for (final Relay publicRelay : publicRelays) {
			publicRelayListDTO.add(convertRelayToRelayResponseDTO(publicRelay));
		}
		
		return new CloudWithRelaysAndPublicRelaysResponseDTO(cloudResponseDTO.getId(), cloudResponseDTO.getOperator(), cloudResponseDTO.getName(), cloudResponseDTO.getSecure(),
											  cloudResponseDTO.getNeighbor(), cloudResponseDTO.getOwnCloud(), cloudResponseDTO.getAuthenticationInfo(),
											  cloudResponseDTO.getCreatedAt(), cloudResponseDTO.getUpdatedAt(), gatekeeperRelayListDTO, gatewayRelayListDTO, publicRelayListDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static CloudWithRelaysResponseDTO convertCloudToCloudWithRelaysResponseDTO(final Cloud cloud) {
		Assert.notNull(cloud, "Cloud is null");
		Assert.notNull(cloud.getGatekeeperRelays(), "Gatekeeper relays set is null");
		Assert.notNull(cloud.getGatewayRelays(), "Gateway relays set is null");
		
		final CloudResponseDTO cloudResponseDTO = convertCloudToCloudResponseDTO(cloud);
		
		final Set<Relay> gatekeeperRelays = new HashSet<>();
		for (final CloudGatekeeperRelay conn : cloud.getGatekeeperRelays()) {
			gatekeeperRelays.add(conn.getRelay());
		}		
		
		final Set<Relay> gatewayRelays = new HashSet<>();
		for (final CloudGatewayRelay conn : cloud.getGatewayRelays()) {
			gatewayRelays.add(conn.getRelay());
		}
		
		final List<RelayResponseDTO> gatekeeperRelayListDTO = new ArrayList<>(gatekeeperRelays.size());
		for (final Relay gatekeeperRelay : gatekeeperRelays) {
			gatekeeperRelayListDTO.add(convertRelayToRelayResponseDTO(gatekeeperRelay));
		}
		
		final List<RelayResponseDTO> gatewayRelayListDTO = new ArrayList<>(gatewayRelays.size());
		for (final Relay gatewayRelay : gatewayRelays) {
			gatewayRelayListDTO.add(convertRelayToRelayResponseDTO(gatewayRelay));
		}
		
		return new CloudWithRelaysResponseDTO(cloudResponseDTO.getId(), cloudResponseDTO.getOperator(), cloudResponseDTO.getName(), cloudResponseDTO.getSecure(),
											  cloudResponseDTO.getNeighbor(), cloudResponseDTO.getOwnCloud(), cloudResponseDTO.getAuthenticationInfo(),
											  cloudResponseDTO.getCreatedAt(), cloudResponseDTO.getUpdatedAt(), gatekeeperRelayListDTO, gatewayRelayListDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static AuthorizationInterCloudListResponseDTO convertAuthorizationInterCloudListToAuthorizationInterCloudListResponseDTO(final Page<AuthorizationInterCloud> entries) {
		Assert.notNull(entries, "AuthorizationInterCloudList is null");
		
		final List<AuthorizationInterCloudResponseDTO> authorizationInterCloudEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final AuthorizationInterCloud entry : entries) {
			authorizationInterCloudEntries.add(convertAuthorizationInterCloudToAuthorizationInterCloudResponseDTO(entry));
		}
		
		return new AuthorizationInterCloudListResponseDTO(authorizationInterCloudEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static AuthorizationInterCloudResponseDTO convertAuthorizationInterCloudToAuthorizationInterCloudResponseDTO(final AuthorizationInterCloud entity) {
		Assert.notNull(entity, "AuthorizationInterCloud is null" );
		Assert.notNull(entity.getCloud(), "AuthorizationInterCloud.Cloud is null" );
		Assert.notNull(entity.getProvider(), "AuthorizationInterCloud.Provider is null");
		Assert.notNull(entity.getServiceDefinition(), "AuthorizationInterCloud.ServiceDefinition is null" );
		Assert.notNull(entity.getInterfaceConnections(), "AuthorizationInterCloud.InterfaceConnections is null");
		Assert.notNull(entity.getCreatedAt(), "AuthorizationInterCloud.CreatedAt is null" );
		Assert.notNull(entity.getUpdatedAt(), "AuthorizationInterCloud.UpdatedAt is null" );
		
		return new AuthorizationInterCloudResponseDTO(entity.getId(), convertCloudToCloudResponseDTO(entity.getCloud()), convertSystemToSystemResponseDTO(entity.getProvider()),
													  convertServiceDefinitionToServiceDefinitionResponseDTO(entity.getServiceDefinition()),
													  collectInterfacesFromAuthorizationInterCloud(entity.getInterfaceConnections()),
													  Utilities.convertZonedDateTimeToUTCString(entity.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entity.getUpdatedAt()));
		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static OrchestratorStoreResponseDTO convertOrchestratorStoreToOrchestratorStoreResponseDTO(final OrchestratorStore entity, final SystemResponseDTO providerSystem,
																									  final CloudResponseDTO providerCloud) {
		Assert.notNull(entity, "OrchestratorStore is null");            
		Assert.notNull(providerSystem, "OrchestratorStore.ProviderSystem is null");
        Assert.notNull(entity.getCreatedAt(), "OrchestratorStore.CreatedAt is null");        
        Assert.notNull(entity.getUpdatedAt(),  "OrchestratorStore.UpdatedAt is null"); 
	
		return new OrchestratorStoreResponseDTO(entity.getId(),	convertServiceDefinitionToServiceDefinitionResponseDTO(entity.getServiceDefinition()),
												convertSystemToSystemResponseDTO(entity.getConsumerSystem()), entity.isForeign(), providerSystem, providerCloud,
												convertServiceInterfaceToServiceInterfaceResponseDTO(entity.getServiceInterface()), entity.getPriority(), Utilities.text2Map(entity.getAttribute()),
												Utilities.convertZonedDateTimeToUTCString(entity.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entity.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static OrchestratorStoreListResponseDTO convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(final List<OrchestratorStoreResponseDTO> entries) {
		Assert.notNull(entries, "OrchestratorStoreList is null");

		return new OrchestratorStoreListResponseDTO(entries, entries.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static OrchestratorStoreListResponseDTO convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(final List<OrchestratorStoreResponseDTO> entries, final long totalElements) {
		Assert.notNull(entries, "OrchestratorStoreList is null");

		return new OrchestratorStoreListResponseDTO(entries, totalElements);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static OrchestratorStoreFlexibleResponseDTO convertOrchestratorStoreFlexibleEntryToOrchestratorStoreFlexibleResponseDTO(final OrchestratorStoreFlexible entry) {
		Assert.notNull(entry, "OrchestratorStoreFlexible entry is null");
		
		return new OrchestratorStoreFlexibleResponseDTO(entry.getId(),
														new SystemDescriberDTO(entry.getConsumerSystemName(), Utilities.text2Map(entry.getConsumerSystemMetadata())),
														new SystemDescriberDTO(entry.getProviderSystemName(), Utilities.text2Map(entry.getProviderSystemMetadata())),
														entry.getServiceDefinitionName(),
														entry.getServiceInterfaceName(),
														Utilities.text2Map(entry.getServiceMetadata()),
														entry.getPriority(),
														Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()),
														Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static OrchestratorStoreFlexibleListResponseDTO convertOrchestratorStoreFlexibleEntryListToOrchestratorStoreFlexibleListResponseDTO(final Iterable<OrchestratorStoreFlexible> entries, final long totalElements) {
		Assert.notNull(entries, "OrchestratorStoreFlexible list is null");
		
		final List<OrchestratorStoreFlexibleResponseDTO> data = new ArrayList<>();
		for (final OrchestratorStoreFlexible entry : entries) {
			data.add(convertOrchestratorStoreFlexibleEntryToOrchestratorStoreFlexibleResponseDTO(entry));
		}
		return new OrchestratorStoreFlexibleListResponseDTO(data, totalElements);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemRequestDTO convertSystemToSystemRequestDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setAddress(system.getAddress());
		systemRequestDTO.setSystemName(system.getSystemName());
		systemRequestDTO.setPort(system.getPort());
		systemRequestDTO.setAuthenticationInfo(systemRequestDTO.getAuthenticationInfo());
		systemRequestDTO.setMetadata(Utilities.text2Map(system.getMetadata()));
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemRequestDTO convertSystemResponseDTOToSystemRequestDTO(final SystemResponseDTO response) {
		Assert.notNull(response, "response is null");
		
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName(response.getSystemName());
		result.setAddress(response.getAddress());
		result.setPort(response.getPort());
		result.setAuthenticationInfo(response.getAuthenticationInfo());
		result.setMetadata(response.getMetadata());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static PreferredProviderDataDTO convertForeignOrchestratorStoreResponseDTOToPreferredProviderDataDTO(final OrchestratorStoreResponseDTO orchestratorStoreResponseDTO) {
		Assert.notNull(orchestratorStoreResponseDTO, "orchestratorStoreResponseDTO is null");
		Assert.isTrue(orchestratorStoreResponseDTO.getForeign(), "orchestratorStoreResponseDTO is not foreign");
		Assert.notNull(orchestratorStoreResponseDTO.getProviderSystem(), "orchestratorStoreResponseDTO.ProviderSystem is null");
		Assert.notNull(orchestratorStoreResponseDTO.getProviderCloud(), "orchestratorStoreResponseDTO.ProviderCloud is null");
		
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		
		final SystemRequestDTO providerSystemRequestDTO = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(orchestratorStoreResponseDTO.getProviderSystem());
		preferredProviderDataDTO.setProviderSystem(providerSystemRequestDTO);
		
		final CloudRequestDTO cloudRequestDTO = convertCloudResponseDTOToCloudRequestDTO(orchestratorStoreResponseDTO.getProviderCloud());
		preferredProviderDataDTO.setProviderCloud(cloudRequestDTO);

		return preferredProviderDataDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static CloudRequestDTO convertCloudResponseDTOToCloudRequestDTO(final CloudResponseDTO cloudResponseDTO) {
		Assert.notNull(cloudResponseDTO, "cloudResponseDTO is null");
		Assert.notNull(cloudResponseDTO.getOperator(), "cloudResponseDTO.Operator is null");
		Assert.notNull(cloudResponseDTO.getName(), "cloudResponseDTO.Name is null");
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		
		cloudRequestDTO.setOperator(cloudResponseDTO.getOperator());
		cloudRequestDTO.setName(cloudResponseDTO.getName());
		cloudRequestDTO.setSecure(cloudResponseDTO.getSecure());
		cloudRequestDTO.setNeighbor(cloudResponseDTO.getNeighbor());
		cloudRequestDTO.setAuthenticationInfo(cloudResponseDTO.getAuthenticationInfo());
		
		return cloudRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static Cloud convertCloudResponseDTOToCloud(final CloudResponseDTO cloudResponseDTO) {
		Assert.notNull(cloudResponseDTO, "cloudResponseDTO is null");
		Assert.notNull(cloudResponseDTO.getOperator(), "cloudResponseDTO.Operator is null");
		Assert.notNull(cloudResponseDTO.getName(), "cloudResponseDTO.Name is null");
		
		final Cloud cloud = new Cloud();
		
		cloud.setId(cloudResponseDTO.getId());
		cloud.setOperator(cloudResponseDTO.getOperator());
		cloud.setName(cloudResponseDTO.getName());
		cloud.setAuthenticationInfo(cloudResponseDTO.getAuthenticationInfo());
		
		return cloud;
	}

	//-------------------------------------------------------------------------------------------------
	public static CloudRequestDTO convertCloudWithRelaysResponseDTOToCloudRequestDTO(final CloudWithRelaysAndPublicRelaysResponseDTO entity) {
		Assert.notNull(entity, "cloudResponseDTO is null");
		Assert.notNull(entity.getOperator(), "cloudResponseDTO.Operator is null");
		Assert.notNull(entity.getName(), "cloudResponseDTO.Name is null");
		
		return convertCloudResponseDTOToCloudRequestDTO( new CloudResponseDTO(entity.getId(), entity.getOperator(), entity.getName(), entity.getSecure(), entity.getNeighbor(), entity.getOwnCloud(), entity.getAuthenticationInfo(),
				   entity.getCreatedAt(), entity.getUpdatedAt()));
	}

	//-------------------------------------------------------------------------------------------------
	public static SubscriptionResponseDTO convertSubscriptionToSubscriptionResponseDTO(final Subscription subscription) {
		Assert.notNull(subscription, "subscription is null");
		Assert.notNull(subscription.getSubscriberSystem(), "subscription.ConsumerSystem is null");
		Assert.notNull(subscription.getEventType(), "subscription.EventType is null");
		Assert.notNull(subscription.getNotifyUri(), "subscription.NotifyUri is null");
		Assert.notNull(subscription.getCreatedAt(), "subscription.CreatedAt is null");
		Assert.notNull(subscription.getUpdatedAt(), "subscription.UpdatedAt is null");
		
		final String startDate = subscription.getStartDate() == null ? null : Utilities.convertZonedDateTimeToUTCString(subscription.getStartDate());
		final String endDate = subscription.getEndDate() == null ? null : Utilities.convertZonedDateTimeToUTCString(subscription.getEndDate());
		
		final Set<SystemResponseDTO> sources = collectPublishersFromSubscription(subscription.getPublisherConnections());
		
		return new SubscriptionResponseDTO(subscription.getId(), convertEventTypeToEventTypeResponseDTO(subscription.getEventType()),
										   convertSystemToSystemResponseDTO(subscription.getSubscriberSystem()), Utilities.text2Map(subscription.getFilterMetaData()), subscription.getNotifyUri(), 
										   subscription.isMatchMetaData(), startDate, endDate, sources,	Utilities.convertZonedDateTimeToUTCString(subscription.getCreatedAt()), 
										   Utilities.convertZonedDateTimeToUTCString(subscription.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SubscriptionResponseDTO convertSubscriptionToOnlyAuthorizedSourcesSubscriptionResponseDTO(final Subscription subscription) {
		Assert.notNull(subscription, "subscription is null");
		Assert.notNull(subscription.getSubscriberSystem(), "subscription.ConsumerSystem is null");
		Assert.notNull(subscription.getEventType(), "subscription.EventType is null");
		Assert.notNull(subscription.getNotifyUri(), "subscription.NotifyUri is null");
		Assert.notNull(subscription.getCreatedAt(), "subscription.CreatedAt is null");
		Assert.notNull(subscription.getUpdatedAt(), "subscription.UpdatedAt is null");
		
		final String startDate = subscription.getStartDate() == null ? null : Utilities.convertZonedDateTimeToUTCString(subscription.getStartDate());
		final String endDate = subscription.getEndDate() == null ? null : Utilities.convertZonedDateTimeToUTCString(subscription.getEndDate());
		
		final Set<SystemResponseDTO> sources = collectAuthorizedPublishersFromSubscription(subscription.getPublisherConnections());
		
		return new SubscriptionResponseDTO(subscription.getId(), convertEventTypeToEventTypeResponseDTO(subscription.getEventType()),
										   convertSystemToSystemResponseDTO(subscription.getSubscriberSystem()), Utilities.text2Map(subscription.getFilterMetaData()), subscription.getNotifyUri(), 
										   subscription.isMatchMetaData(), startDate, endDate, sources, Utilities.convertZonedDateTimeToUTCString(subscription.getCreatedAt()), 
										   Utilities.convertZonedDateTimeToUTCString(subscription.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static EventTypeResponseDTO convertEventTypeToEventTypeResponseDTO(final EventType eventType) {
		Assert.notNull(eventType, "eventType is null");
		Assert.notNull(eventType.getEventTypeName(), "eventType.EvenTypeName is null");

		return new EventTypeResponseDTO(eventType.getId(), eventType.getEventTypeName(), Utilities.convertZonedDateTimeToUTCString(eventType.getCreatedAt()), 
									    Utilities.convertZonedDateTimeToUTCString(eventType.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static EventDTO convertEventPublishRequestDTOToEventDTO(final EventPublishRequestDTO eventPublishRequestDTO) {
		Assert.notNull(eventPublishRequestDTO, "eventPublishRequestDTO is null");
		Assert.notNull(eventPublishRequestDTO.getEventType(), "eventPublishRequestDTO.EvenType is null");
		Assert.notNull(eventPublishRequestDTO.getPayload(), "eventPublishRequestDTO.Payload is null");
		Assert.notNull(eventPublishRequestDTO.getTimeStamp(), "eventPublishRequestDTO.TimeStamp is null");
		
		return new EventDTO(eventPublishRequestDTO.getEventType(), eventPublishRequestDTO.getMetaData(), eventPublishRequestDTO.getPayload(), eventPublishRequestDTO.getTimeStamp());
	}

	//-------------------------------------------------------------------------------------------------
	public static SubscriptionListResponseDTO convertSubscriptionPageToSubscriptionListResponseDTO(final Page<Subscription> entries) {
		Assert.notNull(entries, "SubscriptionPage is null" );
		
		final List<SubscriptionResponseDTO> subscriptionEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final Subscription entry : entries) {
			subscriptionEntries.add(convertSubscriptionToSubscriptionResponseDTO(entry));
		}
		
		return new SubscriptionListResponseDTO(subscriptionEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static List<RelayRequestDTO> convertRelayResponseDTOCollectionToRelayRequestDTOList(final Collection<RelayResponseDTO> responses) {
		Assert.notNull(responses, "Collection<RelayResponseDTO> is null.");
		
		final List<RelayRequestDTO> relayRequests = new ArrayList<>();
		for (final RelayResponseDTO relayResponseDTO : responses) {
			relayRequests.add(convertRelayResponseDTOToRelayRequestDTO(relayResponseDTO));
		}
		return relayRequests;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static RelayRequestDTO convertRelayResponseDTOToRelayRequestDTO(final RelayResponseDTO response) {
		Assert.notNull(response, "Relay response is null.");
		
		return new RelayRequestDTO(response.getAddress(), response.getPort(), response.getAuthenticationInfo(), response.isSecure(), response.isExclusive(), response.getType().name());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static Relay convertRelayResponseDTOToRelay(final RelayResponseDTO dto) {
		Assert.notNull(dto, "RelayResponseDTO is null.");
		Assert.isTrue(!Utilities.isEmpty(dto.getAddress()), "RelayResponseDTO.address is null or empty.");
		Assert.notNull(dto.getType(), "RelayResponseDTO.type is null.");
		
		final Relay relay = new Relay();
		relay.setId(dto.getId());
		relay.setAddress(dto.getAddress());
		relay.setPort(dto.getPort());
		relay.setSecure(dto.isSecure());
		relay.setExclusive(dto.isExclusive());
		relay.setType(dto.getType());
		
		return relay;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertForeignSystemToSystemResponseDTO(final ForeignSystem foreignSystem) {
		Assert.notNull(foreignSystem, "ForeignSystem is null");
		
		return new SystemResponseDTO(foreignSystem.getId(), foreignSystem.getSystemName(), foreignSystem.getAddress(), foreignSystem.getPort(), foreignSystem.getAuthenticationInfo(), Utilities.text2Map(foreignSystem.getMetadata()),
									 Utilities.convertZonedDateTimeToUTCString(foreignSystem.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(foreignSystem.getUpdatedAt()));		
	}

	//-------------------------------------------------------------------------------------------------
	public static QoSIntraPingMeasurementResponseDTO convertQoSIntraPingMeasurementToPingMeasurementResponseDTO(final QoSIntraPingMeasurement pingMeasurement) {
		Assert.notNull(pingMeasurement, "pingMeasurement is null");

		final QoSIntraMeasurementResponseDTO measurementResponseDTO = convertQoSIntraMeasurementToQoSIntraMeasurementResponseDTO(pingMeasurement.getMeasurement());

		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = new QoSIntraPingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(pingMeasurement.getId());
		pingMeasurementResponseDTO.setMeasurement(measurementResponseDTO);
		pingMeasurementResponseDTO.setAvailable(pingMeasurement.isAvailable());
		pingMeasurementResponseDTO.setLastAccessAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getLastAccessAt()));
		pingMeasurementResponseDTO.setMinResponseTime(pingMeasurement.getMinResponseTime());
		pingMeasurementResponseDTO.setMaxResponseTime(pingMeasurement.getMaxResponseTime());
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(pingMeasurement.getMeanResponseTimeWithTimeout());
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(pingMeasurement.getMeanResponseTimeWithoutTimeout());
		pingMeasurementResponseDTO.setJitterWithTimeout(pingMeasurement.getJitterWithTimeout());
		pingMeasurementResponseDTO.setJitterWithoutTimeout(pingMeasurement.getJitterWithoutTimeout());
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(pingMeasurement.getLostPerMeasurementPercent());
		pingMeasurementResponseDTO.setSent(pingMeasurement.getSent());
		pingMeasurementResponseDTO.setReceived(pingMeasurement.getReceived());
		pingMeasurementResponseDTO.setCountStartedAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getCountStartedAt()));
		pingMeasurementResponseDTO.setSentAll(pingMeasurement.getSentAll());
		pingMeasurementResponseDTO.setReceivedAll(pingMeasurement.getReceivedAll());
		pingMeasurementResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getCreatedAt()));
		pingMeasurementResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getUpdatedAt()));

		return pingMeasurementResponseDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSInterDirectPingMeasurementResponseDTO convertQoSInterDirectPingMeasurementToPingMeasurementResponseDTO(final QoSInterDirectPingMeasurement pingMeasurement) {
		Assert.notNull(pingMeasurement, "pingMeasurement is null");

		final QoSInterDirectMeasurementResponseDTO measurementResponseDTO = convertQoSInterDirectMeasurementToQoSInterDirectMeasurementResponseDTO(pingMeasurement.getMeasurement());

		final QoSInterDirectPingMeasurementResponseDTO pingMeasurementResponseDTO = new QoSInterDirectPingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(pingMeasurement.getId());
		pingMeasurementResponseDTO.setMeasurement(measurementResponseDTO);
		pingMeasurementResponseDTO.setAvailable(pingMeasurement.isAvailable());
		pingMeasurementResponseDTO.setLastAccessAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getLastAccessAt()));
		pingMeasurementResponseDTO.setMinResponseTime(pingMeasurement.getMinResponseTime());
		pingMeasurementResponseDTO.setMaxResponseTime(pingMeasurement.getMaxResponseTime());
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(pingMeasurement.getMeanResponseTimeWithTimeout());
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(pingMeasurement.getMeanResponseTimeWithoutTimeout());
		pingMeasurementResponseDTO.setJitterWithTimeout(pingMeasurement.getJitterWithTimeout());
		pingMeasurementResponseDTO.setJitterWithoutTimeout(pingMeasurement.getJitterWithoutTimeout());
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(pingMeasurement.getLostPerMeasurementPercent());
		pingMeasurementResponseDTO.setSent(pingMeasurement.getSent());
		pingMeasurementResponseDTO.setReceived(pingMeasurement.getReceived());
		pingMeasurementResponseDTO.setCountStartedAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getCountStartedAt()));
		pingMeasurementResponseDTO.setSentAll(pingMeasurement.getSentAll());
		pingMeasurementResponseDTO.setReceivedAll(pingMeasurement.getReceivedAll());
		pingMeasurementResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getCreatedAt()));
		pingMeasurementResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(pingMeasurement.getUpdatedAt()));

		return pingMeasurementResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public static QoSIntraMeasurementResponseDTO convertQoSIntraMeasurementToQoSIntraMeasurementResponseDTO(final QoSIntraMeasurement measurement) {
		Assert.notNull(measurement, "measurement is null");

		final SystemResponseDTO system = convertSystemToSystemResponseDTO(measurement.getSystem());

		return new QoSIntraMeasurementResponseDTO(
				measurement.getId(), 
				system, 
				measurement.getMeasurementType(), 
				Utilities.convertZonedDateTimeToUTCString(measurement.getLastMeasurementAt()), 
				Utilities.convertZonedDateTimeToUTCString(measurement.getCreatedAt()), 
				Utilities.convertZonedDateTimeToUTCString(measurement.getUpdatedAt()));
	}

	//-------------------------------------------------------------------------------------------------
	public static QoSInterDirectMeasurementResponseDTO convertQoSInterDirectMeasurementToQoSInterDirectMeasurementResponseDTO(final QoSInterDirectMeasurement measurement) {
		Assert.notNull(measurement, "measurement is null");

		return new QoSInterDirectMeasurementResponseDTO(measurement.getId(),
														convertCloudToCloudResponseDTO(measurement.getCloud()),
														measurement.getAddress(),
														measurement.getMeasurementType(),
														Utilities.convertZonedDateTimeToUTCString(measurement.getLastMeasurementAt()),
														Utilities.convertZonedDateTimeToUTCString(measurement.getCreatedAt()),
														Utilities.convertZonedDateTimeToUTCString(measurement.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSIntraPingMeasurementListResponseDTO convertQoSIntraPingMeasurementPageToPingMeasurementListResponseDTO( final Page<QoSIntraPingMeasurement> entries) {
		Assert.notNull(entries, "pingMeasurementPage is null");

		final List<QoSIntraPingMeasurementResponseDTO> pingMeasurementEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final QoSIntraPingMeasurement entry : entries) {
			pingMeasurementEntries.add(convertQoSIntraPingMeasurementToPingMeasurementResponseDTO(entry));
		}

		return new QoSIntraPingMeasurementListResponseDTO(pingMeasurementEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSInterDirectPingMeasurementListResponseDTO convertQoSInterDirectPingMeasurementPageToPingMeasurementListResponseDTO( final Page<QoSInterDirectPingMeasurement> entries) {
		Assert.notNull(entries, "pingMeasurementPage is null");

		final List<QoSInterDirectPingMeasurementResponseDTO> pingMeasurementEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final QoSInterDirectPingMeasurement entry : entries) {
			pingMeasurementEntries.add(convertQoSInterDirectPingMeasurementToPingMeasurementResponseDTO(entry));
		}

		return new QoSInterDirectPingMeasurementListResponseDTO(pingMeasurementEntries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSInterRelayMeasurementResponseDTO convertQoSInterRelayMeasurementToQoSInterRelayMeasurementResponseDTO(final QoSInterRelayMeasurement entry) {
		Assert.notNull(entry, "QoSInterRelayMeasurement is null");
		
		return new QoSInterRelayMeasurementResponseDTO(entry.getId(), convertCloudToCloudResponseDTO(entry.getCloud()), convertRelayToRelayResponseDTO(entry.getRelay()),
													   entry.getMeasurementType(), entry.getStatus(), Utilities.convertZonedDateTimeToUTCString(entry.getLastMeasurementAt()),
													   Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSInterRelayEchoMeasurementResponseDTO convertQoSInterRelayEchoMeasurementToQoSInterRelayEchoMeasurementResponseDTO(final QoSInterRelayEchoMeasurement entry) {
		Assert.notNull(entry, "QoSInterRelayEchoMeasurement is null");
		
		return new QoSInterRelayEchoMeasurementResponseDTO(entry.getId(),
														   convertQoSInterRelayMeasurementToQoSInterRelayMeasurementResponseDTO(entry.getMeasurement()),
														   Utilities.convertZonedDateTimeToUTCString(entry.getLastAccessAt()),
														   entry.getMinResponseTime(),
														   entry.getMaxResponseTime(),
														   entry.getMeanResponseTimeWithTimeout(),
														   entry.getMeanResponseTimeWithoutTimeout(),
														   entry.getJitterWithTimeout(),
														   entry.getJitterWithoutTimeout(),
														   entry.getLostPerMeasurementPercent(),
														   entry.getSent(),
														   entry.getReceived(),
														   Utilities.convertZonedDateTimeToUTCString(entry.getCountStartedAt()),
														   entry.getSentAll(),
														   entry.getReceivedAll(),
														   Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()),
														   Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSInterRelayEchoMeasurementListResponseDTO convertQoSInterRelayEchoMeasurementPageToQoSInterRelayEchoMeasurementListResponseDTO(final Page<QoSInterRelayEchoMeasurement> entries) {
		Assert.notNull(entries, "Page<QoSInterRelayEchoMeasurement> is null");
		
		final List<QoSInterRelayEchoMeasurementResponseDTO> data = new ArrayList<>(entries.getSize());
		for (final QoSInterRelayEchoMeasurement entry : entries) {
			data.add(convertQoSInterRelayEchoMeasurementToQoSInterRelayEchoMeasurementResponseDTO(entry));
		}
		
		return new QoSInterRelayEchoMeasurementListResponseDTO(data, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSReservationResponseDTO convertQoSReservationToQoSReservationResponseDTO(final QoSReservation entry) {
		Assert.notNull(entry, "QoSReservation is null");
		
		return new QoSReservationResponseDTO(entry.getId(), entry.getReservedProviderId(), entry.getReservedServiceId(), entry.getConsumerSystemName(), entry.getConsumerAddress(),
											 entry.getConsumerPort(), Utilities.convertZonedDateTimeToUTCString(entry.getReservedTo()), entry.isTemporaryLock(),
											 Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static QoSReservationListResponseDTO convertQoSReservationListToQoSReservationListResponseDTO(final List<QoSReservation> entries) {
		Assert.notNull(entries, "QoSReservation list is null");
		
		final List<QoSReservationResponseDTO> data = new ArrayList<>();
		for (final QoSReservation entry : entries) {
			data.add(convertQoSReservationToQoSReservationResponseDTO(entry));
		}
		
		return new QoSReservationListResponseDTO(data, data.size());
	}
	
	//-------------------------------------------------------------------------------------------------
    public static DeviceResponseDTO convertDeviceToDeviceResponseDTO(final Device device) {
        Assert.notNull(device, "Device is null");
        return new DeviceResponseDTO(device.getId(), device.getDeviceName(), device.getAddress(), device.getMacAddress(), device.getAuthenticationInfo(),
                                     Utilities.convertZonedDateTimeToUTCString(device.getCreatedAt()),
                                     Utilities.convertZonedDateTimeToUTCString(device.getUpdatedAt()));
    }

    //-------------------------------------------------------------------------------------------------
    public static DeviceListResponseDTO convertDeviceEntryListToDeviceListResponseDTO(final Page<Device> deviceEntryList) {
        Assert.notNull(deviceEntryList, "deviceEntryList is null");

        final long count = deviceEntryList.getTotalElements();
        final DeviceListResponseDTO deviceListResponseDTO = new DeviceListResponseDTO();
        deviceListResponseDTO.setCount(count);
        deviceListResponseDTO.setData(deviceEntryListToDeviceResponseDTOList(deviceEntryList.getContent()));

        return deviceListResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    public static SystemQueryResultDTO convertListOfSystemRegistryEntriesToSystemQueryResultDTO(final List<SystemRegistry> entries, final int unfilteredHits) {
        final List<SystemRegistryResponseDTO> results = new ArrayList<>();

        if (entries != null) {
            Assert.isTrue(unfilteredHits >= entries.size(), "Invalid value of unfiltered hits: " + unfilteredHits);
            for (final SystemRegistry srEntry : entries) {
                results.add(convertSystemRegistryToSystemRegistryResponseDTO(srEntry));
            }
        }

        return new SystemQueryResultDTO(results, unfilteredHits);
    }

    //-------------------------------------------------------------------------------------------------
    public static DeviceQueryResultDTO convertListOfDeviceRegistryEntriesToDeviceQueryResultDTO(final List<DeviceRegistry> entries, final int unfilteredHits) {
        final List<DeviceRegistryResponseDTO> results = new ArrayList<>();

        if (entries != null) {
            Assert.isTrue(unfilteredHits >= entries.size(), "Invalid value of unfiltered hits: " + unfilteredHits);
            for (final DeviceRegistry srEntry : entries) {
                results.add(convertDeviceRegistryToDeviceRegistryResponseDTO(srEntry));
            }
        }

        return new DeviceQueryResultDTO(results, unfilteredHits);
    }

    //-------------------------------------------------------------------------------------------------
    public static ChoreographerStepResponseDTO convertStepToStepResponseDTO(final ChoreographerStep step) {
        Assert.notNull(step, "Step is null.");

        return new ChoreographerStepResponseDTO(step.getId(),
                                                step.getName(),
                                                step.getServiceDefinition(),
                                                step.getMinVersion(),
                                                step.getMaxVersion(),
                                                Utilities.text2Map(step.getStaticParameters()),
                                                step.getQuantity(),
                                                Utilities.fromJson(step.getSrTemplate(), ChoreographerServiceQueryFormDTO.class),
                                                collectNextStepNamesFromStep(step.getNextStepConnections()),
                                                Utilities.convertZonedDateTimeToUTCString(step.getCreatedAt()),
                                                Utilities.convertZonedDateTimeToUTCString(step.getUpdatedAt()));
    }

    //-------------------------------------------------------------------------------------------------
    public static ChoreographerActionResponseDTO convertActionToActionResponseDTO(final Entry<ChoreographerAction, List<ChoreographerStep>> actionDetails) {
        Assert.notNull(actionDetails, "Action details entry is null.");
        
        final ChoreographerAction action = actionDetails.getKey();
        final List<ChoreographerStep> steps = actionDetails.getValue();

        return new ChoreographerActionResponseDTO(action.getId(),
                                                  action.getName(),
                                                  action.isFirstAction(),
                                                  action.getNextAction() != null ? action.getNextAction().getName() : null,
                                                  collectStepsFromAction(steps),
                                                  collectFirstStepNamesFromAction(steps),
                                                  Utilities.convertZonedDateTimeToUTCString(action.getCreatedAt()),
                                                  Utilities.convertZonedDateTimeToUTCString(action.getUpdatedAt()));
    }

    //-------------------------------------------------------------------------------------------------
    public static ChoreographerPlanResponseDTO convertPlanToPlanResponseDTO(final ChoreographerPlan planEntry, final Map<ChoreographerAction,List<ChoreographerStep>> planDetails) {
        Assert.notNull(planEntry, "Plan entry is null.");

        return new ChoreographerPlanResponseDTO(planEntry.getId(),
                                                planEntry.getName(),
                                                planEntry.getFirstAction().getName(),
                                                collectActionsFromPlan(planDetails),
                                                Utilities.convertZonedDateTimeToUTCString(planEntry.getCreatedAt()),
                                                Utilities.convertZonedDateTimeToUTCString(planEntry.getUpdatedAt()));
    }
    
    //-------------------------------------------------------------------------------------------------
    public static ChoreographerSessionResponseDTO convertSessionToSessionResponseDTO(final ChoreographerSession entry) {
    	 Assert.notNull(entry, "session entry is null.");
    	 
    	 return new ChoreographerSessionResponseDTO(entry.getId(),
    			 									entry.getPlan().getId(),
    			 									entry.getPlan().getName(),
    			 									entry.getStatus(),
    			 									entry.getQuantityDone(),
    			 									entry.getQuantityGoal(),
    			 									entry.getExecutionNumber(),
    			 									entry.getNotifyUri(),
                                                    Utilities.convertZonedDateTimeToUTCString(entry.getStartedAt()),
                                                    Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
    }

    //-------------------------------------------------------------------------------------------------
    public static ChoreographerSessionListResponseDTO convertSessionListToSessionListResponseDTO(final Iterable<ChoreographerSession> entries, final long count) {
    	Assert.notNull(entries, "session entry list is null.");
    	
    	final List<ChoreographerSessionResponseDTO> data = new ArrayList<>();
    	for (final ChoreographerSession entry : entries) {
    		data.add(convertSessionToSessionResponseDTO(entry));
		}
    	
    	return new ChoreographerSessionListResponseDTO(data, count);
    }
    
    //-------------------------------------------------------------------------------------------------
    public static ChoreographerSessionStepResponseDTO convertSessionStepToSessionStepResponseDTO(final ChoreographerSessionStep entry) {
    	Assert.notNull(entry, "session step entry is null.");
    	
    	return new ChoreographerSessionStepResponseDTO(entry.getId(),
    												   convertSessionToSessionResponseDTO(entry.getSession()),
    												   convertStepToStepResponseDTO(entry.getStep()),
    												   convertExecutorToExecutorResponseDTO(entry.getExecutor(), new ArrayList<>()),
    												   entry.getStatus(),
    												   entry.getMessage(),
    												   Utilities.convertZonedDateTimeToUTCString(entry.getStartedAt()),
    												   Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));
    }
    
    //-------------------------------------------------------------------------------------------------
    public static ChoreographerSessionStepListResponseDTO convertSessionStepListToSessionStepListResponseDTO(final Iterable<ChoreographerSessionStep> entries, final long count) {
    	Assert.notNull(entries, "session step entry list is null.");
    	
    	final List<ChoreographerSessionStepResponseDTO> data = new ArrayList<>();
    	for (final ChoreographerSessionStep entry : entries) {
			data.add(convertSessionStepToSessionStepResponseDTO(entry));
		}
    	return new ChoreographerSessionStepListResponseDTO(data, count);
    }
    
    //-------------------------------------------------------------------------------------------------
    public static ChoreographerWorklogResponseDTO convertWorklogToWorklogResponseDTO(final ChoreographerWorklog entry) {
    	Assert.notNull(entry, "worklog entry is null.");
    	
    	return new ChoreographerWorklogResponseDTO(entry.getId(),
    											   Utilities.convertZonedDateTimeToUTCString(entry.getEntryDate()),
    											   entry.getPlanName(),
    											   entry.getActionName(),
    											   entry.getStepName(),
    											   entry.getSessionId(),
    											   entry.getExecutionNumber(),
    											   entry.getMessage(),
    											   entry.getMessage());
    }
    
    //-------------------------------------------------------------------------------------------------
    public static ChoreographerWorklogListResponseDTO convertWorklogListToWorklogListResponseDTO(final Iterable<ChoreographerWorklog> entries, final long count) {
    	Assert.notNull(entries, "worklog entry list is null.");
    	
    	final List<ChoreographerWorklogResponseDTO> data = new ArrayList<>();
    	for (final ChoreographerWorklog entry : entries) {
			data.add(convertWorklogToWorklogResponseDTO(entry));
		}
    	return new ChoreographerWorklogListResponseDTO(data, count);
    }
    
    //-------------------------------------------------------------------------------------------------
	public static ChoreographerExecutorResponseDTO convertExecutorToExecutorResponseDTO(final ChoreographerExecutor executor, final List<ChoreographerExecutorServiceDefinition> serviceDefinitions) {
		Assert.notNull(executor, "Executor is null.");
		Assert.notNull(serviceDefinitions, "serviceDefinitions is null.");
		
		final ChoreographerExecutorResponseDTO dto = new ChoreographerExecutorResponseDTO(executor.getId(),
																						  executor.getName(),
																						  executor.getAddress(),
																						  executor.getPort(),
																						  executor.getBaseUri(),
																						  new ArrayList<>(),
																						  Utilities.convertZonedDateTimeToUTCString(executor.getCreatedAt()),
																						  Utilities.convertZonedDateTimeToUTCString(executor.getUpdatedAt()));
		
		for (final ChoreographerExecutorServiceDefinition serviceDef : serviceDefinitions) {
			dto.getServiceDefinitions().add(convertExecutorServiceDefinitionToExecturServiceDefinitionResponseDTO(serviceDef));
		}
		
		return dto;
	}

	//-------------------------------------------------------------------------------------------------
	public static ChoreographerExecutorServiceDefinitionResponseDTO convertExecutorServiceDefinitionToExecturServiceDefinitionResponseDTO(final ChoreographerExecutorServiceDefinition executorServiceDefinition) {
		Assert.notNull(executorServiceDefinition, "ChoreographerExecutorServiceDefinition entry is null.");

		return new ChoreographerExecutorServiceDefinitionResponseDTO(executorServiceDefinition.getId(),
																	 executorServiceDefinition.getExecutor().getId(),
																	 executorServiceDefinition.getServiceDefinition(),
																	 executorServiceDefinition.getMinVersion(),
																	 executorServiceDefinition.getMaxVersion(),
																	 Utilities.convertZonedDateTimeToUTCString(executorServiceDefinition.getCreatedAt()),
																	 Utilities.convertZonedDateTimeToUTCString(executorServiceDefinition.getUpdatedAt()));
	}
	
    //-------------------------------------------------------------------------------------------------
    public static SystemRegistryResponseDTO convertSystemRegistryToSystemRegistryResponseDTO(final SystemRegistry entry) {

        Assert.notNull(entry, "SR entry is null.");
        Assert.notNull(entry.getDevice(), "Related device is null.");
        Assert.notNull(entry.getSystem(), "Related system is null.");

        final SystemResponseDTO systemResponseDTO = convertSystemToSystemResponseDTO(entry.getSystem());
        final DeviceResponseDTO deviceResponseDTO = convertDeviceToDeviceResponseDTO(entry.getDevice());

        final SystemRegistryResponseDTO dto = new SystemRegistryResponseDTO();
        dto.setId(entry.getId());
        dto.setProvider(deviceResponseDTO);
        dto.setSystem(systemResponseDTO);
        dto.setEndOfValidity(Utilities.convertZonedDateTimeToUTCString(entry.getEndOfValidity()));
        dto.setMetadata(Utilities.text2Map(entry.getMetadata()));
        dto.setVersion(entry.getVersion());
        dto.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()));
        dto.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));

        return dto;
    }

    //-------------------------------------------------------------------------------------------------
    public static SystemRegistryListResponseDTO convertSystemRegistryListToSystemRegistryListResponseDTO(final Iterable<SystemRegistry> page) {
        Assert.notNull(page, "Iterable must not be null");
        final List<SystemRegistryResponseDTO> list = new ArrayList<>();
        for (final SystemRegistry entry : page) {
            list.add(convertSystemRegistryToSystemRegistryResponseDTO(entry));
        }
        return new SystemRegistryListResponseDTO(list, list.size());
    }

    //-------------------------------------------------------------------------------------------------
    public static SystemQueryResultDTO convertSystemRegistryListToSystemQueryResultDTO(final Iterable<SystemRegistry> page) {
        Assert.notNull(page, "Iterable must not be null");
        final List<SystemRegistryResponseDTO> list = new ArrayList<>();
        for (final SystemRegistry entry : page) {
            list.add(convertSystemRegistryToSystemRegistryResponseDTO(entry));
        }
        return new SystemQueryResultDTO(list, list.size());
    }

    //-------------------------------------------------------------------------------------------------
    public static DeviceRegistryResponseDTO convertDeviceRegistryToDeviceRegistryResponseDTO(final DeviceRegistry entry) {

        Assert.notNull(entry, "DR entry is null.");
        Assert.notNull(entry.getDevice(), "Related device is null.");

        final DeviceResponseDTO deviceResponseDTO = convertDeviceToDeviceResponseDTO(entry.getDevice());

        final DeviceRegistryResponseDTO dto = new DeviceRegistryResponseDTO();
        dto.setId(entry.getId());
        dto.setDevice(deviceResponseDTO);
        dto.setEndOfValidity(Utilities.convertZonedDateTimeToUTCString(entry.getEndOfValidity()));
        dto.setMetadata(Utilities.text2Map(entry.getMetadata()));
        dto.setVersion(entry.getVersion());
        dto.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(entry.getCreatedAt()));
        dto.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(entry.getUpdatedAt()));

        return dto;
    }

    //-------------------------------------------------------------------------------------------------
    public static DeviceRegistryListResponseDTO convertDeviceRegistryListToDeviceRegistryListResponseDTO(final Iterable<DeviceRegistry> page) {
        Assert.notNull(page, "Iterable must not be null");
        final List<DeviceRegistryResponseDTO> list = new ArrayList<>();
        for (final DeviceRegistry entry : page) {
            list.add(convertDeviceRegistryToDeviceRegistryResponseDTO(entry));
        }
        return new DeviceRegistryListResponseDTO(list, list.size());
    }

    //-------------------------------------------------------------------------------------------------
    public static DeviceQueryResultDTO convertDeviceRegistryListToDeviceQueryResultDTO(final Iterable<DeviceRegistry> page) {
        Assert.notNull(page, "Iterable must not be null");
        final List<DeviceRegistryResponseDTO> list = new ArrayList<>();
        for (final DeviceRegistry entry : page) {
            list.add(convertDeviceRegistryToDeviceRegistryResponseDTO(entry));
        }
        return new DeviceQueryResultDTO(list, list.size());
    }

    //-------------------------------------------------------------------------------------------------
    public static List<ChoreographerStepResponseDTO> collectStepsFromAction(final List<ChoreographerStep> steps) {
        Assert.notNull(steps, "Steps list is null.");

        final List<ChoreographerStepResponseDTO> result = new ArrayList<>(steps.size());
        for (final ChoreographerStep step : steps) {
            result.add(convertStepToStepResponseDTO(step));
        }

        result.sort(Comparator.comparing(ChoreographerStepResponseDTO::getId));
        return result;
    }

    //-------------------------------------------------------------------------------------------------
    private static List<String> collectFirstStepNamesFromAction(final List<ChoreographerStep> steps) {
        Assert.notNull(steps, "Steps list is null.");

        final List<String> result = new ArrayList<>();
        for (final ChoreographerStep step : steps) {
        	if (step.isFirstStep()) {
        		result.add(step.getName());
        	}
        }

        return result;
    }

    //-------------------------------------------------------------------------------------------------
    public static List<ChoreographerActionResponseDTO> collectActionsFromPlan(final Map<ChoreographerAction,List<ChoreographerStep>> planDetails) {
        Assert.notNull(planDetails, "Plan details is null.");
        final List<ChoreographerActionResponseDTO> result = new ArrayList<>(planDetails.size());
        for (final Entry<ChoreographerAction,List<ChoreographerStep>> actionDetails : planDetails.entrySet()) {
            result.add(convertActionToActionResponseDTO(actionDetails));
        }

        result.sort(Comparator.comparing(ChoreographerActionResponseDTO::getId));
        return result;
    }

	public static ChoreographerSuitableExecutorResponseDTO convertSuitableExecutorIdsToSuitableExecutorResponseDTO (final List<Long> executorIds) {
		final ChoreographerSuitableExecutorResponseDTO dto = new ChoreographerSuitableExecutorResponseDTO();
		for (final long id : executorIds) {
			dto.getSuitableExecutorIds().add(id);
		}
		return dto;
	}

	// -------------------------------------------------------------------------------------------------
	public static IssuedCertificatesResponseDTO convertCaCertificateListToIssuedCertificatesResponseDTO(
			final Page<CaCertificate> certificateEntryList) {
		Assert.notNull(certificateEntryList, "certificateEntryList is null");

		final long count = certificateEntryList.getTotalElements();
		final IssuedCertificatesResponseDTO certificatesResponseDTO = new IssuedCertificatesResponseDTO();
		certificatesResponseDTO.setCount(count);
		certificatesResponseDTO.setIssuedCertificates(
				certificateEntryListToCertificatesResponseDTOList(certificateEntryList.getContent()));

		return certificatesResponseDTO;
	}
	
	// -------------------------------------------------------------------------------------------------
	public static TrustedKeysResponseDTO convertCaTrustedKeyListToTrustedKeysResponseDTO(
			final Page<CaTrustedKey> trustedKeyEntryList) {
		Assert.notNull(trustedKeyEntryList, "trustedKeyEntryList is null");

		final long count = trustedKeyEntryList.getTotalElements();
		final TrustedKeysResponseDTO trustedKeysResponseDTO = new TrustedKeysResponseDTO();
		trustedKeysResponseDTO.setCount(count);
		trustedKeysResponseDTO
				.setTrustedKeys(trustedKeyEntryListToTrustedKeysResponseDTOList(trustedKeyEntryList.getContent()));

		return trustedKeysResponseDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static LogEntryDTO convertLogsToLogEntryDTO(final Logs log) {
		Assert.notNull(log, "log is null");
		
		return new LogEntryDTO(log.getLogId(),
							   Utilities.convertZonedDateTimeToUTCString(log.getEntryDate()),
							   log.getLogger(),
							   log.getLogLevel() != null ? log.getLogLevel().name() : "null",
							   log.getSystem() != null ? log.getSystem().name() : "null",
							   log.getMessage(),
							   log.getException());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static LogEntryListResponseDTO convertLogsPageToLogEntryListResponseDTO(final Page<Logs> logs) {
		Assert.notNull(logs, "logs page is null");
		
		final List<LogEntryDTO> data = new ArrayList<>(logs.getSize());
		for (final Logs log : logs.getContent()) {
			data.add(convertLogsToLogEntryDTO(log));
		}
		
		return new LogEntryListResponseDTO(data, logs.getTotalElements());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private DTOConverter() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static List<SystemResponseDTO> systemEntryListToSystemResponseDTOList(final List<System> systemList) {
		final List<SystemResponseDTO> systemResponseDTOs = new ArrayList<>(systemList.size());
		
		for (final System system : systemList) {
			systemResponseDTOs.add(convertSystemToSystemResponseDTO(system));
		}
		
		return systemResponseDTOs;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static List<ServiceInterfaceResponseDTO> collectInterfacesFromServiceRegistry(final Set<ServiceRegistryInterfaceConnection> interfaceConnections) {
		final List<ServiceInterfaceResponseDTO> result = new ArrayList<>(interfaceConnections.size());
		for (final ServiceRegistryInterfaceConnection conn : interfaceConnections) {
			result.add(convertServiceInterfaceToServiceInterfaceResponseDTO(conn.getServiceInterface()));
		}
		
		result.sort((dto1, dto2) -> dto1.getInterfaceName().compareToIgnoreCase(dto2.getInterfaceName()));
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static List<ServiceInterfaceResponseDTO> collectInterfacesFromAuthorizationIntraCloud(final Set<AuthorizationIntraCloudInterfaceConnection> interfaceConnections) {
		final List<ServiceInterfaceResponseDTO> result = new ArrayList<>(interfaceConnections.size());
		for (final AuthorizationIntraCloudInterfaceConnection conn : interfaceConnections) {
			result.add(convertServiceInterfaceToServiceInterfaceResponseDTO(conn.getServiceInterface()));
		}
		
		result.sort((dto1, dto2) -> dto1.getInterfaceName().compareToIgnoreCase(dto2.getInterfaceName()));
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static List<ServiceInterfaceResponseDTO> collectInterfacesFromAuthorizationInterCloud(final Set<AuthorizationInterCloudInterfaceConnection> interfaceConnections) {
		final List<ServiceInterfaceResponseDTO> result = new ArrayList<>(interfaceConnections.size());
		for (final AuthorizationInterCloudInterfaceConnection conn : interfaceConnections) {
			result.add(convertServiceInterfaceToServiceInterfaceResponseDTO(conn.getServiceInterface()));
		}
		
		result.sort((dto1, dto2) -> dto1.getInterfaceName().compareToIgnoreCase(dto2.getInterfaceName()));
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private static Set<SystemResponseDTO> collectPublishersFromSubscription(final Set<SubscriptionPublisherConnection> connections) {
		final Set<SystemResponseDTO> result = new HashSet<>(connections.size());
		for (final SubscriptionPublisherConnection conn : connections) {
			result.add(convertSystemToSystemResponseDTO(conn.getSystem()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Set<SystemResponseDTO> collectAuthorizedPublishersFromSubscription(final Set<SubscriptionPublisherConnection> connections) {
		final Set<SystemResponseDTO> result = new HashSet<>(connections.size());
		for (final SubscriptionPublisherConnection conn : connections) {
			if (conn.isAuthorized()) {
				result.add(convertSystemToSystemResponseDTO(conn.getSystem()));
			}
		}
		
		return result;
	}
    
    //-------------------------------------------------------------------------------------------------
    private static List<DeviceResponseDTO> deviceEntryListToDeviceResponseDTOList(final List<Device> deviceList) {
        final List<DeviceResponseDTO> deviceResponseDTOs = new ArrayList<>(deviceList.size());

        for (final Device device : deviceList) {
            deviceResponseDTOs.add(convertDeviceToDeviceResponseDTO(device));
        }

        return deviceResponseDTOs;
    }

    //-------------------------------------------------------------------------------------------------
    private static List<String> collectNextStepNamesFromStep(final Set<ChoreographerStepNextStepConnection> nextStepConnections) {
        if (nextStepConnections != null) {
        	final List<ChoreographerStepNextStepConnection> connections = new ArrayList<>(nextStepConnections);
        	connections.sort(Comparator.comparing(ChoreographerStepNextStepConnection::getId));
        	
            final List<String> result = new ArrayList<>(connections.size());
            for (final ChoreographerStepNextStepConnection connection : connections) {
                result.add(connection.getTo().getName());
            }

            return result;
        } else {
            return List.of();
        }
    }
	
	// -------------------------------------------------------------------------------------------------
	private static List<TrustedKeyDTO> trustedKeyEntryListToTrustedKeysResponseDTOList(final List<CaTrustedKey> trustedKeyList) {
		final List<TrustedKeyDTO> trustedKeyDTOs = new ArrayList<>(trustedKeyList.size());

		for (final CaTrustedKey trustedKey : trustedKeyList) {
			final TrustedKeyDTO dto = new TrustedKeyDTO(trustedKey.getId(),
					Utilities.convertZonedDateTimeToUTCString(trustedKey.getCreatedAt()),
					trustedKey.getDescription());
			trustedKeyDTOs.add(dto);
		}

		return trustedKeyDTOs;
	}

	// -------------------------------------------------------------------------------------------------
	private static List<IssuedCertificateDTO> certificateEntryListToCertificatesResponseDTOList(
			final List<CaCertificate> certificateList) {
		Assert.notNull(certificateList, "certificateList is null");

		final List<IssuedCertificateDTO> certificateDTOs = new ArrayList<>(certificateList.size());

		final ZonedDateTime now = ZonedDateTime.now();

		for (final CaCertificate certificate : certificateList) {
			final IssuedCertificateDTO dto = new IssuedCertificateDTO();
			dto.setId(certificate.getId());
			dto.setCommonName(certificate.getCommonName());
			dto.setSerialNumber(certificate.getSerial());
			dto.setCreatedBy(certificate.getCreatedBy());
			dto.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(certificate.getCreatedAt()));
			
			final ZonedDateTime revokedAt = certificate.getRevokedAt();
			final ZonedDateTime validAfter = certificate.getValidAfter();
			final ZonedDateTime validBefore = certificate.getValidBefore();
			dto.setRevokedAt(Utilities.convertZonedDateTimeToUTCString(revokedAt));
			dto.setValidFrom(Utilities.convertZonedDateTimeToUTCString(validAfter));
			dto.setValidUntil(Utilities.convertZonedDateTimeToUTCString(validBefore));
			dto.setStatus(getStatus(now, validAfter, validBefore, revokedAt));

			certificateDTOs.add(dto);
		}

		return certificateDTOs;
	}

	//-------------------------------------------------------------------------------------------------
	private static IssuedCertificateStatus getStatus(final ZonedDateTime now, final ZonedDateTime validAfter,
													 final ZonedDateTime validBefore, final ZonedDateTime revokedAt) {
		Assert.notNull(now, "now cannot be null");
		Assert.notNull(validAfter, "validAfter cannot be null");
		Assert.notNull(validBefore, "validBefore cannot be null");
		
		if (revokedAt != null) {
			return IssuedCertificateStatus.REVOKED;
		}
		if (now.isAfter(validBefore) || now.isBefore(validAfter)) {
			return IssuedCertificateStatus.EXPIRED;
		}
		return IssuedCertificateStatus.GOOD;
	}
}