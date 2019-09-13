package eu.arrowhead.client.skeleton.common.util;

public class ClientCommonConstants {
	
	//=================================================================================================
	// members

	public static final String CLIENT_SYSTEM_NAME = "client_system_name";
	public static final String $CLIENT_SYSTEM_NAME = "${" + CLIENT_SYSTEM_NAME + "}";
	public static final String CLIENT_SERVER_ADDRESS = "server.address";
	public static final String $CLIENT_SERVER_ADDRESS_WD = "${" + CLIENT_SERVER_ADDRESS + ": localhost" + "}";
	public static final String CLIENT_SERVER_PORT = "server.port";
	public static final String $CLIENT_SERVER_PORT_WD = "${" + CLIENT_SERVER_PORT + ": 8080" + "}";
	public static final String TOKEN_SECURITY_FILTER_ENABELD = "token.security.filter.enabeld";
	public static final String $TOKEN_SECURITY_FILTER_ENABELD_WD = "${" + TOKEN_SECURITY_FILTER_ENABELD + ": true" + "}";
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private ClientCommonConstants() {
		throw new UnsupportedOperationException();
	}
}
