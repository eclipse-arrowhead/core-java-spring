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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DecryptedMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.ExceptionType;

@RunWith(SpringRunner.class)
public class RelayCryptographerDecodeTest {

	//=================================================================================================
	// members

	private static final String ENCRYPTED_MESSAGE = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.aDb1NxVTRSgGNkPO9GM-ogmSiDhMBk_cvg-RL9VbPg3UEAtYLFxX5D3PEVkf-kxB6vnZX3yUyMpv5_buvgGetS8HgAqMHI3cHFzvrQexkvWgBdHxPNaDFIRBb1dxCKiHnDvXBpBSz0rYMQ7BBCqFgjVtgitkq5XLX8En28ht6yxt6dGJ5raX7VkF9LAZp7eWYhDooKpNt4d53H0pstiILIs7gaNzDDwO4ysXfBYIqK1eJUqITJgAaqj7smEsBlezsLbuniGVUssnvmJTigz9JXP5oHJCeO-SLbSN6FKExZJc_uWUeMNLPh6jwk6njXLZKL0-0picKklcJorD3722dg.VGCgwTOGqbgJKuixAnw5dA.uqvSRkpuS2GoDkMG9nX8Y-jtM45b-bH-0wDX4jgpN61CwjbgaPgTF7_4WNlN84bIg7T1_ivOpAGWZzEYZ1x7y7FgP2ITv6J13muNEckmRr6s63l9Tghn0ns19PMTusFpNnc2CLIn7yp0fyM2iYrzljERBMBFbftRGKIAOdMzggLXJjTEmQXXHvYy6Ns8LrKq4p-y-nbmsivroQODb2rrXP48Or3291tT5CICzj7KC0cdADjCiDsKWcTpXmTVlbzVZgwYVCzcwPtEBVYpPnThJwgAkPnV1Qim-dkvIqfwookrzBO2gG7IMs0deGAQIkNKjSvj-OBHNaElIGRCb5XVysqsZfhdmeStqV2vBtGyFHCYEnbby29JmnFvwJhjcaNSjPTaotEw5eaMQJyNSw5n-Cb9rqG3UlnV8ARJuLutY5BGLGasd_CWPdw7irqU3iV7kxkrAvB5lCFBQzD1Ld1X2F4G2PhZwCRdI4jOMaW96J0FipGYR94SDnuFrifvehfeIQBLhNdaLxLQeRSry5TbRhZA5tmOj9XxNN-i1esaHhww1M6-5_Ci3ItVbp3bV81j4L19FDpbBVPULkPWKUcpd1dj3y1D-LXm2a6HzxNbUHIyayZwuEASFE3h2IbZvzSqBkzx6vGG3TOxiBctxmfkkXp3jjTUa6uB7wEcuReQBu2o5g2nwuxv3MjIDrkFoiM_Lp1h2wHaoASUOzB2e4REJhHSQK90C2DJaW9guYwZca9Z6viAxVb_O3K3pxEq0DLdXpkjmn7WLUI3mwW3d9P0XUXq0wQg_qrnSsvuJehcQA3tnHwHUwUM3iCmcv1KYaFR.00Qyy2Xq8VxZ70bfWTrVrydF4zgb-fcUQFtcHM83Um4";
	private static final String ENCRYPTED_MESSAGE_BYTES = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.MXjNu-SasBiWTDmZz6eW9xi6Kemseu2_tRuu2zlmL6rp51sRTYnhgK1Q5v3yxuX1R2VUGBPHd9eMXUFjUjW0GrQMC9mlQpFIXeEDa8AQ-K6MPIPP-aB4tYLDpk_KgJewfWEeqpMVbjnKdnqrGDPhqRMAU2MIYmKupby5a2P6aT434S4zq12eu3FgbYPg73iy2tuo4znc3D9A5fy8l5zpu7yoM8V9pisP1kJ_YfD3zrSXZ45m3vi8zG0MQurApu4bw4x-8f4TZHf9wMbMNnMyw9FR_V-nuS_PRZ9lIZVFwO9wZwH6SR4B0_UBLi7ej-T2vV4-k_95yuiuhW5NMgQZ7g.6BFm8GQRRXTtKWOQrALEBQ.xM2mr2Z_6xdSmVlT6jBdb7X3tczYCyf9g5-5hZrOIEIHeMf7dPxTZDqJbtQsbxvJPN_BUw-dJ2xqJGV8b-t7EAJYgS3rFYW9a7DfKOAiDfIQqE8ULXtpa9U-Wsz1zaSiFFsnLkXEYsEdRDVan4npAdQl9W--snIl_SZx2RUWpHyRGrPDd9zobKdCfALVNrU2JZtEmKehD7yZ-EySrODxNIf-sE5XeVSbaBL0IxFDdsx1xOpCVbty6E7R-J4Yz-f5oYcQKFaQxHnai-C-Dyr_LpyDj-PS3lvELjjP6a-jihJr8e7yPuOWqb4EE2o7yDPMzKCbzT1sOgbNxybcLp5l7F6bePYAweUxOZBI3XyHd6pgC5corcUom4B5fX7jKYxwNCcCuyzzD_LQr6qQvjp270tL8QwcNO7AQdZ8SAUuw0GcEeEZGi9WACKghdiofwnir5kcoFK-gceVEdmY765ofsrsheY0lzpz4uin5-HKoXPyD-XOzLKuHUgcNebu7efWqnK2TyGsdMb2HxyHuYnBeNv981ufx3zpIyu4tTIIfppGYFmkzzopZi0wuNbVKO7KlthXe60JZ3zP1CYQp9SfWCMkOsSWwtWy6SGBKy9EY-5_IjEhBSejPWU2pWs01YB5.Ymh9tyQ0qU3wZVLHFjTSAqrk6bM4_YbzA8kbHcPKrY0";
	
