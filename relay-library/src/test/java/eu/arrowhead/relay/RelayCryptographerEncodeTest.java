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

package eu.arrowhead.relay;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.ExceptionType;

@RunWith(SpringRunner.class)
public class RelayCryptographerEncodeTest {

	//=================================================================================================
	// members
	
	private RelayCryptographer testingObject;
	
	private PublicKey senderPublicKey;
	
	private PublicKey recipientPublicKey;
	private PrivateKey recipientPrivateKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		recipientPublicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
		
		final InputStream publicKeyInputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.pub");
		senderPublicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream2);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.p12"), "123456".toCharArray());
		final PrivateKey privateKey = Utilities.getPrivateKey(keystore, "123456");
		testingObject = new RelayCryptographer(privateKey);
		
		final KeyStore keystore2 = KeyStore.getInstance("PKCS12");
		keystore2.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.p12"), "123456".toCharArray());
		recipientPrivateKey = Utilities.getPrivateKey(keystore2, "123456");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void encodeSessionId1RecipientPublicKeyNull() {
		testingObject.encodeSessionId(null, (String) null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void encodeSessionId1RecipientPublicKeyEmpty() {
		testingObject.encodeSessionId(null, " ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void encodeSessionId1RecipientPublicKeyInvalid() {
		testingObject.encodeSessionId(null, "bm90IGEga2V5");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void encodeSessionId2SessionIdNull() {
		testingObject.encodeSessionId(null, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void encodeSessionId2SessionIdEmpty() {
		testingObject.encodeSessionId(" ", recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void encodeSessionId2RecipientPublicKeyNull() {
		testingObject.encodeSessionId("abcde", (PublicKey) null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void encodeSessionId2RecipientErrorWhileSigning() {
		final RelayCryptographer temp = new RelayCryptographer(getInvalidPrivateKey());
		temp.encodeSessionId("abcde", recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void encodeSessionId2RecipientErrorWhileEncrypting() {
		testingObject.encodeSessionId("abcde", getInvalidPublicKey());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void encodeSessionId2EverythingOK() throws InvalidJwtException, MalformedClaimException {
		final String result = testingObject.encodeSessionId("sessionId", recipientPublicKey);
		final JwtClaims claims = validateAndDecodeMessage(result);
		Assert.assertEquals("sessionId", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_SESSION_ID));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeRelayMessageMessageTypeNull() {
		testingObject.encodeRelayMessage(null, null, null, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeRelayMessageMessageTypeEmpty() {
		testingObject.encodeRelayMessage("", null, null, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeRelayMessageSessionIdNull() {
		testingObject.encodeRelayMessage("gsd_poll", null, null, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeRelayMessageSessionIdEmpty() {
		testingObject.encodeRelayMessage("gsd_poll", " ", null, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeRelayMessageRecipientPublicKeyNull() {
		testingObject.encodeRelayMessage("gsd_poll", "sessionId", null, (PublicKey) null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void encodeRelayMessagePayloadSerializationError() {
		testingObject.encodeRelayMessage("gsd_poll", "sessionId", new ClassThatJacksonCannotSerialize(), recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void encodeRelayMessageEveryThingOKWithDTO() throws InvalidJwtException, MalformedClaimException {
		final ErrorMessageDTO dto = new ErrorMessageDTO("testMessage", 42, ExceptionType.GENERIC, "test");
		final String dtoJSON = Utilities.toPrettyJson(Utilities.toJson(dto));
		
		final String result = testingObject.encodeRelayMessage("gsd_poll", "sessionId", dto, recipientPublicKey);
		final JwtClaims claims = validateAndDecodeMessage(result);
		Assert.assertEquals("gsd_poll", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_MESSAGE_TYPE));
		Assert.assertEquals("sessionId", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_SESSION_ID));
		Assert.assertEquals(dtoJSON, Utilities.toPrettyJson(claims.getStringClaimValue(CommonConstants.JWT_CLAIM_PAYLOAD)));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void encodeRelayMessageEveryThingOKWithByteArray() throws InvalidJwtException, MalformedClaimException {
		final byte[] bytes = { 1, 2, 3, 4 , 5 };
		final String expected = "AQIDBAU=";
		
		final String result = testingObject.encodeRelayMessage("gsd_poll", "sessionId", bytes, recipientPublicKey);
		final JwtClaims claims = validateAndDecodeMessage(result);
		Assert.assertEquals("gsd_poll", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_MESSAGE_TYPE));
		Assert.assertEquals("sessionId", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_SESSION_ID));
		Assert.assertEquals(expected, claims.getStringClaimValue(CommonConstants.JWT_CLAIM_PAYLOAD));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeBytesByteArrayNull() {
		testingObject.encodeBytes(null, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeBytesByteArrayEmpty() {
		testingObject.encodeBytes(new byte[0], recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void encodeBytesRecipientPublicKeyNull() {
		testingObject.encodeBytes(new byte[] { 1 }, (PublicKey) null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void encodeBytesEveryThingOK() throws InvalidJwtException, MalformedClaimException {
		final byte[] bytes = { 1, 2, 3, 4 , 5 };
		final String expected = "AQIDBAU=";
		
		final String result = testingObject.encodeBytes(bytes, recipientPublicKey);
		final JwtClaims claims = validateAndDecodeMessage(result);
		Assert.assertEquals(CoreCommonConstants.RELAY_MESSAGE_TYPE_RAW, claims.getStringClaimValue(CommonConstants.JWT_CLAIM_MESSAGE_TYPE));
		Assert.assertEquals(expected, claims.getStringClaimValue(CommonConstants.JWT_CLAIM_PAYLOAD));
	}
	
	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PrivateKey getInvalidPrivateKey() {
		return new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PublicKey getInvalidPublicKey() {
		return new PublicKey() {
			public String getFormat() {	return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private JwtClaims validateAndDecodeMessage(final String encryptedMessage) throws InvalidJwtException {
		final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireJwtId()
																.setEnableRequireEncryption()
																.setEnableRequireIntegrity()
																.setDecryptionKey(recipientPrivateKey)
																.setVerificationKey(senderPublicKey)
																.setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWS_SIGN_ALG))
																.setJweAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_KEY_MANAGEMENT_ALG))
																.setJweContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_ENCRYPTION_ALG))
																.build();

		return jwtConsumer.processToClaims(encryptedMessage);
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	private static class ClassThatJacksonCannotSerialize {
	    private final ClassThatJacksonCannotSerialize self = this;

	    @Override
	    public String toString() {
	        return self.getClass().getName();
	   }
	}
}