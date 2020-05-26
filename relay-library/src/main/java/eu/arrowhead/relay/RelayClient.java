package eu.arrowhead.relay;

import javax.jms.JMSException;
import javax.jms.Session;

public interface RelayClient {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Session createConnection(final String host, final int port, final boolean secure) throws JMSException;
	public void closeConnection(final Session session);
	public boolean isConnectionClosed(final Session session);
}