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

package eu.arrowhead.relay.gatekeeper.activemq;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Set;

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

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GeneralAdvertisementMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.GatekeeperMain;
import eu.arrowhead.relay.RelayCryptographer;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClientFactory;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayRequest;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
public class ActiveMQGatekeeperRelayClientTest {
	
	//=================================================================================================
	// members
	
	private PublicKey clientPublicKey;
	private PublicKey otherPublicKey;
	private PrivateKey otherPrivateKey;
	
	@Autowired
	private SSLProperties sslProperties;
	
	private ActiveMQGatekeeperRelayClient testObject;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.pub");
		clientPublicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
		
		final InputStream otherPublicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		otherPublicKey = Utilities.getPublicKeyFromPEMFile(otherPublicKeyInputStream);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.p12"), "123456".toCharArray());
		final PrivateKey clientPrivateKey = Utilities.getPrivateKey(keystore, "123456");
		
		testObject = new ActiveMQGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", clientPublicKey, clientPrivateKey, sslProperties, 1000);
		
		final KeyStore keystore2 = KeyStore.getInstance("PKCS12");
		keystore2.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.p12"), "123456".toCharArray());
		otherPrivateKey = Utilities.getPrivateKey(keystore2, "123456");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		GatekeeperRelayClientFactory.createGatekeeperRelayClient(null, null, null, null, 0, Set.of());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		GatekeeperRelayClientFactory.createGatekeeperRelayClient(" ", null, null, null, 0, Set.of());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		GatekeeperRelayClientFactory.createGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", null, null, null, 0, Set.of());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		GatekeeperRelayClientFactory.createGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", clientPublicKey, null, null, 0, Set.of());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSSLPropertiesNull() {
		GatekeeperRelayClientFactory.createGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", clientPublicKey, otherPrivateKey, null, 0, Set.of());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostNull() throws JMSException {
		testObject.createConnection(null, 42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostEmpty() throws JMSException {
		testObject.createConnection("\n", 42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooLow() throws JMSException {
		testObject.createConnection("localhost", -42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooHigh() throws JMSException {
		testObject.createConnection("localhost", 420000, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testCreateConnectionFailed() throws JMSException {
		testObject.createConnection("invalid.address.dafafasdasdfgf.qq", 42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSubsrcibeGeneralAdvertisementTopicSessionNull() throws JMSException {
		testObject.subscribeGeneralAdvertisementTopic(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetGeneralAdvertisementMessageMessageNull() throws JMSException {
		testObject.getGeneralAdvertisementMessage(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testGetGeneralAdvertisementMessageNotTextMessage() throws JMSException {
		testObject.getGeneralAdvertisementMessage(new ActiveMQObjectMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessageOtherRecipient() throws JMSException {
		final GeneralAdvertisementMessageDTO dto = new GeneralAdvertisementMessageDTO("gatekeeper.testcloud1.aitia.arrowhead.eu", "abcd", "gatekeeper.testcloud3.elte.arrowhead.eu", "1234");
		final String json = Utilities.toJson(dto);
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText(json);
		final GeneralAdvertisementMessageDTO result = testObject.getGeneralAdvertisementMessage(msg);
		Assert.assertNull(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessageMessageForMe() throws JMSException {
		final GeneralAdvertisementMessageDTO dto = getTestGeneralAdvertisementMessageDTOWithEncryptedSessionId();
		final String json = Utilities.toJson(dto);
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText(json);
		final GeneralAdvertisementMessageDTO result = testObject.getGeneralAdvertisementMessage(msg);
		Assert.assertEquals("human-readable-session-id", result.getSessionId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSessionNull() throws JMSException {
		testObject.sendAcknowledgementAndReturnRequest(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestGAMessageNull() throws JMSException {
		testObject.sendAcknowledgementAndReturnRequest(getTestSession(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSenderPublicKeyNull() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithEncryptedSessionId();
		gaMessage.setSenderPublicKey(null);
		testObject.sendAcknowledgementAndReturnRequest(getTestSession(), gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSenderPublicKeyEmpty() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithEncryptedSessionId();
		gaMessage.setSenderPublicKey(" ");
		testObject.sendAcknowledgementAndReturnRequest(getTestSession(), gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSessionIdNull() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithEncryptedSessionId();
		gaMessage.setSessionId(null);
		testObject.sendAcknowledgementAndReturnRequest(getTestSession(), gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSessionIdEmpty() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithEncryptedSessionId();
		gaMessage.setSessionId("\r\n\t ");
		testObject.sendAcknowledgementAndReturnRequest(getTestSession(), gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendAcknowledgementAndReturnRequestNoRequest() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final GatekeeperRelayRequest result = testObject.sendAcknowledgementAndReturnRequest(getTestSession(), gaMessage);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidMessageClass() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(new ActiveMQObjectMessage());
		testObject.sendAcknowledgementAndReturnRequest(testSession, gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidMessageType() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final RelayCryptographer senderCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedMsg = senderCryptographer.encodeRelayMessage("invalid", "sessionId", new String("does not matter"), clientPublicKey);
		final TextMessage textMsg = new ActiveMQTextMessage();
		textMsg.setText(encryptedMsg);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(textMsg);
		testObject.sendAcknowledgementAndReturnRequest(testSession, gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidSessionId() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final RelayCryptographer senderCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedMsg = senderCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "otherSessionId", new String("does not matter"), clientPublicKey);
		final TextMessage textMsg = new ActiveMQTextMessage();
		textMsg.setText(encryptedMsg);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(textMsg);
		testObject.sendAcknowledgementAndReturnRequest(testSession, gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendAcknowledgementAndReturnRequestMessageTypePayloadMismatch() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final RelayCryptographer senderCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedMsg = senderCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", new String("mismatched payload"), clientPublicKey);
		final TextMessage textMsg = new ActiveMQTextMessage();
		textMsg.setText(encryptedMsg);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(textMsg);
		testObject.sendAcknowledgementAndReturnRequest(testSession, gaMessage);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testSendAcknowledgementAndReturnRequestTryToAccessPayloadViaWrongMethod() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final RelayCryptographer senderCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedMsg = senderCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", new GSDPollRequestDTO(), clientPublicKey);
		final TextMessage textMsg = new ActiveMQTextMessage();
		textMsg.setText(encryptedMsg);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(textMsg);
		final GatekeeperRelayRequest result = testObject.sendAcknowledgementAndReturnRequest(testSession, gaMessage);
		result.getICNProposalRequest();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendAcknowledgementAndReturnRequestEverythingsOK() throws JMSException {
		final GeneralAdvertisementMessageDTO gaMessage = getTestGeneralAdvertisementMessageDTOWithSessionId("sessionId");
		final RelayCryptographer senderCryptographer = new RelayCryptographer(otherPrivateKey);
		final GSDPollRequestDTO payload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		payload.setRequestedService(serviceQueryFormDTO);
		final String encryptedMsg = senderCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", payload, clientPublicKey);
		final TextMessage textMsg = new ActiveMQTextMessage();
		textMsg.setText(encryptedMsg);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(textMsg);
		final GatekeeperRelayRequest result = testObject.sendAcknowledgementAndReturnRequest(testSession, gaMessage);
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getAnswerSender());
		Assert.assertNotNull(result.getPeerPublicKey());
		Assert.assertNotNull(result.getMessageType());
		Assert.assertNotNull(result.getSessionId());
		Assert.assertEquals(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, result.getMessageType());
		Assert.assertEquals("sessionId", result.getSessionId());
		final GSDPollRequestDTO request = result.getGSDPollRequest();
		Assert.assertNotNull(request);
		Assert.assertNotNull("test-service", request.getRequestedService().getServiceDefinitionRequirement());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseSessionNull() throws JMSException {
		testObject.sendResponse(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseRequestNull() throws JMSException {
		testObject.sendResponse(getTestSession(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseResponsePayloadNull() throws JMSException {
		testObject.sendResponse(getTestSession(), getTestGatekeeperRelayRequest("sessionId", CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, new String("does not matter")), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendResponseResponseInvalidMessageType() throws JMSException {
		testObject.sendResponse(getTestSession(), getTestGatekeeperRelayRequest("sessionId", "invalid", new String("does not matter")), new String("nor this one"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSendResponseResponseMessageTypeResponsePayloadMismatch() throws JMSException {
		testObject.sendResponse(getTestSession(), getTestGatekeeperRelayRequest("sessionId", CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, new String("does not matter")), new String("nor this one"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testSendResponseResponseNormalResponse() throws JMSException {
		final GSDPollResponseDTO responsePayload = new GSDPollResponseDTO();
		responsePayload.setRequiredServiceDefinition("test-service");
		testObject.sendResponse(getTestSession(), getTestGatekeeperRelayRequest("sessionId", CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, new String("does not matter")), responsePayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testSendResponseResponseErrorResponse() throws JMSException {
		final ErrorMessageDTO errorPayload = new ErrorMessageDTO("error", 401, ExceptionType.ARROWHEAD, "out there");
		testObject.sendResponse(getTestSession(), getTestGatekeeperRelayRequest("sessionId", CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, new String("does not matter")), errorPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementSessionNull() throws JMSException {
		testObject.publishGeneralAdvertisement(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientCNNull() throws JMSException {
		testObject.publishGeneralAdvertisement(getTestSession(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientCNEmpty() throws JMSException {
		testObject.publishGeneralAdvertisement(getTestSession(), " ", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientPublicKeyNull() throws JMSException {
		testObject.publishGeneralAdvertisement(getTestSession(), "gatekeeper.testcloud1.aitia.arrowhead.eu", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientPublicKeyEmpty() throws JMSException {
		testObject.publishGeneralAdvertisement(getTestSession(), "gatekeeper.testcloud1.aitia.arrowhead.eu", " ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishGeneralAdvertisementNoAcknowledgement() throws JMSException {
		final String recipientPublicKey = Base64.getEncoder().encodeToString(otherPublicKey.getEncoded());
		final GeneralAdvertisementResult result = testObject.publishGeneralAdvertisement(getTestSession(), "gatekeeper.testcloud1.aitia.arrowhead.eu", recipientPublicKey);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testPublishGeneralAdvertisementInvalidAcknowledgementClass() throws JMSException {
		final String recipientPublicKey = Base64.getEncoder().encodeToString(otherPublicKey.getEncoded());
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(new ActiveMQObjectMessage());
		testObject.publishGeneralAdvertisement(testSession, "gatekeeper.testcloud1.aitia.arrowhead.eu", recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testPublishGeneralAdvertisementInvalidAcknowledgementMessageType() throws JMSException {
		final String recipientPublicKey = Base64.getEncoder().encodeToString(otherPublicKey.getEncoded());
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encodedMessage = recipientCryptographer.encodeRelayMessage("invalid", "sessionId", null, clientPublicKey); 
		final TextMessage ackMsg = new ActiveMQTextMessage();
		ackMsg.setText(encodedMessage);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(ackMsg);
		testObject.publishGeneralAdvertisement(testSession, "gatekeeper.testcloud1.aitia.arrowhead.eu", recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testPublishGeneralAdvertisementInvalidAcknowledgementSessionId() throws JMSException {
		final String recipientPublicKey = Base64.getEncoder().encodeToString(otherPublicKey.getEncoded());
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encodedMessage = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACK, "sessionId", null, clientPublicKey); 
		final TextMessage ackMsg = new ActiveMQTextMessage();
		ackMsg.setText(encodedMessage);
		final TestSession testSession = getTestSession();
		testSession.setConsumerWithMessage(ackMsg);
		testObject.publishGeneralAdvertisement(testSession, "gatekeeper.testcloud1.aitia.arrowhead.eu", recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseSessionNull() throws JMSException {
		testObject.sendRequestAndReturnResponse(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseAdvertisementResponseNull() throws JMSException {
		testObject.sendRequestAndReturnResponse(getTestSession(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseRequestPayloadNull() throws JMSException {
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(null, "sessionId");		
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseInvalidRequestPayloadClass() throws JMSException {
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(null, "sessionId");		
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, new String("not a valid request"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendRequestAndReturnResponseNoResponse() throws JMSException {
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(null, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		final GatekeeperRelayResponse result = testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendRequestAndReturnResponseInvalidResponseClass() throws JMSException {
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(new ActiveMQObjectMessage(), "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendRequestAndReturnResponseInvalidMessageType() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL, "sessionId", new String("does not matter"), clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendRequestAndReturnResponseInvalidSessionId() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "otherSessionId", new String("does not matter"), clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseInvalidErrorResponse() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		
		final class DummyError {
			private int errorCode = 1;
			private int somethingElse = 2;
			@SuppressWarnings("unused") public int getErrorCode() { return errorCode; }
			@SuppressWarnings("unused")	public int getSomethingElse() { return somethingElse; }
			@SuppressWarnings("unused")	public void setErrorCode(final int errorCode) { this.errorCode = errorCode; }
			@SuppressWarnings("unused")	public void setSomethingElse(final int somethingElse) { this.somethingElse = somethingElse; }
		}
		
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", new DummyError(), clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseErrorResponseUnknownType() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final ErrorMessageDTO error = new ErrorMessageDTO("an error", 42, null, null);
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", error, clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSendRequestAndReturnResponseErrorResponseTypeInvalidParameter() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final ErrorMessageDTO error = new ErrorMessageDTO("an error", 42, ExceptionType.INVALID_PARAMETER, null);
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", error, clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseNormalResponseInvalidResponseClass() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", new String("invalid"), clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testSendRequestAndReturnResponseNormalResponseTryingToAccessTheResponseViaWrongMethod() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final GSDPollResponseDTO response = new GSDPollResponseDTO();
		response.setRequiredServiceDefinition("test-service");
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", response, clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		final GatekeeperRelayResponse result = testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
		result.getICNProposalResponse();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendRequestAndReturnResponseNormalResponseEverythingIsOK() throws JMSException {
		final RelayCryptographer recipientCryptographer = new RelayCryptographer(otherPrivateKey);
		final GSDPollResponseDTO response = new GSDPollResponseDTO();
		response.setRequiredServiceDefinition("test-service");
		final String encryptedResponse = recipientCryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "sessionId", response, clientPublicKey);
		final TextMessage respMsg = new ActiveMQTextMessage();
		respMsg.setText(encryptedResponse);
		final GeneralAdvertisementResult advertisementResponse = getTestGeneralAdvertisementResult(respMsg, "sessionId");		
		final GSDPollRequestDTO requestPayload = new GSDPollRequestDTO();
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		requestPayload.setRequestedService(serviceQueryFormDTO);
		final GatekeeperRelayResponse result = testObject.sendRequestAndReturnResponse(getTestSession(), advertisementResponse, requestPayload);
		Assert.assertNotNull(result);
		Assert.assertEquals(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, result.getMessageType());
		Assert.assertEquals("sessionId", result.getSessionId());
		Assert.assertNotNull(result.getGSDPollResponse());
		Assert.assertEquals("test-service", result.getGSDPollResponse().getRequiredServiceDefinition());
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageDTO getTestGeneralAdvertisementMessageDTOWithEncryptedSessionId() {
		final String sessionId = "human-readable-session-id";
		final String senderPublicKeyStr = Base64.getEncoder().encodeToString(otherPublicKey.getEncoded());
		final RelayCryptographer senderCryptographer = new RelayCryptographer(otherPrivateKey);
		final String encryptedSessionId = senderCryptographer.encodeSessionId(sessionId, clientPublicKey);
		
		return new GeneralAdvertisementMessageDTO("gatekeeper.testcloud1.aitia.arrowhead.eu", senderPublicKeyStr, "gatekeeper.testcloud2.aitia.arrowhead.eu", encryptedSessionId);
	}
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageDTO getTestGeneralAdvertisementMessageDTOWithSessionId(final String sessionId) {
		final String senderPublicKeyStr = Base64.getEncoder().encodeToString(otherPublicKey.getEncoded());
		
		return new GeneralAdvertisementMessageDTO("gatekeeper.testcloud1.aitia.arrowhead.eu", senderPublicKeyStr, "gatekeeper.testcloud2.aitia.arrowhead.eu", sessionId);
	}
	
	//-------------------------------------------------------------------------------------------------
	public TestSession getTestSession() {
		return new TestSession();
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperRelayRequest getTestGatekeeperRelayRequest(final String sessionId, final String messageType, final Object payload) {
		return new GatekeeperRelayRequest(new TestMessageProducer(), otherPublicKey, sessionId, messageType, payload);
	}
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementResult getTestGeneralAdvertisementResult(final Message msg, final String sessionId) {
		return new GeneralAdvertisementResult(new TestMessageConsumer(msg), "gatekeeper.testcloud1.aitia.arrowhead.eu", otherPublicKey, sessionId);
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	private static class TestSession implements Session {
			
		//=================================================================================================
		// members
		
		private MessageConsumer consumer = new TestMessageConsumer(null);
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public void setConsumerWithMessage(final Message msg) {
			this.consumer = new TestMessageConsumer(msg); 
			
		}
		
		//=================================================================================================
		// mocked methods
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public void close() throws JMSException {}
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public Queue createQueue(final String queueName) throws JMSException { 
			return new Queue() {
				public String getQueueName() throws JMSException {
					return queueName;
				}
			};
		}
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public Topic createTopic(final String topicName) throws JMSException { 
			return new Topic() {
				public String getTopicName() throws JMSException {
					return topicName;
				}
			};
		}
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public MessageConsumer createConsumer(final Destination destination) throws JMSException {
			return consumer;
		}
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public MessageProducer createProducer(final Destination destination) throws JMSException { 
			return new TestMessageProducer();
		}
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public TextMessage createTextMessage(final String text) throws JMSException { 
			final ActiveMQTextMessage msg = new ActiveMQTextMessage();
			msg.setText(text);
			
			return msg;
		}

		//=================================================================================================
		// not used methods

		//-------------------------------------------------------------------------------------------------
		public BytesMessage createBytesMessage() throws JMSException { return null; }
		public MapMessage createMapMessage() throws JMSException { return null; }
		public Message createMessage() throws JMSException { return null; }
		public ObjectMessage createObjectMessage() throws JMSException { return null; }
		public ObjectMessage createObjectMessage(final Serializable object) throws JMSException {	return null; }
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
	}
	
	//-------------------------------------------------------------------------------------------------
	private static class TestMessageConsumer implements MessageConsumer {
		
		//=================================================================================================
		// members
		
		private final Message msg;
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public TestMessageConsumer(final Message msg) {
			this.msg = msg;
		}

		//-------------------------------------------------------------------------------------------------
		@Override
		public Message receive(final long timeout) throws JMSException {
			return msg;
		}

		//-------------------------------------------------------------------------------------------------
		@Override
		public void close() throws JMSException {}
		
		//=================================================================================================
		// not used methods
		
		//-------------------------------------------------------------------------------------------------
		public String getMessageSelector() throws JMSException { return null; }
		public MessageListener getMessageListener() throws JMSException { return null; }
		public void setMessageListener(final MessageListener listener) throws JMSException {}
		public Message receive() throws JMSException { return null; }
		public Message receiveNoWait() throws JMSException { return null; }
	}
	
	//-------------------------------------------------------------------------------------------------
	private static class TestMessageProducer implements MessageProducer {
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public void close() throws JMSException {}
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public void send(final Message message) throws JMSException {}
		
		//=================================================================================================
		// not used methods

		//-------------------------------------------------------------------------------------------------
		public void setDisableMessageID(final boolean value) throws JMSException {}
		public boolean getDisableMessageID() throws JMSException { return false; }
		public void setDisableMessageTimestamp(final boolean value) throws JMSException {}
		public boolean getDisableMessageTimestamp() throws JMSException { return false; }
		public void setDeliveryMode(final int deliveryMode) throws JMSException {}
		public int getDeliveryMode() throws JMSException { return 0; }
		public void setPriority(final int defaultPriority) throws JMSException {}
		public int getPriority() throws JMSException { return 0; }
		public void setTimeToLive(final long timeToLive) throws JMSException {}
		public long getTimeToLive() throws JMSException { return 0; }
		public void setDeliveryDelay(final long deliveryDelay) throws JMSException {}
		public long getDeliveryDelay() throws JMSException { return 0; }
		public Destination getDestination() throws JMSException { return null; }
		public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
		public void send(final Destination destination, final Message message) throws JMSException {}
		public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
		public void send(final Message message, final CompletionListener completionListener) throws JMSException {}
		public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
		public void send(final Destination destination, final Message message, final CompletionListener completionListener) throws JMSException {}
		public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
	}
}