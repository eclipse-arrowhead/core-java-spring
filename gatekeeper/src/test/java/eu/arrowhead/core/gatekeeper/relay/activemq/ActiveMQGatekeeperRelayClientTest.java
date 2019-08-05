package eu.arrowhead.core.gatekeeper.relay.activemq;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.GeneralAdvertisementMessageDTO;
import eu.arrowhead.core.gatekeeper.relay.RelayClientFactory;

@RunWith(SpringRunner.class)
public class ActiveMQGatekeeperRelayClientTest {
	
	//=================================================================================================
	// members
	
	private PublicKey clientPublicKey;
	
	private ActiveMQGatekeeperRelayClient testObject;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.pub");
		clientPublicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.p12"), "123456".toCharArray());
		final PrivateKey clientPrivateKey = Utilities.getPrivateKey(keystore, "123456");
		
		testObject = new ActiveMQGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", clientPublicKey, clientPrivateKey, 1000);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		RelayClientFactory.createGatekeeperRelayClient(null, null, null, 0);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		RelayClientFactory.createGatekeeperRelayClient(" ", null, null, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		RelayClientFactory.createGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", null, null, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		RelayClientFactory.createGatekeeperRelayClient("gatekeeper.testcloud2.aitia.arrowhead.eu", clientPublicKey, null, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostNull() throws JMSException {
		testObject.createConnection(null, 42);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostEmpty() throws JMSException {
		testObject.createConnection("\n", 42);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooLow() throws JMSException {
		testObject.createConnection("localhost", -42);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooHigh() throws JMSException {
		testObject.createConnection("localhost", 420000);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testCreateConnectionfailed() throws JMSException {
		testObject.createConnection("invalid.address.dafafasdasdfgf.qq", 42);
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
	
	//TODO:
}