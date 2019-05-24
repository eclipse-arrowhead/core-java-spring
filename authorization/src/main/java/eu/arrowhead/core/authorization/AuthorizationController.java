package eu.arrowhead.core.authorization;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CommonConstants;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class AuthorizationController {
	
	private static final String ECHO_URI = "/echo";
	private static final String DELETE_SERVICE_URI = "/delete";
	private static final String TEST_URI = "/test";
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Value(CommonConstants.$KEYSTORE_TYPE)
	private String keyStoreType;
	
	@Value(CommonConstants.$KEYSTORE_PATH)
	private Resource keyStore;
	
	@Value(CommonConstants.$KEYSTORE_PASSWORD)
	private String keyStorePassword;
	
	@Value(CommonConstants.$KEY_PASSWORD)
	private String keyPassword;
	
	@Value(CommonConstants.$TRUSTSTORE_PATH)
	private Resource trustStore;
	
	@Value(CommonConstants.$TRUSTSTORE_PASSWORD)
	private String trustStorePassword;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_ADDRESS)
	private String srAddress;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PORT)
	private int srPort;
	
	
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	@GetMapping(path = DELETE_SERVICE_URI)
	public String deleteThisService() {
		logger.debug("deleteThisService() is called.");
	
		// example how to call an other webservice (most of the code needs to refactor to an init method)
		// And Subject Alternative Names needs to add to p12 files (127.0.0.1, ip-address and hostname at least) 
		final KeyStore keystore = KeyStore.getInstance(keyStoreType);
		keystore.load(keyStore.getInputStream(), keyStorePassword.toCharArray());
		final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
							   								 .loadKeyMaterial(keystore, keyPassword.toCharArray())
							   								 .setKeyStoreType(keyStoreType)
							   								 .build();
		final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
		final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
		final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		final RestTemplate restTemplate = new RestTemplate(factory);
		
		final String uriString = UriComponentsBuilder.newInstance().scheme("https").host(srAddress).port(srPort).path("/echo").build().toUriString();
		final ResponseEntity<String> responseEntity = restTemplate.getForEntity(uriString, String.class);
		
		return "Delete this service." + responseEntity.getBody();
	}
}
