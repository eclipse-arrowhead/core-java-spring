package eu.arrowhead.core.qos.manager;

import eu.arrowhead.core.qos.manager.impl.QoSVerificationParameters;

public interface QoSVerifier {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean verify(final QoSVerificationParameters parameters);
}