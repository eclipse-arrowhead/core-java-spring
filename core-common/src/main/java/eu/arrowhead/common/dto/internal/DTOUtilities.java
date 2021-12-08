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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class DTOUtilities {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DTOUtilities.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static boolean equalsSystemInResponseAndRequest(final SystemResponseDTO response, final SystemRequestDTO request) {
		logger.debug("equalsSystemInResponseAndRequest started...");
		
		if (response == null) {
			return request == null;
		}
		
		if (request == null) {
			return false;
		}
		
		final SystemRequestDTO converted = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(response);
		normalizeSystemRequestDTO(converted);
		
		final SystemRequestDTO requestCopy = copySystemRequestDTO(request);
		normalizeSystemRequestDTO(requestCopy);
		
		return converted.equals(requestCopy);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static boolean equalsCloudInResponseAndRequest(final CloudResponseDTO response, final CloudRequestDTO request) {
		logger.debug("equalsCloudInResponseAndRequest started...");
		
		if (response == null) {
			return request == null;
		}
		
		if (request == null) {
			return false;
		}
		
		final CloudRequestDTO converted = DTOConverter.convertCloudResponseDTOToCloudRequestDTO(response);
		normalizeCloudRequestDTO(converted);
		
		final CloudRequestDTO requestCopy = copyCloudRequestDTO(request);
		normalizeCloudRequestDTO(requestCopy);
		
		return converted.equals(requestCopy);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOUtilities() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static SystemRequestDTO copySystemRequestDTO(final SystemRequestDTO orig) {
		logger.debug("copySystemRequestDTO started...");

		if (orig == null) {
			return null;
		}
		
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName(orig.getSystemName());
		result.setAddress(orig.getAddress());
		result.setPort(orig.getPort());
		result.setAuthenticationInfo(orig.getAuthenticationInfo());
		result.setMetadata(orig.getMetadata()); // no need of deep copy
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void normalizeSystemRequestDTO(final SystemRequestDTO dto) {
		logger.debug("normalizeSystemRequestDTO started...");
		
		final String systemName = dto.getSystemName();
		if (systemName != null) {
			dto.setSystemName(systemName.toLowerCase().trim());
		}
		
		final String address = dto.getAddress();
		if (address != null) {
			dto.setAddress(address.toLowerCase().trim());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private static CloudRequestDTO copyCloudRequestDTO(final CloudRequestDTO orig) {
		logger.debug("copyCloudRequestDTO started...");
		
		if (orig == null) {
			return null;
		}
		
		final CloudRequestDTO result = new CloudRequestDTO();
		result.setName(orig.getName());
		result.setOperator(orig.getOperator());
		result.setSecure(orig.getSecure());
		result.setNeighbor(orig.getNeighbor());
		result.setAuthenticationInfo(orig.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void normalizeCloudRequestDTO(final CloudRequestDTO dto) {
		logger.debug("normalizeCloudRequestDTO started...");
		
		if (dto == null) {
			return;
		}
		
		final String cloudName = dto.getName();
		if (cloudName != null) {
			dto.setName(cloudName.toLowerCase().trim());
		}
		
		final String operator = dto.getOperator();
		if (operator != null) {
			dto.setOperator(operator.toLowerCase().trim());
		}
	}
}