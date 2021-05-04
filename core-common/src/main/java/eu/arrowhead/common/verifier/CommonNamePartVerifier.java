/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;

@Component
public class CommonNamePartVerifier {

	//=================================================================================================
	// members
	
	public static final int COMMON_NAME_PART_MAX_LENGTH = 63;
	public static final String COMMON_NAME_PART_PATTERN_STRING = "^[A-Za-z](?:[0-9A-Za-z-]*[0-9A-Za-z])?$";
	private static final Pattern commonNamePartPattern;
	
	static {
	    commonNamePartPattern = Pattern.compile(COMMON_NAME_PART_PATTERN_STRING);
	}
	
	private final Logger logger = LogManager.getLogger(CommonNamePartVerifier.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean isValid(final String name) {
		if (Utilities.isEmpty(name)) {
			logger.debug("Invalid CN part.");
			return false;
		}
		
		final String candidate = name.trim();
		if (candidate.length() > COMMON_NAME_PART_MAX_LENGTH) {
			return false;
		}
		
		final Matcher matcher = commonNamePartPattern.matcher(candidate);
		final boolean match = matcher.matches();
		if (match) {
			logger.debug("CN part {} validation result: {}", candidate, match);
			return true;
		} else {
			logger.debug("Invalid CN part: {}", candidate);
			return false;
		}
	}
}