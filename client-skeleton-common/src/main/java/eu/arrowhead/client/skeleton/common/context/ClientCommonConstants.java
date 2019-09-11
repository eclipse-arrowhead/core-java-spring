package eu.arrowhead.client.skeleton.common.context;

public class ClientCommonConstants {
	
	//=================================================================================================
	// members

	public static final String CLIENT_SYSTEM_NAME = "client_system_name";
	public static final String $CLIENT_SYSTEM_NAME = "${" + CLIENT_SYSTEM_NAME + "}";
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private ClientCommonConstants() {
		throw new UnsupportedOperationException();
	}
}