	private RelayCryptographer testingObject;
	
	private PublicKey senderPublicKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gatekeeper.pub");
		senderPublicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.p12"), "123456".toCharArray());
		final PrivateKey privateKey = Utilities.getPrivateKey(keystore, "123456");
		testingObject = new RelayCryptographer(privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void decodeMessage1SenderPublicKeyNull() {
		testingObject.decodeMessage(null, (String) null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void decodeMessage1SenderPublicKeyEmpty() {
		testingObject.decodeMessage(null, " ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void decodeMessage1SenderPublicKeyInvalid() {
		testingObject.decodeMessage(null, "bm90IGEga2V5");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void decodeMessage2EncryptedMessageNull() {
		testingObject.decodeMessage(null, senderPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void decodeMessage2EncryptedMessageEmpty() {
		testingObject.decodeMessage(" ", senderPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void decodeMessage2SenderPublicKeyNull() {
		testingObject.decodeMessage("asdsad", (PublicKey) null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void decodeMessage2SenderPublicKeyInvalid() {
		testingObject.decodeMessage(ENCRYPTED_MESSAGE, getInvalidPublicKey());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void decodeMessage2OwnPrivateKeyInvalid() {
		final RelayCryptographer temp = new RelayCryptographer(getInvalidPrivateKey());
		temp.decodeMessage(ENCRYPTED_MESSAGE, senderPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void decodeMessage2EverythingOKWithDTO() throws JsonParseException, JsonMappingException, IOException {
		final DecryptedMessageDTO result = testingObject.decodeMessage(ENCRYPTED_MESSAGE, senderPublicKey);
		final ObjectMapper mapper = new ObjectMapper();
		final ErrorMessageDTO payload = mapper.readValue(result.getPayload(), ErrorMessageDTO.class);
		Assert.assertEquals("gsd_poll", result.getMessageType());
		Assert.assertEquals("sessionId", result.getSessionId());
		Assert.assertEquals("testMessage", payload.getErrorMessage());
		Assert.assertEquals(42, payload.getErrorCode());
		Assert.assertEquals(ExceptionType.GENERIC, payload.getExceptionType());
		Assert.assertEquals("test", payload.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void decodeMessage2EverythingOKWithByteArray() throws JsonParseException, JsonMappingException, IOException {
		final DecryptedMessageDTO result = testingObject.decodeMessage(ENCRYPTED_MESSAGE_BYTES, senderPublicKey);
		Assert.assertEquals(CoreCommonConstants.RELAY_MESSAGE_TYPE_RAW, result.getMessageType());
		Assert.assertEquals("AQIDBAU=", result.getPayload());
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
}