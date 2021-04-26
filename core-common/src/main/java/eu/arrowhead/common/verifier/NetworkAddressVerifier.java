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
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;

@Component
public class NetworkAddressVerifier {

	//=================================================================================================
	// members
	
	public static final String IPV4_REGEX_STRING = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
	public static final String IPV6_REGEX_STRING = "\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z";
	public static final String DOMAIN_NAME_REGEX_STRING = "^((?!-)[A-Za-z0-9-]{1, 63}(?<!-)\\.)+[A-Za-z]{2, 6}$"; //https://www.geeksforgeeks.org/how-to-validate-a-domain-name-using-regular-expression/
	
	private static final Pattern ipv4Pattern;
	private static final Pattern ipv6Pattern;
	private static final Pattern domainNamePattern;
	
	static {
		ipv4Pattern = Pattern.compile(IPV4_REGEX_STRING);
		ipv6Pattern = Pattern.compile(IPV6_REGEX_STRING);
		domainNamePattern = Pattern.compile(DOMAIN_NAME_REGEX_STRING);
	}
	
	private final Logger logger = LogManager.getLogger(NetworkAddressVerifier.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean isValid(final String address) { //TODO junit
		if (Utilities.isEmpty(address)) {
			logger.debug("Newtwork address is empty");
			return false;
		}
		
		final String candidate = address.trim();
		if (ipv4Pattern.matcher(candidate).matches()) {
			return validateIPV4(candidate);
		}
		if (ipv6Pattern.matcher(candidate).matches()) {
			return validateIPV6(candidate);
		}
		if (domainNamePattern.matcher(candidate).matches()) {
			return validateDomainName(candidate);
		}
		
		return validate(candidate);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private boolean validateIPV4(final String address) {
		//TODO
		
		// Filter out IP placeholder(default route) (0.0.0.0)
		// Filter out loopback (127.0.0.0 - 127.255.255.255)
		// Filter out APIPA (Automatic Private IP Address: 169.254.?.?)
		// Filter out local broadcast (255.255.255.255)
		// Filter out directed broadcast (cannot determine it without the subnet mask)
		// Filter out multicast (Class D: 244.0.0.0 - 239.255.255.255)
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean validateIPV6(final String address) {
		//TODO
		// Filter out anycast
		// Filter out multicast (prefix ff00::/8)
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean validateDomainName(final String address) {
		//TODO
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean validate(final String address) {
		//TODO
		// Filter out 'localhost'
		// Filter out 'loopback'
		return false;
	}
}
