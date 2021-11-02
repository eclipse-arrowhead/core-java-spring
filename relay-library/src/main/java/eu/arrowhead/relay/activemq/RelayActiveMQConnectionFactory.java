/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;

public class RelayActiveMQConnectionFactory {
	
	//=================================================================================================
	// members
	
	private static final String TCP = "tcp";
	private static final String SSL = "ssl";

	private static final int CLIENT_ID_LENGTH = 16;
	
	protected String host;
	protected int port;
	protected SSLProperties sslProps;
	
	protected ActiveMQConnectionFactory tcpConnectionFactory = new ActiveMQConnectionFactory();
	protected ActiveMQSslConnectionFactory sslConnectionFactory = new ActiveMQSslConnectionFactory(); 
	
	private static final Logger logger = LogManager.getLogger(RelayActiveMQConnectionFactory.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public RelayActiveMQConnectionFactory() {}

	//-------------------------------------------------------------------------------------------------
	public RelayActiveMQConnectionFactory(final String host, final int port, final SSLProperties sslProps) {
		this.host = host;
		this.port = port;
		this.sslProps = sslProps;
	}

	//-------------------------------------------------------------------------------------------------
	public String getHost() { return host; }
	public int getPort() { return port; }
	public SSLProperties getSslProps() { return sslProps; }

	//-------------------------------------------------------------------------------------------------
	public void setHost(final String host) { this.host = host; }
	public void setPort(final int port) { this.port = port; }
	public void setSslProps(final SSLProperties sslProps) { this.sslProps = sslProps; }
	
	//-------------------------------------------------------------------------------------------------
	public Connection createTCPConnection() throws JMSException {
		logger.debug("createTCPConnection started...");
		
		Assert.isTrue(!Utilities.isEmpty(host), "Host is null or blank.");
		Assert.isTrue(port > CommonConstants.SYSTEM_PORT_RANGE_MIN && port < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Port is invalid.");

		final UriComponents uri = Utilities.createURI(TCP, host, port, null);
		tcpConnectionFactory.setBrokerURL(uri.toUriString());
		tcpConnectionFactory.setClientID(RandomStringUtils.randomAlphanumeric(CLIENT_ID_LENGTH));
		final Connection connection = tcpConnectionFactory.createConnection();
		tcpConnectionFactory.setClientID(null);

		return connection;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Connection createSSLConnection() throws JMSException {
		logger.debug("createSSLConnection started...");
		
		Assert.isTrue(!Utilities.isEmpty(host), "Host is null or blank.");
		Assert.isTrue(port > CommonConstants.SYSTEM_PORT_RANGE_MIN && port < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Port is invalid.");
		Assert.notNull(sslProps, "SSL properties object is null.");
		Assert.notNull(sslProps.getKeyStore(), "Key store is null.");
		Assert.notNull(sslProps.getTrustStore(), "Trust store is null.");
		
		final UriComponents uri = Utilities.createURI(SSL, host, port, null);
		try {
			sslConnectionFactory.setBrokerURL(uri.toUriString());
			sslConnectionFactory.setClientID(RandomStringUtils.randomAlphanumeric(CLIENT_ID_LENGTH));
			sslConnectionFactory.setKeyStoreType(sslProps.getKeyStoreType());
			sslConnectionFactory.setKeyStore(sslProps.getKeyStore().getURI().toString());
			sslConnectionFactory.setKeyStorePassword(sslProps.getKeyStorePassword());
			sslConnectionFactory.setKeyStoreKeyPassword(sslProps.getKeyPassword());
			sslConnectionFactory.setTrustStoreType(sslProps.getKeyStoreType());
			sslConnectionFactory.setTrustStore(sslProps.getTrustStore().getURI().toString());
			sslConnectionFactory.setTrustStorePassword(sslProps.getTrustStorePassword());
			
			final Connection connection = sslConnectionFactory.createConnection();
			sslConnectionFactory.setClientID(null);
			
			return connection;
		} catch (final JMSException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage());
			logger.debug("Stacktrace: ", ex);
			throw new JMSException("Error while creating SSL connection: " + ex.getMessage());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Connection createConnection(final boolean secure) throws JMSException {
		return secure ? createSSLConnection() : createTCPConnection();
	}
}