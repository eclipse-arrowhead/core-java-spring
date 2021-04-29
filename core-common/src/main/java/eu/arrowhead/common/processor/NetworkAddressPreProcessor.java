package eu.arrowhead.common.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;

@Component
public class NetworkAddressPreProcessor {
	
	private final Logger logger = LogManager.getLogger(NetworkAddressPreProcessor.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String normalize(final String address) {
		logger.debug("normalize started...");
		final String candidate = address.toLowerCase().trim();
		
		// Simple string
		if (!candidate.contains(".") && !candidate.contains(":")) {
			return candidate;
		
		// IPv4 or domain name
		} else if(candidate.contains(".") && !candidate.contains(":")) {
			return candidate;
		
		// IPv6
		} else if (!candidate.contains(".") && candidate.contains(":")) {
			return normalizeIPv6(candidate);
			
		// IPv6-IPv4 hybrid
		} else {
			return normalizeIPv6IPv4Hybrid(candidate);
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private String normalizeIPv6(final String candidate) {
		logger.debug("normalizeIPv6 started...");
		final List<String> groups = new ArrayList<>(8);
		
		final String[] split = candidate.split(":");
		for (final String part : split) {
			String group = part;
			if (group.length() < 4) {
				for (int i = 0; i < (4 - part.length()); i++) {
					group = "0" + group;
				}
			} 
			groups.add(group);
		}
		
		if (candidate.startsWith("::") && !candidate.endsWith("::")) {
			final int size = groups.size();
			if (size > 8) {
				for (int i = 0; i < (size - 8); i++) {
					groups.remove(0);
				}
			}
			if (size < 8) {
				for (int i = 0; i < (8 - size); i++) {
					groups.add(0, "0000");
				}
			}
		}
		
		if (candidate.endsWith("::") && !candidate.startsWith("::")) {
			final int size = groups.size();
			if (size > 8) {
				for (int i = 0; i < (size - 8); i++) {
					groups.remove(groups.size() - 1);
				}
			}
			if (size < 8) {
				for (int i = 0; i < (8 - size); i++) {
					groups.add("0000");
				}
			}
		}
		
		if (candidate.endsWith("::") && candidate.startsWith("::")) {
			logger.debug("unprocessable abbreviation of IPv6. (leading and trailing '::') " + candidate);
			return candidate; //NetworkAddressVerifier will filter it out
		}
		
		String normalized = ""; 
		for (final String group : groups) {
			if (Utilities.isEmpty(normalized)) {
				normalized = group;
			} else {
				normalized = normalized + ":" + group;				
			}
		}
		return normalized;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String normalizeIPv6IPv4Hybrid(final String candidate) {
		logger.debug("normalizeIPv6IPv4Hybrid started...");
		
		final String[] split = candidate.split(":");
		final String ip4str = split[split.length - 1];
		final String[] ip4parts = ip4str.split("\\.");
		
		if (ip4parts.length != 4) {
			logger.debug("unprocessable IPv6-IPv4 hybrid. Invalid IPv4 part: " + candidate);
			return candidate; //NetworkAddressVerifier will filter it out
		}
		
		final StringBuilder ip4HexBuilder = new StringBuilder();
		
		for (int i = 0; i < 4; i++) {
			try {
				final int octet = Integer.parseInt(ip4parts[i]);
				final String hex = Integer.toHexString(octet);
				if (hex.length() == 1) {
					ip4HexBuilder.append("0");
				}
				ip4HexBuilder.append(hex);
				if (i == 1) {
					ip4HexBuilder.append(":");
				}
			} catch (final NumberFormatException ex) {
				logger.debug("unprocessable IPv6-IPv4 hybrid. Not number octet: " + candidate);
				return candidate; //NetworkAddressVerifier will filter it out
			}
		}
		
		final String converted = candidate.replace(ip4str, ip4HexBuilder.toString());
		return normalizeIPv6(converted);
	}
}
