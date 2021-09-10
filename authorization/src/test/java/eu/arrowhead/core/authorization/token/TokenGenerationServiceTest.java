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

package eu.arrowhead.core.authorization.token;

import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.TokenGenerationDetailedResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationMultiServiceResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.authorization.AuthorizationMain;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
public class TokenGenerationServiceTest {

	//=================================================================================================
	// members

	private static final String authInfo = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1aaeuv1I4bF5dxMIvUvLMxjRn309kdJewIIH08DfL17/LSssD70ZaLz0yxNfbPPQpFK8LMK+HQHDiGZH5yp4qJDuEgfmUrqWibnBIBc/K3Ob45lQy0zdFVtFsVJYBFVymQwgxJT6th0hI3RGLbCJMzbmpDzT7g0IDsN+64tMyi08ZCPrqk99uzYgioSSWNb9bhG2Z9646b3oiY5utQWRhP/2z/t6vVJHtRYeyaXPl6Z2M/5KnjpSvpSeZQhNrw+Is1DEE5DHiEjfQFWrLwDOqPKDrvmFyIlJ7P7OCMax6dIlSB7GEQSSP+j4eIxDWgjm+Pv/c02UVDc0x3xX/UGtNwIDAQAB";

	@InjectMocks
	private TokenGenerationService tokenGenerationService;
	
	@Mock
	private CommonDBService commonDBService;
	
	@Spy
	private ServiceInterfaceNameVerifier interfaceNameVerifier; 
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void init() {
		ReflectionTestUtils.setField(tokenGenerationService, "sslEnabled", true);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGenerateTokenInsecureMode() {
		ReflectionTestUtils.setField(tokenGenerationService, "sslEnabled", false);
		tokenGenerationService.generateTokens(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGenerateTokenNullRequest() {
		tokenGenerationService.generateTokens(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenNullConsumer() {
		tokenGenerationService.generateTokens(new TokenGenerationRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenConsumerNameNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenConsumerNameEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName(" ");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenConsumerCloudOperatorNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, new CloudRequestDTO(), List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenConsumerCloudOperatorEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setOperator(" ");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, consumerCloud, List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenConsumerCloudNameNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setOperator("aitia");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, consumerCloud, List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenConsumerCloudNameEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setOperator("aitia");
		consumerCloud.setName(" ");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, consumerCloud, List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenProvidersListNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(new TokenGenerationProviderDTO()), "testService");
		request.setProviders(null);
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenProvidersListEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(new TokenGenerationProviderDTO()), "testService");
		request.setProviders(List.of());
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class) 
	public void testGenerateTokenProviderNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(new TokenGenerationProviderDTO()), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenProviderNameNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(new SystemRequestDTO());
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenProviderNameEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName(" ");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenServiceInterfacesNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenServiceInterfacesEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of());
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenServiceInterfaceInvalid() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("INVALID"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenServiceNull() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		request.setService(null);
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenServiceEmpty() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		request.setService("  \t");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenProviderHasNoKey() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGenerateTokenProviderHasNotValidKey() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAuthenticationInfo("bm90IGEga2V5");
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGenerateTokenProviderInitOwnCloudFromCertificateNoCommonName() {
		when(commonDBService.getOwnCloud(true)).thenThrow(new DataNotFoundException("own cloud not found"));
		
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAuthenticationInfo(authInfo);
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGenerateTokenProviderNoServerPrivateKey() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("aitia");
		cloud.setName("testcloud2");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAuthenticationInfo(authInfo);
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, cloud, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGenerateTokenProviderErrorWhileSigning() throws Exception {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(getInvalidKey());
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("aitia");
		cloud.setName("testcloud2");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAuthenticationInfo(authInfo);
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, cloud, List.of(providerDTO), "testService");
		tokenGenerationService.generateTokens(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTokenGenerationOk() throws Exception {
		final KeyStore authKeystore = KeyStore.getInstance("PKCS12");
		authKeystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.p12"), "123456".toCharArray());
		final PrivateKey authPrivateKey = Utilities.getPrivateKey(authKeystore, "123456");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(authPrivateKey);
		
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("aitia");
		cloud.setName("testcloud2");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAuthenticationInfo(authInfo);
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		providerDTO.setTokenDuration(600);
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, cloud, List.of(providerDTO), "testservice");
		final Map<SystemRequestDTO,Map<String,String>> result = tokenGenerationService.generateTokens(request);
		Assert.assertTrue(!result.isEmpty());
		Assert.assertTrue(result.containsKey(provider));
		final Map<String,String> tokens = result.get(provider);
		Assert.assertNotNull(tokens);
		Assert.assertTrue(!tokens.isEmpty());
		final String encryptedToken = tokens.get("HTTP-SECURE-JSON");
		Assert.assertTrue(!Utilities.isEmpty(encryptedToken));
		
		final AlgorithmConstraints jwsAlgConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWS_SIGN_ALG);
		final AlgorithmConstraints jweAlgConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_KEY_MANAGEMENT_ALG);
		final AlgorithmConstraints jweEncConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_ENCRYPTION_ALG);

		final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		final PublicKey authPublicKey = Utilities.getPublicKeyFromPEMFile(is);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/provider.p12"), "123456".toCharArray());
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, "123456");
		
		final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireJwtId()
																.setRequireNotBefore()
																.setEnableRequireEncryption()
																.setEnableRequireIntegrity()
																.setExpectedIssuer(CommonConstants.CORE_SYSTEM_AUTHORIZATION)
																.setDecryptionKey(providerPrivateKey)
																.setVerificationKey(authPublicKey)
																.setJwsAlgorithmConstraints(jwsAlgConstraints)
																.setJweAlgorithmConstraints(jweAlgConstraints)
																.setJweContentEncryptionAlgorithmConstraints(jweEncConstraints)
																.build();
		
		final JwtClaims claims = jwtConsumer.processToClaims(encryptedToken);
		Assert.assertTrue(claims.isClaimValueString(CommonConstants.JWT_CLAIM_CONSUMER_ID));
		Assert.assertEquals("consumer.testcloud2.aitia", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_CONSUMER_ID));
		Assert.assertTrue(claims.isClaimValueString(CommonConstants.JWT_CLAIM_SERVICE_ID));
		Assert.assertEquals("testservice", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_SERVICE_ID));
		Assert.assertTrue(System.currentTimeMillis() < claims.getExpirationTime().getValueInMillis());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMultiTokenGenerationOk() throws Exception {
		final KeyStore authKeystore = KeyStore.getInstance("PKCS12");
		authKeystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.p12"), "123456".toCharArray());
		final PrivateKey authPrivateKey = Utilities.getPrivateKey(authKeystore, "123456");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(authPrivateKey);
		
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setPort(1000);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("aitia");
		cloud.setName("testcloud2");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setPort(2000);
		provider.setAuthenticationInfo(authInfo);
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO();
		providerDTO.setProvider(provider);
		providerDTO.setServiceInterfaces(List.of("HTTP-SECURE-JSON"));
		providerDTO.setTokenDuration(600);
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, cloud, List.of(providerDTO), "testservice");
		final TokenGenerationMultiServiceResponseDTO result = tokenGenerationService.generateMultiServiceTokensResponse(List.of(request));
		
