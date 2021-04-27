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
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;

@Component
public class NetworkAddressVerifier {

	//=================================================================================================
	// members
	
	public static final String IPV4_REGEX_STRING = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
	public static final String IPV6_REGEX_STRING = "\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z";
	public static final String DOMAIN_NAME_REGEX_STRING = "^((?!-)[A-Za-z0-9-]{1, 63}(?<!-)\\.)+[A-Za-z]{2, 6}$";
	
	private static final Pattern ipv4Pattern;
	private static final Pattern ipv6Pattern;
	private static final Pattern domainNamePattern;
	
	static {
		ipv4Pattern = Pattern.compile(IPV4_REGEX_STRING);
		ipv6Pattern = Pattern.compile(IPV6_REGEX_STRING);
		domainNamePattern = Pattern.compile(DOMAIN_NAME_REGEX_STRING);
	}
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	// TODO from props
	private boolean allowSelfAddressing;
	
	// TODO from props
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
		}
		if (ipv6Pattern.matcher(candidate).matches()) {
			verifyIPV6(candidate);
		}
		if (domainNamePattern.matcher(candidate).matches()) {
			verifyDomainName(candidate);
		}
		
		verifyNoType(candidate);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void verifyIPV4(final String candidate) {
		logger.debug("verifyIPV4 started...");
		
		if (!allowSelfAddressing) {			
			// Filter out IP placeholder(default route) (0.0.0.0)
			if (candidate.equalsIgnoreCase("0.0.0.0")) {
				throw new InvalidParameterException(candidate + " ipv4 network address is invalid: self-addressing is disabled");
			}
			
			// Filter out loopback (127.0.0.0 - 127.255.255.255)
			if (candidate.startsWith("127")) {
				throw new InvalidParameterException(candidate + " ipv4 network address is invalid: self-addressing is disabled");
			}
		}
		
		if (!allowNonRoutableAddressing) {
			// Filter out APIPA (Automatic Private IP Address: 169.254.?.?)
			if (candidate.startsWith("169.254")) {
				throw new InvalidParameterException(candidate + " ipv4 network address is invalid: non-routable-addressing is disabled");
			}
		}
		
		// Filter out local broadcast (255.255.255.255)
		if (candidate.equalsIgnoreCase("255.255.255.255")) {
			throw new InvalidParameterException(candidate + " ipv4 network address is invalid: local broadcast address is denied");
		}
		
		// Could not filter out directed broadcast (cannot determine it without the subnet mask)

		// Filter out multicast (Class D: 224.0.0.0 - 239.255.255.255)
		final String[] octets = candidate.split("\\.");
		final Integer firstOctet = Integer.valueOf(octets[0]);
		if (firstOctet >= 224 && firstOctet <= 239) {
			throw new InvalidParameterException(candidate + " ipv4 network address is invalid: multicast addresses are denied");
		}		
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyIPV6(final String candidate) {
		logger.debug("verifyIPV6 started...");
		
		if (!allowSelfAddressing) {
			// Filter out unspecified address (0:0:0:0:0:0:0:0) and loopback address (0:0:0:0:0:0:0:1)			
			if (candidate.equalsIgnoreCase("0:0:0:0:0:0:0:0") || candidate.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
				throw new InvalidParameterException(candidate + " ipv6 network address is invalid: self-addressing is disabled");
			}			
		}

		if (!allowNonRoutableAddressing) {
			// Filter out link-local addresses (prefix fe80)
			if (candidate.startsWith("fe80")) {
				throw new InvalidParameterException(candidate + " ipv6 network address is invalid: non-routable-addressing is disabled");
			}
		}
		
		// Filter out multicast (prefix ff)
		if (candidate.startsWith("ff")) {
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
		
		if (cnVerifier.isValid(candidate)) {
			throw new InvalidParameterException(candidate + " no-type network address is invalid: only letters (english alphabet), numbers and dash (-) are allowed and have to start with a letter (also cannot end with dash).");
		}
		
		if (!allowSelfAddressing) {
			// Filter out 'localhost' and 'loopback'
			if (candidate.equalsIgnoreCase("localhost") || candidate.equalsIgnoreCase("loopback")) {
				throw new InvalidParameterException(candidate + " no-type network address is invalid: self-addressing is disabled");
			}
		}
		
		// TODO clarify the effects to docker		
	}
}
