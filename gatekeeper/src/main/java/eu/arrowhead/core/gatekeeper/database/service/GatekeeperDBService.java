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

package eu.arrowhead.core.gatekeeper.database.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.repository.CloudGatekeeperRelayRepository;
import eu.arrowhead.common.database.repository.CloudGatewayRelayRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.RelayRepository;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.RelayListResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@Service
public class GatekeeperDBService {

	//=================================================================================================
	// members
	
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0.";
	private static final String INVALID_FORMAT_ERROR_MESSAGE = " has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private RelayRepository relayRepository;
	
	@Autowired
	private CloudGatekeeperRelayRepository cloudGatekeeperRelayRepository;
	
	@Autowired
	private CloudGatewayRelayRepository cloudGatewayRelayRepository;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	private final Logger logger = LogManager.getLogger(GatekeeperDBService.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public CloudWithRelaysAndPublicRelaysListResponseDTO getCloudsWithPublicRelaysResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getCloudsWithPublicRelaysResponse started...");
		
		final Page<Cloud> entries = getClouds(page, size, direction, sortField);
		final List<Relay> publicRelays = getPublicGatewayRelays();
		
		return DTOConverter.convertCloudToCloudWithRelaysAndPublicRelaysListResponseDTO(entries, publicRelays);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public CloudWithRelaysListResponseDTO getCloudsResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getCloudsResponse started...");
		
		final Page<Cloud> entries = getClouds(page, size, direction, sortField);
		
		return DTOConverter.convertCloudToCloudWithRelaysListResponseDTO(entries);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public Page<Cloud> getClouds(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getClouds started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!Cloud.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return cloudRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<Cloud> getNeighborClouds() {
		logger.debug("getNeighborClouds started...");
		
		try {
			return cloudRepository.findByNeighbor(true);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<Cloud> getCloudsByIds(final Iterable<Long> ids) {
		logger.debug("getCloudsByIds started...");
		
		try {
			return cloudRepository.findAllById(ids);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	public CloudWithRelaysResponseDTO getCloudByIdResponse(final long id) {
		logger.debug("getCloudByIdResponse started...");
		
		final Cloud entry = getCloudById(id);
		
		return DTOConverter.convertCloudToCloudWithRelaysResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	public Cloud getCloudById(final long id) {
		logger.debug("getCloudById started...");
		
		try {
			if (id < 1) {
				throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
			}
						
			final Optional<Cloud> cloudOpt = cloudRepository.findById(id);
			if (cloudOpt.isEmpty()) {
				throw new InvalidParameterException("Cloud with id '" + id + "' not exists");
			}
			
			return cloudOpt.get();			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public Cloud getCloudByOperatorAndName(final String operator, final String name) {
		logger.debug("getCloudByOperatorAndName started...");
		
		if (Utilities.isEmpty(operator) || Utilities.isEmpty(name)) {
			throw new InvalidParameterException("operator or name is empty");
		}
		
		final Optional<Cloud> cloudOpt = cloudRepository.findByOperatorAndName(operator.toLowerCase().trim(), name.toLowerCase().trim());
		if (cloudOpt.isEmpty()) {
			throw new InvalidParameterException("Cloud with the following operator and name not exists: " + operator + ", " + name);
		}
		
		return cloudOpt.get();
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public CloudWithRelaysListResponseDTO registerBulkCloudsWithRelaysResponse(final List<CloudRequestDTO> dtoList) {
		logger.debug("registerBulkCloudsWithRelaysResponse started...");
		
		final List<Cloud> entries = registerBulkCloudsWithRelays(dtoList);
		
		return DTOConverter.convertCloudToCloudWithRelaysListResponseDTO(new PageImpl<Cloud>(entries));
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<Cloud> registerBulkCloudsWithRelays(final List<CloudRequestDTO> dtoList) {
		logger.debug("registerBulkCloudsWithRelays started...");
		
		try {
			final Map<String, Cloud> cloudsToSave = new HashMap<>();
			final Map<String, List<Relay>> gatekeeperRelaysForClouds = new HashMap<>();
			final Map<String, List<Relay>> gatewayRelaysForClouds = new HashMap<>();
			
			if (dtoList == null || dtoList.isEmpty()) {
				throw new InvalidParameterException("List of CloudRequestDTO is null or empty");
			}
			
			for (final CloudRequestDTO dto : dtoList) {
				if (dto == null) {
					throw new InvalidParameterException("List of CloudRequestDTO contains null element");
				}				

				validateCloudParameters(true, dto.getOperator(), dto.getName(), dto.getSecure(), dto.getAuthenticationInfo(), dto.getGatekeeperRelayIds(), dto.getGatewayRelayIds());
				
				final String operator = dto.getOperator().toLowerCase().trim();
				final String name = dto.getName().toLowerCase().trim();
				final boolean secure = dto.getSecure() == null ? false : dto.getSecure();
				final boolean neighbor = dto.getNeighbor() == null ? false : dto.getNeighbor();
				
				final String cloudUniqueConstraint = operator + "." + name;
				if (cloudsToSave.containsKey(cloudUniqueConstraint)) {
					throw new InvalidParameterException("List of CloudRequestDTO contains uinque constraint violation: " + dto.getOperator() + " operator with " + dto.getName() + " name");
				}
				cloudsToSave.put(cloudUniqueConstraint, new Cloud(operator, name, secure, neighbor, false, dto.getAuthenticationInfo()));
				
				gatekeeperRelaysForClouds.put(cloudUniqueConstraint, collectAndValidateGatekeeperRelays(dto.getGatekeeperRelayIds()));
				gatewayRelaysForClouds.put(cloudUniqueConstraint, collectAndValidateGatewayRelays(dto.getGatewayRelayIds()));				
			}
			
			final List<Cloud> savedClouds = cloudRepository.saveAll(cloudsToSave.values());
			cloudRepository.flush();
			
			final Set<Long> savedCloudIds = saveCloudAndRelayConnections(savedClouds, gatekeeperRelaysForClouds, gatewayRelaysForClouds);
			
			return cloudRepository.findAllById(savedCloudIds);			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public CloudWithRelaysResponseDTO updateCloudByIdWithRelaysResponse(final long id, final CloudRequestDTO dto) {
		logger.debug("updateCloudByIdWithRelaysResponse started...");
		
		final Cloud entry = updateCloudByIdWithRelays(id, dto);
		
		return DTOConverter.convertCloudToCloudWithRelaysResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	@Transactional(rollbackFor = ArrowheadException.class)
	public Cloud updateCloudByIdWithRelays(final long id, final CloudRequestDTO dto) {
		logger.debug("updateCloudByIdWithRelays started...");
						
		try {
			if (id < 1) {
				throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
			}
						
			if (dto == null) {
				throw new InvalidParameterException("CloudRequestDTO is null");		
			}
			
			final Optional<Cloud> cloudOpt = cloudRepository.findById(id);
			if (cloudOpt.isEmpty()) {
				throw new InvalidParameterException("Cloud with id '" + id + "' not exists"); 
			}
			final Cloud cloud = cloudOpt.get();
			
			validateCloudParameters(false, dto.getOperator(), dto.getName(), dto.getSecure(), dto.getAuthenticationInfo(), dto.getGatekeeperRelayIds(), dto.getGatewayRelayIds());
			
			final String dtoOperator = dto.getOperator().toLowerCase().trim();
			final String dtoName = dto.getName().toLowerCase().trim();
			final boolean dtoSecure = dto.getSecure() == null ? false : dto.getSecure();
			final boolean dtoNeighbor = dto.getNeighbor() == null ? false : dto.getNeighbor();
			
			if (!cloud.getOperator().equalsIgnoreCase(dtoOperator) || !cloud.getName().equalsIgnoreCase(dtoName)) {
				checkUniqueConstraintOfCloudTable(dtoOperator, dtoName);
			}
			
			final List<Relay> validatedGatekeeperRelaysToAdd = deleteUnnecessaryGatekeeperRelayConnectionsAndGetValidatedAdditionalRelays(cloud, dto.getGatekeeperRelayIds());
			final List<Relay> validatedGatewayRelaysToAdd = deleteUnnecessaryGatewayRelayConnectionsAndGetValidatedAdditionalRelays(cloud, dto.getGatewayRelayIds());
						
			saveCloudAndRelayConnections(cloud, validatedGatekeeperRelaysToAdd, validatedGatewayRelaysToAdd);			
			cloud.setOperator(dtoOperator);
			cloud.setName(dtoName);
			cloud.setSecure(dtoSecure);
			cloud.setNeighbor(dtoNeighbor);
			cloud.setAuthenticationInfo(dto.getAuthenticationInfo());
			
			final Cloud savedCloud = cloudRepository.saveAndFlush(cloud);
			cloudRepository.refresh(cloud);
			
			return savedCloud;
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public CloudWithRelaysResponseDTO assignRelaysToCloudResponse(final long id, final List<Long> gatekeeperRelayIds, final List<Long> gatewayRelayIds) {
		logger.debug("assignRelaysToCloud started...");
		
		final Cloud entry = assignRelaysToCloud(id, gatekeeperRelayIds, gatewayRelayIds);
		
		return DTOConverter.convertCloudToCloudWithRelaysResponseDTO(entry);
	}
		
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	@Transactional(rollbackFor = ArrowheadException.class)
	public Cloud assignRelaysToCloud(final long id, final List<Long> gatekeeperRelayIds, final List<Long> gatewayRelayIds) {
		logger.debug("assignRelaysToCloud started...");
		
		try {
			if (id < 1) {
				throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
			}
			
			final Optional<Cloud> cloudOpt = cloudRepository.findById(id);
			if(cloudOpt.isEmpty()) {
				throw new InvalidParameterException("Cloud with id '" + id +"' not exists");
			}
			final Cloud cloud = cloudOpt.get();
			
			final Set<Long> extantGatekeeperRelayIds = collectGatekeeperRelayIdsFromCloud(cloud);
			final Set<Long> extantGatewayRelayIds = collectGatewayRelayIdsFromCloud(cloud);
			
			final Set<Long> normalizedGatekeeperRelayIds = new HashSet<>();
			if (gatekeeperRelayIds != null && !gatekeeperRelayIds.isEmpty()) {		
				for (final Long relayId : gatekeeperRelayIds) {
					if (relayId != null && relayId >= 1 && !extantGatekeeperRelayIds.contains(relayId) ) {					
						normalizedGatekeeperRelayIds.add(relayId);
					} else {
						throw new InvalidParameterException("Invalid relay id: " + relayId);
					}
				}
			}
			
			final Set<Long> normalizedGatewayRelayIds = new HashSet<>();
			if (gatewayRelayIds != null && !gatewayRelayIds.isEmpty()) {		
				for (final Long relayId : gatewayRelayIds) {
					if (relayId != null && relayId >= 1 && !extantGatewayRelayIds.contains(relayId)) {
						normalizedGatewayRelayIds.add(relayId);
					} else {
						throw new InvalidParameterException("Invalid relay id: " + relayId);
					}
				}
			}			
			
			final List<Relay> gatekeeperRelays = collectAndValidateGatekeeperRelays(normalizedGatekeeperRelayIds);
			final List<Relay> gatewayRelays = collectAndValidateGatewayRelays(normalizedGatewayRelayIds);
			final Set<Long> savedCloudIds = saveCloudAndRelayConnections(cloud, gatekeeperRelays, gatewayRelays);
			
			return cloudRepository.findById(savedCloudIds.iterator().next()).get();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeCloudById(final long id) {
		logger.debug("removeCloudById started...");
		
		if (id < 1) {
			throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
		}

		try {
			if (cloudRepository.existsById(id)) {
				cloudRepository.deleteById(id);
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public RelayListResponseDTO getRelaysResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getRelaysResponse started...");
		
		final Page<Relay> entries = getRelays(page, size, direction, sortField);
		
		return DTOConverter.convertRelayListToRelayResponseListDTO(entries);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public Page<Relay> getRelays(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getRelays started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!Relay.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return relayRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<Relay> getPublicGatewayRelays() {
		logger.debug("getPublicRelaysByType started...");
				
		try {
			return relayRepository.findAllByExclusiveAndTypeIn(false, List.of(RelayType.GATEWAY_RELAY, RelayType.GENERAL_RELAY));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public RelayResponseDTO getRelayByIdResponse(final long id) {
		logger.debug("getRelayByIdResponse started...");
		
		final Relay entry = getRelayById(id);
		
		return DTOConverter.convertRelayToRelayResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	public Relay getRelayById(final long id) {
		logger.debug("updateRelayById started...");
		
		try {
			if (id < 1) {
				throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
			}
				
			final Optional<Relay> relayOpt = relayRepository.findById(id);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with id '" + id + "' not exists");
			}
			
			return relayOpt.get();			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public RelayResponseDTO getRelayByAddressAndPortResponse(final String address, final int port) {
		logger.debug("getRelayByAddressAndPortResponse started...");
		
		final Relay entry = getRelayByAddressAndPort(address, port);
		
		return DTOConverter.convertRelayToRelayResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	public Relay getRelayByAddressAndPort(final String address, final int port) {
		logger.debug("getRelayByAddressAndPort started...");
		
		try {
			if (Utilities.isEmpty(address)) {
				throw new InvalidParameterException("Address is empty");
			}
			final String _address = address.toLowerCase().trim();
				
			final Optional<Relay> relayOpt = relayRepository.findByAddressAndPort(_address, port);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with the following address and port not exists: " + _address + ", " + port);
			}
			
			return relayOpt.get();			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Relay getRelayByAuthenticationInfo(final String authenticationInfo) { 
		logger.debug("getRelayByAuthenticationInfo started...");
		
		try {
			if (Utilities.isEmpty(authenticationInfo)) {
				throw new InvalidParameterException("Authentication info is empty");
			}
				
			final Optional<Relay> relayOpt = relayRepository.findByAuthenticationInfo(authenticationInfo);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with the following authentication info not exists: " + authenticationInfo);
			}
			
			return relayOpt.get();			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	public Set<Relay> getAllLiveGatekeeperRelays() {
		final Set<Relay> result = new HashSet<>();
		final List<Cloud> clouds = cloudRepository.findAll();
		for (final Cloud cloud : clouds) {
			if (!cloud.getOwnCloud()) {
				final Set<CloudGatekeeperRelay> gatekeeperRelays = cloud.getGatekeeperRelays();
				for (final CloudGatekeeperRelay gkRelay : gatekeeperRelays) {
					result.add(gkRelay.getRelay());
				}
			}
		}
		
		return result;
	}
 	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public RelayListResponseDTO registerBulkRelaysResponse(final List<RelayRequestDTO> dtoList) {
		logger.debug("registerBulkRelaysResponse started...");
		
		final List<Relay> entries = registerBulkRelays(dtoList);
		
		return DTOConverter.convertRelayListToRelayResponseListDTO(new PageImpl<>(entries));
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<Relay> registerBulkRelays(final List<RelayRequestDTO> dtoList) { 
		logger.debug("registerBulkRelays started...");
		
		try {
			final Map<String, Relay> relaysToSave = new HashMap<>();
			final Set<String> authenticationInfos = new HashSet<>();
			
			if (dtoList == null || dtoList.isEmpty()) {
				throw new InvalidParameterException("List of RelayRequestDTO is null or empty");
			}
			
			for (final RelayRequestDTO dto : dtoList) {
				if (dto == null) {
					throw new InvalidParameterException("List of RelayRequestDTO contains null element");
				}
				
				validateRelayParameters(true, dto.getAddress(), dto.getPort(), dto.getAuthenticationInfo(), dto.isExclusive(), dto.getType());
				
				if (!Utilities.isEmpty(dto.getAuthenticationInfo())) {
					if (authenticationInfos.contains(dto.getAuthenticationInfo())) {
						throw new InvalidParameterException("List of RelayRequestDTO contains the following authentication info multiple times: " + dto.getAuthenticationInfo());
					}
					
					authenticationInfos.add(dto.getAuthenticationInfo());
				}
				
				final String address = dto.getAddress().toLowerCase().trim();
				final String uniqueConstraint = address  + ":" + dto.getPort();
				
				if (relaysToSave.containsKey(uniqueConstraint)) {
					throw new InvalidParameterException("List of RelayRequestDTO contains unique constraint violation: " + address + " address with " + dto.getPort() + " port");
				}
				
				relaysToSave.put(uniqueConstraint, new Relay(address, dto.getPort(), dto.getAuthenticationInfo(), dto.isSecure(), dto.isExclusive(), Utilities.convertStringToRelayType(dto.getType())));
			}
			
			final List<Relay> savedRelays = relayRepository.saveAll(relaysToSave.values());
			relayRepository.flush();
			
			return savedRelays;
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public RelayResponseDTO updateRelayByIdResponse(final long id, final String address, final int port, final String authenticationInfo, final boolean isSecure, final boolean isExclusive, final RelayType type) { 
		logger.debug("updateRelayByIdResponse started...");
		
		final Relay entry = updateRelayById(id, address, port, authenticationInfo, isSecure, isExclusive, type);
		
		return DTOConverter.convertRelayToRelayResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	@Transactional(rollbackFor = ArrowheadException.class)
	public Relay updateRelayById(final long id, final String address, final int port, final String authenticationInfo, final boolean isSecure, final boolean isExclusive, RelayType type) { 
		logger.debug("updateRelayById started...");
		
		try {
			if (id < 1) {
				throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
			}
			
			final Optional<Relay> relayOpt = relayRepository.findById(id);
			if (relayOpt.isEmpty()) {
				throw new InvalidParameterException("Relay with id '" + id + "' not exists");
			}
			final Relay relay = relayOpt.get();
			
			if (type == null) {
				type = relay.getType();
			}
			
			if (relay.getType() != type) { 
				throw new InvalidParameterException("Type of relay couldn't be updated");
			}
			
			validateRelayParameters(false, address, port, authenticationInfo, isExclusive, type.toString());
			
			final String _authenticationInfo = Utilities.isEmpty(authenticationInfo) ? null : authenticationInfo;
			
			if (_authenticationInfo != null && !_authenticationInfo.equals(relay.getAuthenticationInfo())) {
				checkAuthenticationInfoUniqueConstraintOfRelayTable(_authenticationInfo);
			}
			
			if (!relay.getAddress().equalsIgnoreCase(address) || relay.getPort() != port) {
				checkAddressPortUniqueConstraintOfRelayTable(address, port);
			}
			
			relay.setAddress(address);
			relay.setPort(port);
			relay.setAuthenticationInfo(_authenticationInfo);
			relay.setSecure(isSecure);
			relay.setExclusive(isExclusive);
			relay.setType(type);
			
			return relayRepository.saveAndFlush(relay);			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeRelayById(final long id) {
		logger.debug("removeRelayById started...");
		
		try {
			if (id < 1) {
				throw new InvalidParameterException(ID_NOT_VALID_ERROR_MESSAGE);
			}
			
			final Optional<Relay> relayOpt = relayRepository.getByIdWithCloudGatekeepers(id);
			if (relayOpt.isPresent()) {
				final Relay relay = relayOpt.get();
				
				for (final CloudGatekeeperRelay cloudConn : relay.getCloudGatekeepers()) {
					if (cloudConn.getCloud().getGatekeeperRelays().size() == 1) {
						throw new InvalidParameterException("Relay couldn't be removed, because the following cloud woud stay without gatekeeper Relay: " + cloudConn.getCloud());	
					}
				}
				
				relayRepository.deleteById(id);
			}			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private void validateCloudParameters(final boolean withUniqueConstraintCheck, String operator, String name, Boolean secure, final String authenticationInfo,
										 final List<Long> gatekeeperRelayIds, final List<Long> gatewayRealyIds) {
		logger.debug("validateCloudParameters started...");
		
		if (Utilities.isEmpty(operator)) {
			throw new InvalidParameterException("Operator is empty");
		}
		operator = operator.toLowerCase().trim();
		
		if (!cnVerifier.isValid(operator)) {
			throw new InvalidParameterException("Operator" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(name)) {
			throw new InvalidParameterException("Name is empty");
		}
		name = name.toLowerCase().trim();
		
		if (!cnVerifier.isValid(name)) {
			throw new InvalidParameterException("Name" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		secure = secure == null ? false : secure;
		if (secure && Utilities.isEmpty(authenticationInfo)) {
			throw new InvalidParameterException("Secure cloud without authenticationInfo is denied");
		}
		
		if (withUniqueConstraintCheck) {
			checkUniqueConstraintOfCloudTable(operator, name);
		}
		
		if (gatekeeperRelayIds == null || gatekeeperRelayIds.isEmpty()) {
			throw new InvalidParameterException("GatekeeperRelayIds list is empty");
		} else {
			for (final Long id : gatekeeperRelayIds) {
				if (id == null || id < 1 || !relayRepository.existsById(id)) {
					throw new InvalidParameterException("GatekeeperRelay with id '" + id  + "' not exists");
				}
			}
		}
		
		if (gatewayRealyIds != null && !gatewayRealyIds.isEmpty()) {		
			for (final Long id : gatewayRealyIds) {
				if (id == null || id < 1 || !relayRepository.existsById(id)) {
					throw new InvalidParameterException("GatekewayRelay with id '" + id  + "' not exists");
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkUniqueConstraintOfCloudTable(final String operator, final String name) {
		logger.debug("checkUniqueConstraintOfCloudTable started...");
		
		if (cloudRepository.existsByOperatorAndName(operator, name)) {
			throw new InvalidParameterException("Cloud with the following operator and name already exists: " + operator + ", " + name);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void validateRelayParameters(final boolean withUniqueConstraintCheck, String address, final Integer port, final String authenticationInfo, Boolean exclusive, final String type) {
		logger.debug("validateRelayParameters started...");
		
		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address is empty");
		}
		address = address.toLowerCase().trim();
		
		if (port == null) {
			throw new InvalidParameterException("Port is null");
		} else if (isPortOutOfValidRange(port)) {
			throw new InvalidParameterException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX);
		}
		
		exclusive = exclusive == null ? false : exclusive;
		
		final RelayType typeEnum = Utilities.convertStringToRelayType(type);
		if (typeEnum == null) {
			throw new InvalidParameterException(type + " type is invalid");
		}
		
		if (exclusive && typeEnum == RelayType.GATEKEEPER_RELAY) {
			throw new InvalidParameterException("GATEKEEPER_RELAY type cloudn't be exclusive");
		}
		
		if (exclusive && typeEnum == RelayType.GENERAL_RELAY) {
			throw new InvalidParameterException("GENERAL_RELAY type cloudn't be exclusive");
		}
		
		if (withUniqueConstraintCheck) {
			checkAddressPortUniqueConstraintOfRelayTable(address, port);
			
			if (!Utilities.isEmpty(authenticationInfo)) {
				checkAuthenticationInfoUniqueConstraintOfRelayTable(authenticationInfo);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkAddressPortUniqueConstraintOfRelayTable(final String address, final int port) {
		logger.debug("checkAddressPortUniqueConstraintOfRelayTable started...");
		
		if (relayRepository.existsByAddressAndPort(address, port)) {
			throw new InvalidParameterException("Relay with the following address and port already exists: " + address + ", " + port);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkAuthenticationInfoUniqueConstraintOfRelayTable(final String authenticationInfo) {
		logger.debug("checkAuthenticationInfoUniqueConstraintOfRelayTable started...");
		
		if (relayRepository.existsByAuthenticationInfo(authenticationInfo)) {
			throw new InvalidParameterException("Relay with the following authentication info already exists: " + authenticationInfo);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private boolean isPortOutOfValidRange(final int port) {
		logger.debug("isPortOutOfValidRange started...");
		
		return port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Relay> collectAndValidateGatekeeperRelays(final Iterable<Long> gatekeeperRelayIds) {
		logger.debug("collectAndValidateGatekeeperRelays started...");
		
		List<Relay> gatekeeperRelays = new ArrayList<>();
		if (gatekeeperRelayIds != null) {
			gatekeeperRelays = relayRepository.findAllById(gatekeeperRelayIds);		
			
			for (final Relay relay : gatekeeperRelays) {
				if (relay.getExclusive()) {
					throw new InvalidParameterException("Relay with gatekeeper purpose couldn't be exclusive");
				}
				if (relay.getType() != RelayType.GATEKEEPER_RELAY && relay.getType() != RelayType.GENERAL_RELAY) {
					throw new InvalidParameterException("Relay with gatekeeper purpose could be only " + RelayType.GATEKEEPER_RELAY + " or " + RelayType.GENERAL_RELAY + " type, but not " +
														 relay.getType() + " type");
				}
			}			
		}
		
		return gatekeeperRelays;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Relay> collectAndValidateGatewayRelays(final Iterable<Long> gatekewayRelayIds) {
		logger.debug("collectAndValidateGatewayRelays started...");
		
		List<Relay> gatewayRelays = new ArrayList<>();
		if (gatekewayRelayIds != null) {
			gatewayRelays = relayRepository.findAllById(gatekewayRelayIds);
			
			for (final Relay relay : gatewayRelays) {
				if (relay.getType() != RelayType.GATEWAY_RELAY || !relay.getExclusive()) {
					final String exclusivity = relay.getExclusive() ? "exclusive " : "non-exclusive ";
					throw new InvalidParameterException("Gateway dedicated relay could be only exclusive " + RelayType.GATEWAY_RELAY + " type, but not " + exclusivity + relay.getType() + " type");
				}
			}
		}
		
		return gatewayRelays;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<Long> saveCloudAndRelayConnections(final List<Cloud> savedClouds, final Map<String, List<Relay>> gatekeeperRelaysForClouds, final Map<String, List<Relay>> gatewayRelaysForClouds) {
		logger.debug("saveCloudAndRelayConnections started...");

		final List<CloudGatekeeperRelay> cloudGatekeeperRelaysToSave = new ArrayList<>();
		final List<CloudGatewayRelay> cloudGatewayRelaysToSave = new ArrayList<>();
		final Set<Long> savedCloudIds = new HashSet<>();
		for (final Cloud cloud : savedClouds) {
			final String cloudUniqueConstraint = cloud.getOperator() + "." + cloud.getName();
			
			final List<Relay> gatekeeperRelays = gatekeeperRelaysForClouds.get(cloudUniqueConstraint);				
			for (final Relay relay : gatekeeperRelays) {
				cloudGatekeeperRelaysToSave.add(new CloudGatekeeperRelay(cloud, relay));
			}
			
			final List<Relay> gatewayRelays = gatewayRelaysForClouds.get(cloudUniqueConstraint);
			for (final Relay relay : gatewayRelays) {
				cloudGatewayRelaysToSave.add(new CloudGatewayRelay(cloud, relay));
			}
			
			savedCloudIds.add(cloud.getId());
		}
		
		cloudGatekeeperRelayRepository.saveAll(cloudGatekeeperRelaysToSave);
		cloudGatekeeperRelayRepository.flush();
		
		cloudGatewayRelayRepository.saveAll(cloudGatewayRelaysToSave);
		cloudGatewayRelayRepository.flush();
		
		for (final Cloud cloud : savedClouds) {
			cloudRepository.refresh(cloud);
		}
		
		return savedCloudIds;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<Long> saveCloudAndRelayConnections (final Cloud cloud, final List<Relay> gatekeeperRelaysForClouds, final List<Relay> gatewayRelaysForClouds) {
		return saveCloudAndRelayConnections(List.of(cloud), Map.of(cloud.getOperator() + "." + cloud.getName(), gatekeeperRelaysForClouds),
											Map.of(cloud.getOperator() + "." + cloud.getName(), gatewayRelaysForClouds));
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<Long> collectGatekeeperRelayIdsFromCloud(final Cloud cloud) {
		logger.debug("collectGatkeeperRelayIdsFromCloud started...");
		
		final Set<Long> idSet = new HashSet<>();
		for (final CloudGatekeeperRelay conn : cloud.getGatekeeperRelays()) {
			idSet.add(conn.getRelay().getId());
		}
		
		return idSet;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<Long> collectGatewayRelayIdsFromCloud(final Cloud cloud) {
		logger.debug("collectGatewayRelayIdsFromCloud started...");
		
		final Set<Long> idSet = new HashSet<>();
		for (final CloudGatewayRelay conn : cloud.getGatewayRelays()) {
			idSet.add(conn.getRelay().getId());
		}
		
		return idSet;	
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Relay> deleteUnnecessaryGatekeeperRelayConnectionsAndGetValidatedAdditionalRelays(final Cloud cloud, final List<Long> gatekeeperRealyIds) {
		logger.debug("deleteUnnecessaryGatekeeperRelayConnectionsAndGetAdditionallyRelayIds started...");
		
		final Set<Long> relaysToKeep = new HashSet<>();
		final Set<Long> relaysConnToDelete = new HashSet<>();
		final Set<Long> relaysToAssign = new HashSet<>();
		for (final CloudGatekeeperRelay relayConn : cloud.getGatekeeperRelays()) {
			boolean relayConnToRemove = true;
			for (final Long dtoRelayId : gatekeeperRealyIds) {
				if (relayConn.getRelay().getId() == dtoRelayId) {
					relaysToKeep.add(relayConn.getRelay().getId());
					relayConnToRemove = false;
					break;
				} 
			}
			
			if (relayConnToRemove) {
				relaysConnToDelete.add(relayConn.getId());
			}
		}
		
		relaysToAssign.addAll(gatekeeperRealyIds);
		relaysToAssign.removeAll(relaysToKeep);
		
		final List<Relay> validatedRelaysToAdd = collectAndValidateGatekeeperRelays(relaysToAssign);
		
		if (relaysToKeep.isEmpty() && validatedRelaysToAdd.isEmpty()) {
			throw new InvalidParameterException("Cloud can't exist without gatekeeper Relay");
		}
		
		final List<CloudGatekeeperRelay> entriesToDelete = cloudGatekeeperRelayRepository.findAllById(relaysConnToDelete);
		cloudGatekeeperRelayRepository.deleteInBatch(entriesToDelete);
		
		return validatedRelaysToAdd;
	}	
	
	//-------------------------------------------------------------------------------------------------
	private List<Relay> deleteUnnecessaryGatewayRelayConnectionsAndGetValidatedAdditionalRelays(final Cloud cloud, final List<Long> gatewayRealyIds) {
		logger.debug("deleteUnnecessaryGatewayRelayConnectionsAndGetAdditionallyRelayIds started...");
		
		final Set<Long> relaysToKeep = new HashSet<>();
		final Set<Long> relaysConnToDelete = new HashSet<>();
		final Set<Long> relaysToAssign = new HashSet<>();
		for (final CloudGatewayRelay relayConn : cloud.getGatewayRelays()) {
			boolean relayConnToRemove = true;
			if (gatewayRealyIds != null && !gatewayRealyIds.isEmpty()) {				
				for (final Long dtoRelayId : gatewayRealyIds) {
					if (relayConn.getRelay().getId() == dtoRelayId) {
						relaysToKeep.add(relayConn.getRelay().getId());
						relayConnToRemove = false;
						break;
					} 
				}
			}
			
			if (relayConnToRemove) {
				relaysConnToDelete.add(relayConn.getId());
			}
		}
		
		if (gatewayRealyIds != null && !gatewayRealyIds.isEmpty()) {
			relaysToAssign.addAll(gatewayRealyIds);
		}
		relaysToAssign.removeAll(relaysToKeep);
		
		final List<CloudGatewayRelay> entriesToDelete = cloudGatewayRelayRepository.findAllById(relaysConnToDelete);
		cloudGatewayRelayRepository.deleteInBatch(entriesToDelete);
		
		return collectAndValidateGatewayRelays(relaysToAssign);
	}
}