		final TokenGenerationDetailedResponseDTO resultItem = result.getData().get(0);
		Assert.assertEquals(request.getService(), resultItem.getService());
		Assert.assertEquals(request.getConsumer().getSystemName(), resultItem.getConsumerName());
		Assert.assertEquals(request.getConsumer().getAddress(), resultItem.getConsumerAdress());
		Assert.assertEquals(request.getConsumer().getPort().intValue(), resultItem.getConsumerPort());
		Assert.assertEquals(request.getProviders().get(0).getProvider().getSystemName(), resultItem.getTokenData().get(0).getProviderName());
		Assert.assertEquals(request.getProviders().get(0).getProvider().getAddress(), resultItem.getTokenData().get(0).getProviderAddress());
		Assert.assertEquals(request.getProviders().get(0).getProvider().getPort().intValue(), resultItem.getTokenData().get(0).getProviderPort());
		
		final Map<String,String> tokens = resultItem.getTokenData().get(0).getTokens();
		Assert.assertNotNull(tokens);
		Assert.assertTrue(!tokens.isEmpty());
		final String encryptedToken = tokens.get("HTTP-SECURE-JSON");
		Assert.assertTrue(!Utilities.isEmpty(encryptedToken));
		
		final AlgorithmConstraints jwsAlgConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWS_SIGN_ALG);
		final AlgorithmConstraints jweAlgConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_KEY_MANAGEMENT_ALG);
		final AlgorithmConstraints jweEncConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_ENCRYPTION_ALG);

		final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		final PublicKey authPublicKey = Utilities.getPublicKeyFromPEMFile(is);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/provider.p12"), "123456".toCharArray());
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, "123456");
		
		final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireJwtId()
																.setRequireNotBefore()
																.setEnableRequireEncryption()
																.setEnableRequireIntegrity()
																.setExpectedIssuer(CommonConstants.CORE_SYSTEM_AUTHORIZATION)
																.setDecryptionKey(providerPrivateKey)
																.setVerificationKey(authPublicKey)
																.setJwsAlgorithmConstraints(jwsAlgConstraints)
																.setJweAlgorithmConstraints(jweAlgConstraints)
																.setJweContentEncryptionAlgorithmConstraints(jweEncConstraints)
																.build();
		
		final JwtClaims claims = jwtConsumer.processToClaims(encryptedToken);
		Assert.assertTrue(claims.isClaimValueString(CommonConstants.JWT_CLAIM_CONSUMER_ID));
		Assert.assertEquals("consumer.testcloud2.aitia", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_CONSUMER_ID));
		Assert.assertTrue(claims.isClaimValueString(CommonConstants.JWT_CLAIM_SERVICE_ID));
		Assert.assertEquals("testservice", claims.getStringClaimValue(CommonConstants.JWT_CLAIM_SERVICE_ID));
		Assert.assertTrue(System.currentTimeMillis() < claims.getExpirationTime().getValueInMillis());
	}
	
	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PrivateKey getInvalidKey() {
		return new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		};
	}
}