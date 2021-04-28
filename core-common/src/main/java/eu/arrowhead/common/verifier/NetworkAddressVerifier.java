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
	
	public static final String IPV4_REGEX_STRING = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
	public static final String IPV6_REGEX_STRING = "\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z";
	public static final String DOMAIN_NAME_REGEX_STRING = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";	
	private static final Pattern ipv4Pattern;
	private static final Pattern ipv6Pattern;
	private static final Pattern domainNamePattern;
	
	static {
		ipv4Pattern = Pattern.compile(IPV4_REGEX_STRING);
		ipv6Pattern = Pattern.compile(IPV6_REGEX_STRING);
		domainNamePattern = Pattern.compile(DOMAIN_NAME_REGEX_STRING);
	}
	
	private static final String IPV4_PLACEHOLDER = "0.0.0.0";
	private static final String IPV4_LOOPBACK_1ST_OCTET = "127";
	private static final String IPV4_APIPA_1ST_AND_2ND_OCTET = "169.254";
	private static final String IPV4_LOCAL_BROADCAST = "255.255.255.255";
	private static final int IPV4_MULTICAST_1ST_OCTET_START = 224;
	private static final int IPV4_MULTICAST_1ST_OCTET_END = 239;
	
	private static final String IPV6_UNSPECIFIED = "0:0:0:0:0:0:0:0";
	private static final String IPV6_LOOPBACK = "0:0:0:0:0:0:0:1";
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
	public void verify(final String address) throws InvalidParameterException { //TODO junit
		logger.debug("verify started...");
		
		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("network address is empty");
		}
		
		final String candidate = address.toLowerCase().trim();
		if (ipv4Pattern.matcher(candidate).matches()) {
			verifyIPV4(candidate);
			
		} else if (ipv6Pattern.matcher(candidate).matches()) {
			verifyIPV6(candidate);
			
		} else if (domainNamePattern.matcher(candidate).matches()) {
			verifyDomainName(candidate);
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
			// Filter out IP placeholder(default route) (0.0.0.0) and loopback (127.0.0.0 - 127.255.255.255)
			if (candidate.equalsIgnoreCase(IPV4_PLACEHOLDER) || candidate.startsWith(IPV4_LOOPBACK_1ST_OCTET)) {
				throw new InvalidParameterException(candidate + " ipv4 network address is invalid: self-addressing is disabled");
			}
		}
		
		if (!allowNonRoutableAddressing) {
			// Filter out APIPA (Automatic Private IP Address: 169.254.?.?)
			if (candidate.startsWith(IPV4_APIPA_1ST_AND_2ND_OCTET)) {
				throw new InvalidParameterException(candidate + " ipv4 network address is invalid: non-routable-addressing is disabled");
			}
		}
		
		// Filter out local broadcast (255.255.255.255)
		if (candidate.equalsIgnoreCase(IPV4_LOCAL_BROADCAST)) {
			throw new InvalidParameterException(candidate + " ipv4 network address is invalid: local broadcast address is denied");
		}
		
		// Could not filter out directed broadcast (cannot determine it without the subnet mask)

		// Filter out multicast (Class D: 224.0.0.0 - 239.255.255.255)
		final String[] octets = candidate.split("\\.");
		final Integer firstOctet = Integer.valueOf(octets[0]);
		if (firstOctet >= IPV4_MULTICAST_1ST_OCTET_START && firstOctet <= IPV4_MULTICAST_1ST_OCTET_END) {
			throw new InvalidParameterException(candidate + " ipv4 network address is invalid: multicast addresses are denied");
		}		
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyIPV6(final String candidate) {
		logger.debug("verifyIPV6 started...");
		
		if (!allowSelfAddressing) {
			// Filter out unspecified address (0:0:0:0:0:0:0:0) and loopback address (0:0:0:0:0:0:0:1)			
			if (candidate.equalsIgnoreCase(IPV6_UNSPECIFIED) || candidate.equalsIgnoreCase(IPV6_LOOPBACK)) {
				throw new InvalidParameterException(candidate + " ipv6 network address is invalid: self-addressing is disabled");
			}			
		}

		if (!allowNonRoutableAddressing) {
			// Filter out link-local addresses (prefix fe80)
			if (candidate.startsWith(IPV6_LINK_LOCAL_PREFIX)) {
				throw new InvalidParameterException(candidate + " ipv6 network address is invalid: non-routable-addressing is disabled");
			}
		}
		
		// Filter out multicast (prefix ff)
		if (candidate.startsWith(IPV6_MULTICAST_PREFIX)) {
			throw new InvalidParameterException(candidate + " ipv6 network address is invalid: multicast addresses are denied");
		}
		
		// Could not filter out anycast addresses (indistinguishable from other unicast addresses)
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyDomainName(final String candidate) {
		logger.debug("verifyDomainName started...");
		//TODO maybe we should offer some kind of white-listing possibility in future versions
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyNoType(final String candidate) {
		logger.debug("verifyNoType started...");
		
		if (!cnVerifier.isValid(candidate)) {
			throw new InvalidParameterException(candidate + " no-type network address is invalid: only letters (english alphabet), numbers and dash (-) are allowed and have to start with a letter (also cannot end with dash).");
		}
		
		if (!allowSelfAddressing) {
			// Filter out 'localhost' and 'loopback'
			if (candidate.equalsIgnoreCase(NO_TYPE_LOCALHOST) || candidate.equalsIgnoreCase(NO_TYPE_LOOPBACK)) {
				throw new InvalidParameterException(candidate + " no-type network address is invalid: self-addressing is disabled");
			}
		}	
	}
}
