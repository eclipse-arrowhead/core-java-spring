package eu.arrowhead.core.authorization.token;

import static org.mockito.Mockito.when;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.authorization.AuthorizationMain;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
public class TokenGenerationServiceTest {

	//=================================================================================================
	// members

	@InjectMocks
	private TokenGenerationService tokenGenerationService;
	
	@Mock
	private CommonDBService commonDBService;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext2;

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void tryToken() throws Exception {
		when(commonDBService.getOwnCloud(true)).thenThrow(new DataNotFoundException("this is a warning."));
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(arrowheadContext2.containsKey(CommonConstants.SERVER_COMMON_NAME));
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(arrowheadContext2.containsKey(CommonConstants.SERVER_PRIVATE_KEY));
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn(arrowheadContext2.get(CommonConstants.SERVER_COMMON_NAME));
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(arrowheadContext2.get(CommonConstants.SERVER_PRIVATE_KEY));
		
		ReflectionTestUtils.setField(tokenGenerationService, "sslEnabled", true);
		
		final String authInfo = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1aaeuv1I4bF5dxMIvUvLMxjRn309kdJewIIH08DfL17/LSssD70ZaLz0yxNfbPPQpFK8LMK+HQHDiGZH5yp4qJDuEgfmUrqWibnBIBc/K3Ob45lQy0zdFVtFsVJYBFVymQwgxJT6th0hI3RGLbCJMzbmpDzT7g0IDsN+64tMyi08ZCPrqk99uzYgioSSWNb9bhG2Z9646b3oiY5utQWRhP/2z/t6vVJHtRYeyaXPl6Z2M/5KnjpSvpSeZQhNrw+Is1DEE5DHiEjfQFWrLwDOqPKDrvmFyIlJ7P7OCMax6dIlSB7GEQSSP+j4eIxDWgjm+Pv/c02UVDc0x3xX/UGtNwIDAQAB";
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAuthenticationInfo(authInfo);
		final TokenGenerationRequestDTO request = new TokenGenerationRequestDTO(consumer, null, List.of(provider), "testservice", 10);
		Map<SystemRequestDTO,String> tokens = tokenGenerationService.generateTokens(request);
		final String encryptedToken = tokens.get(provider);
		System.out.println(encryptedToken.length());
		
		final AlgorithmConstraints jwsAlgConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWS_SIGN_ALG);
		final AlgorithmConstraints jweAlgConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_KEY_MANAGEMENT_ALG);
		final AlgorithmConstraints jweEncConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_ENCRYPTION_ALG);
		
		final PublicKey authPublicKey = (PublicKey) arrowheadContext2.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/provider.p12"), "123456".toCharArray());
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, "123456");
		
		final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireJwtId()
								.setRequireNotBefore()
								.setExpectedIssuer(CommonConstants.CORE_SYSTEM_AUTHORIZATION)
								.setDecryptionKey(providerPrivateKey)
								.setVerificationKey(authPublicKey)
								.setJwsAlgorithmConstraints(jwsAlgConstraints)
								.setJweAlgorithmConstraints(jweAlgConstraints)
								.setJweContentEncryptionAlgorithmConstraints(jweEncConstraints)
								.build();
		
		final JwtClaims claims = jwtConsumer.processToClaims(encryptedToken);
		System.out.println(claims);
	}
}