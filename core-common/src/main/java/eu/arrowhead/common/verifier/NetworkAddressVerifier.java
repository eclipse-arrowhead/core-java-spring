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

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;

@Component
public class NetworkAddressVerifier {

	//=================================================================================================
	// members
	
	public static final String ERROR_MSG_PREFIX = "Network address verification failure: ";
	public static final String IPV4_REGEX_STRING = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
	public static final String IPV6_REGEX_STRING = "^([0-9a-fA-F]{4}:){7}[0-9a-fA-F]{4}$";
	private static final Pattern ipv4Pattern;
	private static final Pattern ipv6Pattern;
	
	static {
		ipv4Pattern = Pattern.compile(IPV4_REGEX_STRING);
		ipv6Pattern = Pattern.compile(IPV6_REGEX_STRING);
	}
	
	public static final String IPV4_PLACEHOLDER = "0.0.0.0";
	public static final String IPV4_LOOPBACK_1ST_OCTET = "127";
	private static final String IPV4_APIPA_1ST_AND_2ND_OCTET = "169.254";
	private static final String IPV4_LOCAL_BROADCAST = "255.255.255.255";
	private static final int IPV4_MULTICAST_1ST_OCTET_START = 224;
	private static final int IPV4_MULTICAST_1ST_OCTET_END = 239;
	
	public static final String IPV6_UNSPECIFIED = "0000:0000:0000:0000:0000:0000:0000:0000";
	public static final String IPV6_LOOPBACK = "0000:0000:0000:0000:0000:0000:0000:0001";
	private static final String IPV6_LINK_LOCAL_PREFIX = "fe80";
	private static final String IPV6_MULTICAST_PREFIX = "ff";
	
	private static final String NO_TYPE_LOCALHOST = "localhost";
	private static final String NO_TYPE_LOOPBACK = "loopback";
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Value(CoreCommonConstants.$ALLOW_SELF_ADDRESSING_WD)
	private boolean allowSelfAddressing;
	
	@Value(CoreCommonConstants.$ALLOW_NON_ROUTABLE_ADDRESSING_WD)
	private boolean allowNonRoutableAddressing;
	
	private final Logger logger = LogManager.getLogger(NetworkAddressVerifier.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public void configure(final boolean allowSelfAddressing, final boolean allowNonRoutableAddressing) {
		this.allowSelfAddressing = allowSelfAddressing;
		this.allowNonRoutableAddressing = allowNonRoutableAddressing;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void verify(final String address) throws InvalidParameterException {
		logger.debug("verify started...");
		
		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException(ERROR_MSG_PREFIX + "Network address is empty.");
		}
		
		final String candidate = address.trim();
		if (ipv4Pattern.matcher(candidate).matches()) {
			verifyIPV4(candidate);
			
		} else if (ipv6Pattern.matcher(candidate).matches()) {
			verifyIPV6(candidate);
			
		} else {
			verifyNoType(candidate);			
		}		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void verifyIPV4(final String candidate) {
		logger.debug("verifyIPV4 started...");
		
		if (!allowSelfAddressing) {			
			// Filter out loopback (127.0.0.0 - 127.255.255.255)
			if (candidate.startsWith(IPV4_LOOPBACK_1ST_OCTET)) {
				throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv4 network address is invalid: self-addressing is disabled.");
			}
		}
		
		if (!allowNonRoutableAddressing) {
			// Filter out APIPA (Automatic Private IP Address: 169.254.?.?)
			if (candidate.startsWith(IPV4_APIPA_1ST_AND_2ND_OCTET)) {
				throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv4 network address is invalid: non-routable-addressing is disabled.");
			}
		}
		
		// Filter out IP placeholder(default route) (0.0.0.0)
		if (candidate.equalsIgnoreCase(IPV4_PLACEHOLDER)) {
			throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv4 network address is invalid: placeholder address is denied.");
		}
		
		// Filter out local broadcast (255.255.255.255)
		if (candidate.equalsIgnoreCase(IPV4_LOCAL_BROADCAST)) {
			throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv4 network address is invalid: local broadcast address is denied.");
		}
		
		// Could not filter out directed broadcast (cannot determine it without the subnet mask)

		// Filter out multicast (Class D: 224.0.0.0 - 239.255.255.255)
		final String[] octets = candidate.split("\\.");
		final Integer firstOctet = Integer.valueOf(octets[0]);
		if (firstOctet >= IPV4_MULTICAST_1ST_OCTET_START && firstOctet <= IPV4_MULTICAST_1ST_OCTET_END) {
			throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv4 network address is invalid: multicast addresses are denied.");
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyIPV6(final String candidate) {
		logger.debug("verifyIPV6 started...");
		
		if (!allowSelfAddressing) {
			// Filter out loopback address (0000:0000:0000:0000:0000:0000:0000:0001)			
			if (candidate.equalsIgnoreCase(IPV6_LOOPBACK)) {
				throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv6 network address is invalid: self-addressing is disabled.");
			}			
		}

		if (!allowNonRoutableAddressing) {
			// Filter out link-local addresses (prefix fe80)
			if (candidate.startsWith(IPV6_LINK_LOCAL_PREFIX)) {
				throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv6 network address is invalid: non-routable-addressing is disabled.");
			}
		}
		
		// Filter out unspecified address (0000:0000:0000:0000:0000:0000:0000:0000)			
		if (candidate.equalsIgnoreCase(IPV6_UNSPECIFIED)) {
			throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv6 network address is invalid: unspecified address is denied.");
		}
		
		// Filter out multicast (prefix ff)
		if (candidate.startsWith(IPV6_MULTICAST_PREFIX)) {
			throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " ipv6 network address is invalid: multicast addresses are denied.");
		}
		
		// Could not filter out anycast addresses (indistinguishable from other unicast addresses)
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyNoType(final String candidate) {
		logger.debug("verifyNoType started...");
		
		final String[] parts = candidate.split("\\.");
		for (final String part : parts) {
			if (!cnVerifier.isValid(part)) {
				throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " no-type network address is invalid: dot(.) separated parts can contain only letters (english alphabet), numbers and dash (-) and have to start with a letter (also cannot end with dash). A part can contain maximum 63 character.");
			}			
		}		
		
		if (!allowSelfAddressing) {
			// Filter out 'localhost' and 'loopback'
			if (candidate.equalsIgnoreCase(NO_TYPE_LOCALHOST) || candidate.equalsIgnoreCase(NO_TYPE_LOOPBACK)) {
				throw new InvalidParameterException(ERROR_MSG_PREFIX + candidate + " no-type network address is invalid: self-addressing is disabled.");
			}
		}	
	}
}
