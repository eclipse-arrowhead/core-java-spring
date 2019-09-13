package eu.arrowhead.client.skeleton.provider.security;

import java.security.PrivateKey;
import java.security.PublicKey;

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
