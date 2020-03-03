package eu.arrowhead.common.dto.internal;

import java.util.ArrayList;
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
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerActionActionStepConnection;
import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import eu.arrowhead.common.database.entity.ChoreographerActionPlanActionConnection;
import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.database.entity.ChoreographerActionStepServiceDefinitionConnection;
import eu.arrowhead.common.database.entity.ChoreographerNextActionStep;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.ForeignSystem;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.ChoreographerActionPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerActionResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerActionStepResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNextActionStepResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
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
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class DTOConverter {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertSystemToSystemResponseDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		return new SystemResponseDTO(system.getId(), system.getSystemName(), system.getAddress(), system.getPort(), system.getAuthenticationInfo(),
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
			final long serviceDefinitionId = srEntry.getServiceDefinition().getId();
			final String serviceDefinition = srEntry.getServiceDefinition().getServiceDefinition();		
			
			// Creating ServicesGroupedBySystemsResponseDTO
			if (servicesBySystemId.containsKey(systemId)) {
				servicesBySystemId.get(systemId).getServices().add(convertServiceRegistryToServiceRegistryResponseDTO(srEntry));
			} else {
				final ServicesGroupedBySystemsResponseDTO dto = new ServicesGroupedBySystemsResponseDTO(systemId, systemName, systemAddress, systemPort, new ArrayList<>());
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
		
		return new RelayResponseDTO(entry.getId(), entry.getAddress(), entry.getPort(), entry.getSecure(), entry.getExclusive(), entry.getType(),
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
		
		return new RelayRequestDTO(relay.getAddress(), relay.getPort(), relay.getSecure(), relay.getExclusive(), relay.getType().name());
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
	public static SystemRequestDTO convertSystemToSystemRequestDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setAddress(system.getAddress());
		systemRequestDTO.setSystemName(system.getSystemName());
		systemRequestDTO.setPort(system.getPort());
		systemRequestDTO.setAuthenticationInfo(systemRequestDTO.getAuthenticationInfo());
		
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
	public static RelayRequestDTO convertRelayResponseDTOToRelayRequestDTO(final RelayResponseDTO response) {
		Assert.notNull(response, "Relay response is null.");
		
		return new RelayRequestDTO(response.getAddress(), response.getPort(), response.isSecure(), response.isExclusive(), response.getType().name());
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertForeignSystemToSystemResponseDTO(final ForeignSystem foreignSystem) {
		Assert.notNull(foreignSystem, "ForeignSystem is null");
		
		return new SystemResponseDTO(foreignSystem.getId(), foreignSystem.getSystemName(), foreignSystem.getAddress(), foreignSystem.getPort(), foreignSystem.getAuthenticationInfo(),
									 Utilities.convertZonedDateTimeToUTCString(foreignSystem.getCreatedAt()), Utilities.convertZonedDateTimeToUTCString(foreignSystem.getUpdatedAt()));		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ChoreographerActionStepResponseDTO convertChoreographerActionStepToChoreographerActionStepResponseDTO(final ChoreographerActionStep actionStep) {
	    Assert.notNull(actionStep, "ChoreographerActionStep is null.");

		return new ChoreographerActionStepResponseDTO(actionStep.getId(), actionStep.getName(),
													  collectServiceDefinitionsFromChoreographerActionStep(actionStep.getActionStepServiceDefinitionConnections()),
													  collectChoreographerNextActionStepsFromChoreographerActionStep(actionStep.getNextActionSteps()),
													  Utilities.convertZonedDateTimeToUTCString(actionStep.getCreatedAt()),
													  Utilities.convertZonedDateTimeToUTCString(actionStep.getUpdatedAt()));
	}

	//-------------------------------------------------------------------------------------------------
	public static List<ChoreographerActionStepResponseDTO> collectChoreographerActionStepsFromChoreographerAction(final Set<ChoreographerActionActionStepConnection> actionStepConnections) {
		Assert.notNull(actionStepConnections, "ActionStepConnectionSet is null.");

		final List<ChoreographerActionStepResponseDTO> result = new ArrayList<>(actionStepConnections.size());
		for (final ChoreographerActionActionStepConnection conn : actionStepConnections) {
			result.add(convertChoreographerActionStepToChoreographerActionStepResponseDTO(conn.getActionStepEntry()));
		}

		result.sort(Comparator.comparing(ChoreographerActionStepResponseDTO::getId));
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	public static String collectChoreographerNextActionNameFromChoreographerAction(final ChoreographerAction nextAction) {
		if (nextAction != null) {
			return nextAction.getActionName();
		}
		
		return null;
	}

    //-------------------------------------------------------------------------------------------------
	public static ChoreographerActionResponseDTO convertChoreographerActionToChoreographerActionResponseDTO(final ChoreographerAction actionEntry) {
		Assert.notNull(actionEntry, "ChoreographerAction entry is null.");

		return new ChoreographerActionResponseDTO(actionEntry.getId(), actionEntry.getActionName(),
												  collectChoreographerNextActionNameFromChoreographerAction(actionEntry.getNextAction()),
												  collectChoreographerActionStepsFromChoreographerAction(actionEntry.getActionActionStepConnections()),
												  Utilities.convertZonedDateTimeToUTCString(actionEntry.getCreatedAt()),
												  Utilities.convertZonedDateTimeToUTCString(actionEntry.getUpdatedAt()));
	}

	//-------------------------------------------------------------------------------------------------
	public static List<ChoreographerActionResponseDTO> collectChoreographerActionsFromChoreographerActionPlan(final Set<ChoreographerActionPlanActionConnection> actionConnections) {
		Assert.notNull(actionConnections, "ActionConnectionsSet is null.");

		final List<ChoreographerActionResponseDTO> result = new ArrayList<>(actionConnections.size());
		for (final ChoreographerActionPlanActionConnection conn : actionConnections) {
			result.add(convertChoreographerActionToChoreographerActionResponseDTO(conn.getActionEntry()));
		}

		result.sort(Comparator.comparing(ChoreographerActionResponseDTO::getId));
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	public static ChoreographerActionPlanResponseDTO convertChoreographerActionPlanToChoreographerActionPlanResponseDTO(final ChoreographerActionPlan actionPlanEntry) {
		Assert.notNull(actionPlanEntry, "ChoreographerActionPlan entry is null.");

		return new ChoreographerActionPlanResponseDTO(actionPlanEntry.getId(), actionPlanEntry.getActionPlanName(),
													  collectChoreographerActionsFromChoreographerActionPlan(actionPlanEntry.getActionPlanActionConnections()),
													  Utilities.convertZonedDateTimeToUTCString(actionPlanEntry.getCreatedAt()),
													  Utilities.convertZonedDateTimeToUTCString(actionPlanEntry.getUpdatedAt()));
	}

	//-------------------------------------------------------------------------------------------------
	public static PingMeasurementResponseDTO convertQoSIntraPingMeasurementToPingMeasurementResponseDTO(
		final QoSIntraPingMeasurement pingMeasurement) {
		Assert.notNull(pingMeasurement, "pingMeasurement is null");

		final QoSIntraMeasurementResponseDTO measurementResponseDTO = convertQoSIntraMeasurementToQoSIntraMeasurementResponseDTO(pingMeasurement.getMeasurement());

		final PingMeasurementResponseDTO pingMeasurementResponseDTO = new PingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(pingMeasurement.getId());
		pingMeasurementResponseDTO.setMeasurement(measurementResponseDTO);
		pingMeasurementResponseDTO.setAvailable(pingMeasurement.isAvailable());
		pingMeasurementResponseDTO.setLastAccessAt(pingMeasurement.getLastAccessAt());
		pingMeasurementResponseDTO.setMinResponseTime(pingMeasurement.getMinResponseTime());
		pingMeasurementResponseDTO.setMaxResponseTime(pingMeasurement.getMaxResponseTime());
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(pingMeasurement.getMeanResponseTimeWithTimeout());
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(pingMeasurement.getMeanResponseTimeWithoutTimeout());
		pingMeasurementResponseDTO.setJitterWithTimeout(pingMeasurement.getJitterWithTimeout());
		pingMeasurementResponseDTO.setJitterWithoutTimeout(pingMeasurement.getJitterWithoutTimeout());
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(pingMeasurement.getLostPerMeasurementPercent());
		pingMeasurementResponseDTO.setSent(pingMeasurement.getSent());
		pingMeasurementResponseDTO.setReceived(pingMeasurement.getReceived());
		pingMeasurementResponseDTO.setCountStartedAt(pingMeasurement.getCountStartedAt());
		pingMeasurementResponseDTO.setSentAll(pingMeasurement.getSentAll());
		pingMeasurementResponseDTO.setReceivedAll(pingMeasurement.getReceivedAll());
		pingMeasurementResponseDTO.setCreatedAt(pingMeasurement.getUpdatedAt());
		pingMeasurementResponseDTO.setUpdatedAt(pingMeasurement.getUpdatedAt());

		return pingMeasurementResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public static QoSIntraMeasurementResponseDTO convertQoSIntraMeasurementToQoSIntraMeasurementResponseDTO(
		final QoSIntraMeasurement measurement) {
		Assert.notNull(measurement, "measurement is null");

		final SystemResponseDTO system = convertSystemToSystemResponseDTO(measurement.getSystem());

		return new QoSIntraMeasurementResponseDTO(
				measurement.getId(), 
				system, 
				measurement.getMeasurementType(), 
				measurement.getLastMeasurementAt(), 
				measurement.getCreatedAt(), 
				measurement.getUpdatedAt());
	}

	//-------------------------------------------------------------------------------------------------
	public static PingMeasurementListResponseDTO convertQoSIntraPingMeasurementPageToPingMeasurementListResponseDTO(
		final Page<QoSIntraPingMeasurement> entries) {
		Assert.notNull(entries, "pingMeasurementPage is null");

		final List<PingMeasurementResponseDTO> pingMeasurementEntries = new ArrayList<>(entries.getNumberOfElements());
		for (final QoSIntraPingMeasurement entry : entries) {
			pingMeasurementEntries.add(convertQoSIntraPingMeasurementToPingMeasurementResponseDTO(entry));
		}

		return new PingMeasurementListResponseDTO(pingMeasurementEntries, entries.getTotalElements());
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
	private static List<ServiceDefinitionResponseDTO> collectServiceDefinitionsFromChoreographerActionStep(final Set<ChoreographerActionStepServiceDefinitionConnection> serviceDefinitionConnections) {
		final List<ServiceDefinitionResponseDTO> result = new ArrayList<>(serviceDefinitionConnections.size());
		for (final ChoreographerActionStepServiceDefinitionConnection conn : serviceDefinitionConnections) {
			result.add(convertServiceDefinitionToServiceDefinitionResponseDTO(conn.getServiceDefinitionEntry()));
		}

		result.sort(Comparator.comparing(ServiceDefinitionResponseDTO::getId));
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private static List<ChoreographerNextActionStepResponseDTO> collectChoreographerNextActionStepsFromChoreographerActionStep(final Set<ChoreographerNextActionStep> nextActionSteps) {
		final List<ChoreographerNextActionStepResponseDTO> result = new ArrayList<>(nextActionSteps.size());
		for (final ChoreographerNextActionStep nextActionStep : nextActionSteps) {
			result.add(convertChoreographerNextActionStepToChoreographerNextActionStepResponseDTO(nextActionStep.getNextActionStepEntry()));
		}

		result.sort(Comparator.comparing(ChoreographerNextActionStepResponseDTO::getId));
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private static ChoreographerNextActionStepResponseDTO convertChoreographerNextActionStepToChoreographerNextActionStepResponseDTO(final ChoreographerActionStep nextActionStepEntry) {
		return new ChoreographerNextActionStepResponseDTO(nextActionStepEntry.getId(), nextActionStepEntry.getName());
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

}