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

package eu.arrowhead.common.database.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.LogsRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@Service
public class CommonDBService {

	//=================================================================================================
	// members
	
	private static final List<LogLevel> ALL_LOG_LEVELS = Arrays.asList(LogLevel.values());
	private static final ZonedDateTime START_OF_TIMES = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 1, ZoneId.systemDefault());
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private LogsRepository logsRepository;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;

	private final Logger logger = LogManager.getLogger(CommonDBService.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Cloud getOwnCloud(final boolean isSecure) {
		logger.debug("getOwnCloud started...");
		try {
			final List<Cloud> cloudList = cloudRepository.findByOwnCloudAndSecure(true, isSecure);
			if (cloudList.isEmpty()) {
				throw new DataNotFoundException("Could not find own cloud information in the database");
			} else if (cloudList.size() > 1) {
				throw new ArrowheadException("More than one cloud is marked as own in " + (isSecure ? "SECURE" : "INSECURE") + " mode.");
			}
			
			return cloudList.get(0);
		} catch (final ArrowheadException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public Cloud insertOwnCloud(final String operator, final String name, final boolean secure, final String authenticationInfo) {
		logger.debug("insertOwnCloudWithoutGatekeeper started...");
		Assert.isTrue(!Utilities.isEmpty(operator), "Operator is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(name), "Name is null or empty.");
		
		if (!cnVerifier.isValid(operator)) {
			throw new InvalidParameterException("Operator has invalid format. Operator must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING);
		}
		
		if (!cnVerifier.isValid(name)) {
			throw new InvalidParameterException("Name has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING);
		}
		
		final String validOperator = operator.toLowerCase().trim();
		final String validName = name.toLowerCase().trim();
		try {
			final Optional<Cloud> optCloud = cloudRepository.findByOperatorAndName(validOperator, validName);
			if (optCloud.isPresent()) {
				throw new InvalidParameterException("Cloud with operator " + validOperator + " and name " + validName + " is already exists.");
			}
			
			final Cloud cloud = new Cloud(validOperator, validName, secure, false, true, authenticationInfo);
			return cloudRepository.saveAndFlush(cloud);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<Logs> getLogEntries(final int page, final int size, final Direction direction, final String sortField, final CoreSystem system, final List<LogLevel> levels, final ZonedDateTime from, final ZonedDateTime to, final String loggerStr) { 	
		logger.debug("getLogEntries started...");
		
		Assert.notNull(system, "System is null.");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? Logs.FIELD_NAME_ID : sortField.trim();
		
		if (!Logs.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		final List<LogLevel> _levels = levels == null || levels.isEmpty() ? ALL_LOG_LEVELS : levels;
		final ZonedDateTime _from = from == null ? START_OF_TIMES : from;
		final ZonedDateTime _to = to == null ? ZonedDateTime.now() : to;
		
		if (_to.isBefore(_from)) {
			throw new InvalidParameterException("Invalid time interval");
		}
		
		final PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
		
		try {
			if (Utilities.isEmpty(loggerStr)) {
				return logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetween(system, _levels, _from, _to, pageRequest);
			} else {
				return logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetweenAndLoggerContainsIgnoreCase(system, _levels, _from, _to, loggerStr, pageRequest);
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public LogEntryListResponseDTO getLogEntriesResponse(final int page, final int size, final Direction direction, final String sortField, final CoreSystem system, final List<LogLevel> levels, final ZonedDateTime from, final ZonedDateTime to,
														 final String loggerStr) {	
		logger.debug("getLogEntriesResponse started...");
		
		return DTOConverter.convertLogsPageToLogEntryListResponseDTO(getLogEntries(page, size, direction, sortField, system, levels, from, to, loggerStr));
	}
}