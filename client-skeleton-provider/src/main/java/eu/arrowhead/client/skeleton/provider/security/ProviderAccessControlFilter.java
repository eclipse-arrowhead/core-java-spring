package eu.arrowhead.client.skeleton.provider.security;

import java.util.Map;

import eu.arrowhead.common.security.AccessControlFilter;

public class ProviderAccessControlFilter extends AccessControlFilter {
	
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		//TODO: implement here your custom access filter if any further
	}
}
