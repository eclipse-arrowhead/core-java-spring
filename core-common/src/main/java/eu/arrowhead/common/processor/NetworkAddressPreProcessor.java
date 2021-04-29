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
		String candidate = address.toLowerCase().trim();
		
		// Simple string
		if (!candidate.contains("\\.") && !candidate.contains(":")) {
			return candidate;
		
		// IPv4 or domain name
		} else if(candidate.contains("\\.") && !candidate.contains(":")) {
			return candidate;
		
		// IPv6
		} else if (!candidate.contains("\\.") && candidate.contains(":")) {
			return normalizeIPv6(candidate);
			
		// IPv6-IPv4 hybrid
		} else {
			return candidate;
		}
		
		//IPv
		// -migration IPv6 mixed (::ffff:192.0.2.128) to IPv6(::ffff:c000:280)
		// -add leading zeros
		// -colon follows colon (multiple also invalid)
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private String normalizeIPv6(final String candidate) {
		logger.debug("normalizeIPv6 started...");
		final List<String> groups = new ArrayList<>(8);
		
		String[] split = candidate.split(":");
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
			int size = groups.size();
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
			int size = groups.size();
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
			logger.debug("unprocessable abbreviation of IPv6. (leading and trailing '::')");
			return candidate; //NetworkAddressVerifier will filter it out
		}
		
		String normalized = ""; 
		for (String group : groups) {
			if (Utilities.isEmpty(normalized)) {
				normalized = group;
			} else {
				normalized = normalized + ":" + group;				
			}
		}
		return normalized;
	}
}
