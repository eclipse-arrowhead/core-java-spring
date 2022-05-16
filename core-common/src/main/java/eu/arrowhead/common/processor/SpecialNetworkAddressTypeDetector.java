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

package eu.arrowhead.common.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.AddressType;

/** This class is only works if the input string is a result of NetworkAddressPreProcessor. */
@Component
public class SpecialNetworkAddressTypeDetector { 

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(SpecialNetworkAddressTypeDetector.class);
	
	private static final String DOT = ".";
	private static final String DOT_REGEXP = "\\.";
	private static final String COLON = ":";
	
	private static final int IPV6_SIZE = 8;
	private static final int IPV4_SIZE = 4;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public AddressType detectAddressType(final String address) {
		logger.debug("detectAddressType started...");
		
		if (Utilities.isEmpty(address)) {
			return null;
		}
		
		// Simple string
		if (!address.contains(DOT) && !address.contains(COLON)) {
			return AddressType.HOSTNAME;
		
		// Possible IPv4 or domain name
		} else if (address.contains(DOT) && !address.contains(COLON)) {
			return isIPv4(address) ? AddressType.IPV4 : AddressType.HOSTNAME;
		
		// Possible IPv6
		} else if (!address.contains(DOT) && address.contains(COLON)) {
			return isNormalizedIPv6(address) ? AddressType.IPV6 : null;
		}

		// Unknown format
		return null;
	}
	
	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	private boolean isIPv4(final String address) {
		logger.debug("isIPv4 started...");

		final String[] parts = address.split(DOT_REGEXP, -1);
		
		if (parts.length != IPV4_SIZE) {
			return false;
		}
		
		for (int i = 0; i < IPV4_SIZE; ++i) {
			try {
				final int octet = Integer.parseInt(parts[i]);
				if (octet > 255 || octet < 0) {
					return false;
				}
			} catch (final NumberFormatException ex) {
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isNormalizedIPv6(final String address) {
		logger.debug("isNormalizedIPv6 started...");

		final String[] parts = address.split(COLON, -1);
		
		if (parts.length != IPV6_SIZE) {
			return false;
		}
		
		for (int i = 0; i < IPV6_SIZE; ++i) {
			try {
				final int numValue = Integer.parseInt(parts[i], 16);
				if (numValue > 65535 || numValue < 0) {
					return false;
				}
			} catch (final NumberFormatException ex) {
				return false;
			}
		}
		
		return true;
	}
}