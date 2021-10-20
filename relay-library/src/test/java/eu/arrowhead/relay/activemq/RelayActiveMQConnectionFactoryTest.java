/* Copyright (c) 2021 AITIA
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

package eu.arrowhead.relay.activemq;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.util.InMemoryResource;

import eu.arrowhead.common.SSLProperties;

public class RelayActiveMQConnectionFactoryTest {

	//=================================================================================================
	// members
	
	private RelayActiveMQConnectionFactory testingObject;
	
	private SSLProperties sslProps;
	private ActiveMQConnectionFactory tcpConnectionFactory;
	private ActiveMQSslConnectionFactory sslConnectionFactory; 
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		sslProps = Mockito.mock(SSLProperties.class);
		tcpConnectionFactory = Mockito.mock(ActiveMQConnectionFactory.class);
		sslConnectionFactory = Mockito.mock(ActiveMQSslConnectionFactory.class);

		testingObject = new RelayActiveMQConnectionFactory(null, -1, sslProps);
		testingObject.tcpConnectionFactory = tcpConnectionFactory;
		testingObject.sslConnectionFactory = sslConnectionFactory;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTCPConnectionHostNull() throws Exception {
		try {
			testingObject.createTCPConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTCPConnectionHostEmpty() throws Exception {
		try {
			testingObject.setHost("");
			testingObject.createTCPConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTCPConnectionPortTooLow() throws Exception {
		try {
			testingObject.setHost("localhost");
			testingObject.createTCPConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTCPConnectionPortTooHigh() throws Exception {
		try {
			testingObject.setHost("localhost");
			testingObject.setPort(123456);
			testingObject.createTCPConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateTCPConnectionOk() throws Exception {
		doNothing().when(tcpConnectionFactory).setBrokerURL(anyString());
		doNothing().when(tcpConnectionFactory).setClientID(anyString());
		when(tcpConnectionFactory.createConnection()).thenReturn(getTestConnection());
		
		testingObject.setHost("localhost");
		testingObject.setPort(12345);
		testingObject.createTCPConnection();
		
		verify(tcpConnectionFactory, times(1)).setBrokerURL(anyString());
		verify(tcpConnectionFactory, times(1)).setClientID(anyString());
		verify(tcpConnectionFactory, times(1)).setClientID(isNull());
		verify(tcpConnectionFactory, times(1)).createConnection();
		verify(sslConnectionFactory, never()).createConnection();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionHostNull() throws Exception {
		try {
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionHostEmpty() throws Exception {
		try {
			testingObject.setHost("");
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionPortTooLow() throws Exception {
		try {
			testingObject.setHost("localhost");
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionPortTooHigh() throws Exception {
		try {
			testingObject.setHost("localhost");
			testingObject.setPort(123456);
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionSSLPropertiesNull() throws Exception {
		try {
			testingObject.setHost("localhost");
			testingObject.setPort(12345);
			testingObject.setSslProps(null);
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("SSL properties object is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionKeyStoreNull() throws Exception {
		when(sslProps.getKeyStore()).thenReturn(null);
		
		try {
			testingObject.setHost("localhost");
			testingObject.setPort(12345);
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Key store is null.", ex.getMessage());
			
			verify(sslProps, times(1)).getKeyStore();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSSLConnectionTrustStoreNull() throws Exception {
		when(sslProps.getKeyStore()).thenReturn(new InMemoryResource("abcd"));
		when(sslProps.getTrustStore()).thenReturn(null);
		
		try {
			testingObject.setHost("localhost");
			testingObject.setPort(12345);
			testingObject.createSSLConnection();
		} catch (final Exception ex) {
			Assert.assertEquals("Trust store is null.", ex.getMessage());
			
			verify(sslProps, times(1)).getKeyStore();
			verify(sslProps, times(1)).getTrustStore();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateSSLConnectionOk() throws Exception {
		doNothing().when(sslConnectionFactory).setBrokerURL(anyString());
		doNothing().when(sslConnectionFactory).setClientID(anyString());
		when(sslProps.getKeyStoreType()).thenReturn("pkcs12");
		when(sslProps.getKeyStore()).thenReturn(new FileSystemResource("a.txt"));
		when(sslProps.getKeyStorePassword()).thenReturn("abcd");
		when(sslProps.getKeyPassword()).thenReturn("abcd");
		when(sslProps.getTrustStore()).thenReturn(new FileSystemResource("a.txt"));
		when(sslProps.getTrustStorePassword()).thenReturn("abcd");
		when(sslConnectionFactory.createConnection()).thenReturn(getTestConnection());

		testingObject.setHost("localhost");
		testingObject.setPort(12345);
		testingObject.createSSLConnection();
		
		verify(sslConnectionFactory, times(1)).setBrokerURL(anyString());
		verify(sslConnectionFactory, times(1)).setClientID(anyString());
		verify(sslProps, times(2)).getKeyStoreType();
		verify(sslProps, times(2)).getKeyStore();
		verify(sslProps, times(1)).getKeyStorePassword();
		verify(sslProps, times(1)).getKeyPassword();
		verify(sslProps, times(2)).getTrustStore();
		verify(sslProps, times(1)).getTrustStorePassword();
		verify(sslConnectionFactory, times(1)).setClientID(isNull());
		verify(sslConnectionFactory, times(1)).createConnection();
		verify(tcpConnectionFactory, never()).createConnection();

	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Connection getTestConnection() {
		return new Connection() {
			public void stop() throws JMSException {}
			public void start() throws JMSException {}
			public void setExceptionListener(final ExceptionListener listener) throws JMSException {}
			public void setClientID(final String clientID) throws JMSException {}
			public ConnectionMetaData getMetaData() throws JMSException { return null; }
			public ExceptionListener getExceptionListener() throws JMSException { return null; }
			public String getClientID() throws JMSException { return null; }
			public ConnectionConsumer createSharedDurableConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException { return null; }
			public ConnectionConsumer createSharedConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException { return null; }
			public Session createSession(final boolean transacted, final int acknowledgeMode) throws JMSException { return null; }
			public Session createSession(final int sessionMode) throws JMSException { return null; }
			public Session createSession() throws JMSException { return null; }
			public ConnectionConsumer createDurableConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException { return null; }
			public ConnectionConsumer createConnectionConsumer(final Destination destination, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException { return null; }
			public void close() throws JMSException {}
		};
	}
}