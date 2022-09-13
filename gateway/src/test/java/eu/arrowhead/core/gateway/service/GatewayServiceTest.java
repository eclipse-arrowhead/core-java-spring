/********************************************************************************
 * Copyright (c) 2019 AITIA
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

package eu.arrowhead.core.gateway.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.jms.BytesMessage;
import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.core.gateway.thread.ConsumerSideServerSocketThread;
import eu.arrowhead.core.gateway.thread.ProviderSideSocketThreadHandler;
import eu.arrowhead.core.gateway.thread.SSLContextFactory;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@RunWith(SpringRunner.class)
public class GatewayServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatewayService testingObject;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private ApplicationContext appContext;
	
	@Mock
	private ConcurrentHashMap<String,ActiveSessionDTO> activeSessions;
	
	@Mock
	private ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads;
	
	@Mock
	private ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers;
	
	@Mock
	private ConcurrentLinkedQueue<Integer> availablePorts;
	
	private GatewayRelayClient relayClient;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatewayRelayClient.class, "relayClient");
		ReflectionTestUtils.setField(testingObject, "relayClient", relayClient);
		ReflectionTestUtils.setField(testingObject, "gatewaySocketTimeout", 60000);
		
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gateway.pub");
		final PublicKey publicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
		ReflectionTestUtils.setField(testingObject, "myPublicKey", publicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoCommonName() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(false);
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventCommonNameWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn(new Object());
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoPublicKey() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(false);
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventPublicKeyWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn("not a public key");
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoPrivateKey() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(false);
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventPrivateKeyWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn("not a private key");
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test
	public void testOnApplicationEventEverythingOK() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		});
		ReflectionTestUtils.setField(testingObject, "sslProps", new SSLProperties());
		
		testingObject.onApplicationEvent(null);
		
		final Object relayClient = ReflectionTestUtils.getField(testingObject, "relayClient");
		Assert.assertNotNull(relayClient);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRequestNull() {
		testingObject.connectProvider(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setRelay(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayAddressNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setAddress(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayAddressEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setAddress(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayPortNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayPortTooLow() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(-192);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayPortTooHigh() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(192426);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType("\r\t");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeInvalid1() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType("invalid");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeInvalid2() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType(RelayType.GATEKEEPER_RELAY.name());
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumer(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerNameNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setSystemName(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerNameEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setSystemName("");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerAddressNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setAddress(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerAddressEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setAddress(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerPortNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(null);
		
		testingObject.connectProvider(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerPortTooLow() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(-192);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerPortTooHigh() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(192426);
		
		testingObject.connectProvider(request);
	}
	
	// we skip the provider check tests because it uses the same method than consumer check
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerCloud(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudOperatorNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setOperator(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudOperatorEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setOperator(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudNameNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setName(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudNameEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setName("");
		
		testingObject.connectProvider(request);
	}
	
	// we skip the provider cloud check tests because it uses the same method than consumer cloud check
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderServiceDefinitionNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setServiceDefinition(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderServiceDefinitionEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerGWPublicKeyNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerGWPublicKey(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerGWPublicKeyEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerGWPublicKey("\n\t\r");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectProviderCannotConnectRelay() throws JMSException {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenThrow(new JMSException("test"));
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectProviderOtherRelayIssue() throws JMSException {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.initializeProviderSideRelay(any(Session.class), any(MessageListener.class))).thenThrow(new JMSException("test"));
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderEverythingOK() throws JMSException, InterruptedException {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		final MessageProducer producer = getTestMessageProducer();
		final MessageConsumer consumer = getTestMessageConsumer();
		
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		final ProviderSideRelayInfo info = new ProviderSideRelayInfo("peerName", "queueId", producer, producer, consumer, consumer);
		when(relayClient.initializeProviderSideRelay(any(Session.class), any(MessageListener.class))).thenReturn(info);
		when(activeSessions.put(any(String.class), any(ActiveSessionDTO.class))).thenReturn(null);
		when(appContext.getBean(SSLProperties.class)).thenReturn(getTestSSLPropertiesForThread());
		
		final boolean[] started = { false };
		new Thread() {
			@Override
			public void run() {
				final SSLProperties props = getTestSSLPropertiesForDummyProvider();
				final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(props);
				final SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
				try {
					final SSLServerSocket dummyServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(22032);
					started[0] = true;
					final Socket socket = dummyServerSocket.accept();
					socket.close();
				} catch (final IOException ex) {
					ex.printStackTrace();
				}
			}
		}.start();
		
		while (!started[0]) {
			Thread.sleep(1000);
		}
		Thread.sleep(1000);
		
		final GatewayProviderConnectionResponseDTO response = testingObject.connectProvider(request);
		
		Assert.assertEquals("queueId", response.getQueueId());
		Assert.assertEquals("peerName", response.getPeerName());
		final String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5r6P+DeDvSwMe5qWGlCoX94oNedSu7fdRpueJ9mNKfTgKwRHE8eOwVOf9By/LecfgRnlT+sf8qZbW3GG9jc+3xOPB+Q+NKJcVvLiU+nay6XZD/IbKaOcZz/pKWlQ+J6OoMQuoLSIA+IaVLuuP8Dlj8GJjKZyAxv643B16US2d6QxrkadQ/oKcnCVyBC/SnRAGALt0MHMTrY+MCU1dGqXb0i+aFmhcbMjBDYApni9bIUdOWy7+BlhnUdDATOenFBni94xZ8Or6cupYmKZLtv6rkvV/YkXM7N4m9avmTHGMU1BUVEbSjJ/6aqiTdBPaenHd6WNeFpgIoreG1vHTWpGeQIDAQAB"; 
		
		Assert.assertEquals(key, response.getProviderGWPublicKey());
		verify(activeProviderSideSocketThreadHandlers, times(1)).put(eq(info.getQueueId()), any(ProviderSideSocketThreadHandler.class));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerRequestNull() {
		testingObject.connectConsumer(null);
	}

	// we skip the relay check tests because it uses the same method than in connectProvider
	// we skip the consumer check tests because it uses the same method than in connectProvider
	// we skip the provider check tests because it uses the same method than in connectProvider
	// we skip the consumer cloud check tests because it uses the same method than in connectProvider
	// we skip the provider cloud check tests because it uses the same method than in connectProvider
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerServiceDefinitionNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerServiceDefinitionEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("          ");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerProviderGWPublicKeyNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setProviderGWPublicKey(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerProviderGWPublicKeyEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setProviderGWPublicKey("");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerQueueIdNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setQueueId(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerQueueIdEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setQueueId("");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerPeerNameNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setPeerName(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectConsumerPeerNameEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setPeerName("   ");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = UnavailableServerException.class)
	public void testConnectConsumerNoFreePort() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		
		when(availablePorts.poll()).thenReturn(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectConsumerCannotConnectRelay() throws JMSException {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		
		when(availablePorts.poll()).thenReturn(54321);
		when(activeSessions.put(any(String.class), any(ActiveSessionDTO.class))).thenReturn(null);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenThrow(new JMSException("test"));
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectConsumerOtherRelayIssue() throws JMSException {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		
		when(availablePorts.poll()).thenReturn(54321);
		when(activeSessions.put(any(String.class), any(ActiveSessionDTO.class))).thenReturn(null);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.initializeConsumerSideRelay(any(Session.class), any(MessageListener.class), any(String.class), any(String.class))).thenThrow(new JMSException("test"));
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerEverythingOK() throws JMSException {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		final MessageProducer producer = getTestMessageProducer();
		final MessageConsumer consumer = getTestMessageConsumer();
		
		when(availablePorts.poll()).thenReturn(54321);
		when(activeSessions.put(any(String.class), any(ActiveSessionDTO.class))).thenReturn(null);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.initializeConsumerSideRelay(any(Session.class), any(MessageListener.class), any(String.class), any(String.class))).thenReturn(new ConsumerSideRelayInfo(producer, producer, consumer, consumer));
		when(appContext.getBean(SSLProperties.class)).thenReturn(getTestSSLPropertiesForThread());
		
		final int serverPort = testingObject.connectConsumer(request);
		
		Assert.assertEquals(54321, serverPort);
		verify(activeConsumerSideSocketThreads, times(1)).put(eq(request.getQueueId()), any(ConsumerSideServerSocketThread.class));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionNullRequest() {
		testingObject.closeSession(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionNullPeerName() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName(null);
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionBlankPeerName() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("  ");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionNullQueueId() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId(null);
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionBlankQueueId() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("  ");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionNullRelay() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(null);
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionNullRelayAddress() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO(null, 1000, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionBlankRelayAddress() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("  ", 1000, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionNullRelayPort() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", null, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionRelayPortOutOfRangeMin() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionRelayPortOutOfRangeMax() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, null, true, true, "GATEWAY_RELAY"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionRelayNullType() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, null));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCloseSessionRelayInvalidType() {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "invalid"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCloseSessionCannotConnectRelay() throws JMSException {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "GATEWAY_RELAY"));
		
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenThrow(new JMSException("test"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCloseSessionOtherRelayIssue() throws JMSException {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setPeerName("test.peer.name");
		request.setQueueId("test-queue-id");
		request.setRelay(new RelayRequestDTO("test-address", 1000, null, true, true, "GATEWAY_RELAY"));
		
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.initializeControlRelay(any(Session.class), any(String.class), any(String.class))).thenThrow(new JMSException("test"));
		
		testingObject.closeSession(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionByPortActiveSessionNotFound() {
		final ActiveSessionDTO activeSessionDTO = new ActiveSessionDTO();
		activeSessionDTO.setConsumerServerSocketPort(1234);
		
		when(activeSessions.values()).thenReturn(List.of(activeSessionDTO));
		
		final String error = testingObject.closeSession(5678);
		
		Assert.assertEquals("Active session not found.", error);
		
		verify(activeSessions, times(1)).values();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionByPortProblemDuringClosing() throws JMSException {
		final ActiveSessionDTO activeSessionDTO = new ActiveSessionDTO();
		activeSessionDTO.setPeerName("peer");
		activeSessionDTO.setQueueId("queueId");
		activeSessionDTO.setConsumerServerSocketPort(1234);
		activeSessionDTO.setRelay(new RelayRequestDTO("localhost", 12345, null, true, false, RelayType.GATEWAY_RELAY.name()));
		
		when(activeSessions.values()).thenReturn(List.of(activeSessionDTO));
		when(relayClient.createConnection("localhost", 12345, true)).thenThrow(JMSException.class);
		
		final String error = testingObject.closeSession(1234);
		
		Assert.assertEquals("Error while trying to connect relay at localhost:12345", error);
		
		verify(activeSessions, times(1)).values();
		verify(relayClient, times(1)).createConnection("localhost", 12345, true);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionByPortOk() throws JMSException {
		final ActiveSessionDTO activeSessionDTO = new ActiveSessionDTO();
		activeSessionDTO.setPeerName("peer");
		activeSessionDTO.setQueueId("queueId");
		activeSessionDTO.setConsumerServerSocketPort(1234);
		activeSessionDTO.setRelay(new RelayRequestDTO("localhost", 12345, null, true, false, RelayType.GATEWAY_RELAY.name()));
		
		when(activeSessions.values()).thenReturn(List.of(activeSessionDTO));
		when(relayClient.createConnection("localhost", 12345, true)).thenReturn(getTestSession());
		when(relayClient.initializeControlRelay(any(Session.class), eq("peer"), eq("queueId"))).thenReturn(new ControlRelayInfo(getTestMessageProducer(), getTestMessageProducer()));
		doNothing().when(relayClient).sendCloseControlMessage(any(Session.class), any(MessageProducer.class), eq("queueId"));
		when(activeSessions.remove(anyString())).thenReturn(activeSessionDTO);
		doNothing().when(relayClient).closeConnection(any(Session.class));
		
		final String error = testingObject.closeSession(1234);
		
		Assert.assertNull(error);
		
		verify(activeSessions, times(1)).values();
		verify(relayClient, times(1)).createConnection("localhost", 12345, true);
		verify(relayClient, times(1)).initializeControlRelay(any(Session.class), eq("peer"), eq("queueId"));
		verify(relayClient, times(2)).sendCloseControlMessage(any(Session.class), any(MessageProducer.class), eq("queueId"));
		verify(activeSessions, times(1)).remove(anyString());
		verify(relayClient, times(1)).closeConnection(any(Session.class));
	}
		
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GatewayProviderConnectionRequestDTO getTestGatewayProviderConnectionRequestDTO() {
		final RelayRequestDTO relay = new RelayRequestDTO("localhost", 1234, null, false, false, RelayType.GATEWAY_RELAY.name());
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setAddress("abc.de");
		consumer.setPort(22001);
		consumer.setAuthenticationInfo("consAuth");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(22032);
		provider.setAuthenticationInfo("provAuth");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setName("testcloud1");
		consumerCloud.setOperator("aitia");
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("testcloud2");
		providerCloud.setOperator("elte");
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		
		return new GatewayProviderConnectionRequestDTO(relay, consumer, provider, consumerCloud, providerCloud, "test-service", publicKey);
	}

	//-------------------------------------------------------------------------------------------------
	private Session getTestSession() {
		return new Session() {

			//-------------------------------------------------------------------------------------------------
			public void close() throws JMSException {}
			public Queue createQueue(final String queueName) throws JMSException { return null;	}
			public Topic createTopic(final String topicName) throws JMSException { return null;	}
			public MessageConsumer createConsumer(final Destination destination) throws JMSException { return null; }
			public MessageProducer createProducer(final Destination destination) throws JMSException { return null;	}
			public TextMessage createTextMessage(final String text) throws JMSException { return null; }
			public BytesMessage createBytesMessage() throws JMSException { return null; }
			public MapMessage createMapMessage() throws JMSException { return null; }
			public Message createMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage(final Serializable object) throws JMSException { return null; }
			public StreamMessage createStreamMessage() throws JMSException { return null; }
			public TextMessage createTextMessage() throws JMSException { return null; }
			public boolean getTransacted() throws JMSException { return false; 	}
			public int getAcknowledgeMode() throws JMSException { return 0; }
			public void commit() throws JMSException {}
			public void rollback() throws JMSException {}
			public void recover() throws JMSException {}
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public void run() {}
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector) throws JMSException { return null; }
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) throws JMSException { return null;	}
			public QueueBrowser createBrowser(final Queue queue) throws JMSException { return null; }
			public QueueBrowser createBrowser(final Queue queue, final String messageSelector) throws JMSException { return null; }
			public TemporaryQueue createTemporaryQueue() throws JMSException { return null; }
			public TemporaryTopic createTemporaryTopic() throws JMSException { return null;	}
			public void unsubscribe(final String name) throws JMSException {}

		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private MessageProducer getTestMessageProducer() {
		return new MessageProducer() {
			
			//-------------------------------------------------------------------------------------------------
			public void setTimeToLive(final long timeToLive) throws JMSException {}
			public void setPriority(final int defaultPriority) throws JMSException {}
			public void setDisableMessageTimestamp(final boolean value) throws JMSException {}
			public void setDisableMessageID(final boolean value) throws JMSException {}
			public void setDeliveryMode(final int deliveryMode) throws JMSException {	}
			public void setDeliveryDelay(final long deliveryDelay) throws JMSException {}
			public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
			public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
			public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
			public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
			public void send(final Destination destination, final Message message, final CompletionListener completionListener) throws JMSException {}
			public void send(final Message message, final CompletionListener completionListener) throws JMSException {}
			public void send(final Destination destination, final Message message) throws JMSException {}
			public void send(final Message message) throws JMSException {}
			public long getTimeToLive() throws JMSException { return 0; }
			public int getPriority() throws JMSException { return 0; }
			public boolean getDisableMessageTimestamp() throws JMSException { return false;	}
			public boolean getDisableMessageID() throws JMSException { return false; }
			public Destination getDestination() throws JMSException { return null; }
			public int getDeliveryMode() throws JMSException { return 0; }
			public long getDeliveryDelay() throws JMSException { return 0; }
			public void close() throws JMSException {}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private MessageConsumer getTestMessageConsumer() {
		return new MessageConsumer() {
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public Message receiveNoWait() throws JMSException { return null; }
			public Message receive(final long timeout) throws JMSException { return null; }
			public Message receive() throws JMSException { return null; }
			public String getMessageSelector() throws JMSException {return null; }
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void close() throws JMSException {}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private GatewayConsumerConnectionRequestDTO getTestGatewayConsumerConnectionRequestDTO() {
		final RelayRequestDTO relay = new RelayRequestDTO("localhost", 1234, null, false, false, RelayType.GATEWAY_RELAY.name());
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setAddress("abc.de");
		consumer.setPort(22001);
		consumer.setAuthenticationInfo("consAuth");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("fgh.de");
		provider.setPort(22002);
		provider.setAuthenticationInfo("provAuth");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setName("testcloud1");
		consumerCloud.setOperator("aitia");
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("testcloud2");
		providerCloud.setOperator("elte");
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		
		return new GatewayConsumerConnectionRequestDTO(relay, "queueId", "peerName", publicKey, consumer, provider, consumerCloud, providerCloud, "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	private SSLProperties getTestSSLPropertiesForThread() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new ClassPathResource("certificates/truststore.p12");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		ReflectionTestUtils.setField(sslProps, "trustStorePassword", "123456");
		
		return sslProps;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SSLProperties getTestSSLPropertiesForDummyProvider() {
		final SSLProperties props = new SSLProperties();
		ReflectionTestUtils.setField(props, "sslEnabled", true);
		ReflectionTestUtils.setField(props, "keyStoreType", "PKCS12");
		ReflectionTestUtils.setField(props, "keyStore", new ClassPathResource("certificates/authorization.p12"));
		ReflectionTestUtils.setField(props, "keyStorePassword", "123456");
		ReflectionTestUtils.setField(props, "keyPassword", "123456");
		ReflectionTestUtils.setField(props, "trustStore", new ClassPathResource("certificates/truststore.p12"));
		ReflectionTestUtils.setField(props, "trustStorePassword", "123456");
		
		return props;
	}
}