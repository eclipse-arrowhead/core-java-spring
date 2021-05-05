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

package eu.arrowhead.common.verifier;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;

@Component
public class ServiceInterfaceNameVerifier {

	//=================================================================================================
	// members
	
	static final String FIELD_STRICT_MODE = "strictMode";
	
	private static final String SERVICE_INTERFACE_NAME_PATTERN_STRING = "^([A-Z_0-9]+)-(SECURE|INSECURE)-([A-Z_0-9]+)$";
	private static final Pattern serviceInterfaceNamePattern;
	
	private static final List<String> validProtocols = List.of(CommonConstants.HTTP.toUpperCase(), CommonConstants.HTTPS.toUpperCase());
	private static final List<String> validFormats = List.of(CommonConstants.JSON, CommonConstants.XML);

	static {
	    serviceInterfaceNamePattern = Pattern.compile(SERVICE_INTERFACE_NAME_PATTERN_STRING);
	}
	
	private final Logger logger = LogManager.getLogger(ServiceInterfaceNameVerifier.class);
	
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_INTF_NAME_VERIFIER_WD)
	private boolean strictMode;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean isValid(final String name) {
		if (Utilities.isEmpty(name)) {
			logger.debug("Invalid interface name.");
			return false;
		}
		
		final String candidate = name.toUpperCase().trim();
		final Matcher matcher = serviceInterfaceNamePattern.matcher(candidate);
		
		final boolean match = matcher.matches();
		if (match) {
			if (strictMode) {
				final String protocol = matcher.group(1);
				final String format = matcher.group(3);
				
				final boolean strictMatch = isValidProtocol(protocol) && isValidFormat(format);
				logger.debug("Interface name {} validation result: {}", candidate, strictMatch);
				
				return strictMatch;
			}

			logger.debug("Interface name {} validation result: {}", candidate, match);
			return match;
		} else {
			logger.debug("Invalid interface name: {}", candidate);
			return false;
		}
	}
	
	//=================================================================================================
	// assistant method

	//-------------------------------------------------------------------------------------------------
	private boolean isValidProtocol(final String protocol) {
		return validProtocols.contains(protocol);
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isValidFormat(final String format) {
		return validFormats.contains(format);
	}
}