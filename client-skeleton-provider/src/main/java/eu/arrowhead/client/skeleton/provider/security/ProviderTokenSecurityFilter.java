package eu.arrowhead.client.skeleton.provider.security;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import eu.arrowhead.common.token.TokenSecurityFilter;

public class ProviderTokenSecurityFilter extends TokenSecurityFilter {
	
	//=================================================================================================
	// members
	
	private PrivateKey myPrivateKey;
	private PublicKey authorizationPublicKey;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);		
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected PrivateKey getMyPrivateKey() {
		return myPrivateKey;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected PublicKey getAuthorizationPublicKey() {
		return authorizationPublicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public void setMyPrivateKey(final PrivateKey myPrivateKey) {
		this.myPrivateKey = myPrivateKey;
	}

	//-------------------------------------------------------------------------------------------------
	public void setAuthorizationPublicKey(final PublicKey authorizationPublicKey) {
		this.authorizationPublicKey = authorizationPublicKey;
	}	
}
