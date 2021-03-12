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

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class CommonDBService {

	//=================================================================================================
	// members
	
	@Autowired
	private CloudRepository cloudRepository;

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
}