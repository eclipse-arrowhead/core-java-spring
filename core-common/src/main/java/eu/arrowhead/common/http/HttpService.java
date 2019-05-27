package eu.arrowhead.common.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.el.MethodNotFoundException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.UnavailableServerException;

@Component
public class HttpService {

	private static final String ERROR_MESSAGE_PART_PKIX_PATH = "PKIX path";
	
	private static final List<HttpMethod> NOT_SUPPORTED_METHODS = Collections.unmodifiableList(Arrays.asList(new HttpMethod[] { HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.TRACE }));
 
	private final Logger logger = LogManager.getLogger(HttpService.class);
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
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
	
	@Value(CommonConstants.$DISABLE_HOSTNAME_VERIFIER_WD)
	private boolean disableHostnameVerifier;
	
	@Value(CommonConstants.$HTTP_CLIENT_CONNECTION_TIMEOUT_WD)
	private int connectionTimeout;
	
	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private int socketTimeout;
	
	@Value(CommonConstants.$HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT_WD)
	private int connectionManagerTimeout;
	
	@Autowired
	private ArrowheadHttpResponseErrorHandler errorHandler;
	
	private RestTemplate template;
	private RestTemplate sslTemplate;
	private SSLContext sslContext;
	
	@PostConstruct
	public void init() throws Exception {
		logger.debug("Initializing HttpService...");
		template = createTemplate(null);
		if (sslEnabled) {
			try {
				sslContext = createSSLContext();
			} catch (final KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				// it's initialization so we just logging the exception then let the application die
				logger.error("Error while creating SSL context: " + e.getMessage(), e);
				throw e;
			}
			sslTemplate = createTemplate(sslContext);
		}
		logger.debug("HttpService is initialized.");
	}
	
	public <T,P> ResponseEntity<T> sendRequest(final UriComponents uri, final HttpMethod method, final Class<T> responseType, final P payload, final SSLContext givenContext) {
		Assert.notNull(method, "Request method is not defined.");
		logger.debug("Sending " + method + " request to: " + uri);
		
		if (uri == null) {
			logger.error("sendRequest() is called with null URI.");
			throw new NullPointerException("HttpService.sendRequest method received null URI. This most likely means the invoking Core System could not " +
			              				   "fetch the service of another Core System from the Service Registry!");
		}
		
		if (NOT_SUPPORTED_METHODS.contains(method)) {
			throw new MethodNotFoundException("Invalid method type was given to the HttpService.sendRequest() method.");
		}
		
		final boolean secure = CommonConstants.HTTPS.equalsIgnoreCase(uri.getScheme());
		if (secure && sslTemplate == null) {
			logger.debug("sendRequest(): secure request sending was invoked in insecure mode.");
			throw new AuthException("SSL Context is not set, but secure request sending was invoked. An insecure module can not send requests to secure modules.", HttpStatus.SC_UNAUTHORIZED);
		}
		
		final RestTemplate usedTemplate = secure ? (givenContext != null ? createTemplate(givenContext) : sslTemplate) : template;

		final HttpEntity<P> entity = getHttpEntity(method, payload);
		try {
			final ResponseEntity<T> response = usedTemplate.exchange(uri.toUri(), method, entity, responseType);
			
			return response;
		} catch (final ResourceAccessException e) {
			if (e.getMessage().contains(ERROR_MESSAGE_PART_PKIX_PATH)) {
				logger.error("The system at " + uri.toUriString() + " is not part of the same certificate chain of trust!");
		        throw new AuthException("The system at " + uri.toUriString() + " is not part of the same certificate chain of trust!", HttpStatus.SC_UNAUTHORIZED, e);
			} else {
		        logger.error("UnavailableServerException occurred at " + uri.toUriString(), e);
		        throw new UnavailableServerException("Could not get any response from: " + uri.toUriString(), HttpStatus.SC_SERVICE_UNAVAILABLE, e);
			}
		}
	}
	
	public <T,P> ResponseEntity<T> sendRequest(final UriComponents uri, final HttpMethod method, final Class<T> responseType, final P payload) {
		return sendRequest(uri, method, responseType, payload, null);
	}
	
	public <T,P> ResponseEntity<T> sendRequest(final UriComponents uri, final HttpMethod method, final Class<T> responseType, final SSLContext givenContext) {
		return sendRequest(uri, method, responseType, null, givenContext);
	}
	
	public <T,P> ResponseEntity<T> sendRequest(final UriComponents uri, final HttpMethod method, final Class<T> responseType) {
		return sendRequest(uri, method, responseType, null, null);
	}
		
	private <P> HttpEntity<P> getHttpEntity(final HttpMethod method, final P payload) {
		final MultiValueMap<String,String> headers = new LinkedMultiValueMap<String,String>();
		headers.put(HttpHeaders.ACCEPT, Arrays.asList(MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE));
		if (payload != null) {
			headers.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
		}
		
		return payload != null ? new HttpEntity<>(payload, headers) : new HttpEntity<>(headers);
	}
	
	private RestTemplate createTemplate(final SSLContext sslContext) {
		final HttpClient client = createClient(sslContext);
		final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
		final RestTemplate restTemplate = new RestTemplate(factory);
		restTemplate.setErrorHandler(errorHandler);
		return restTemplate;
	}
	
	private HttpClient createClient(final SSLContext sslContext) {
		HttpClient client;
		
		if (sslContext == null) {
			client = HttpClients.custom().setDefaultRequestConfig(createRequestConfig())
										 .build();
		} else {
			SSLConnectionSocketFactory socketFactory;
			if (disableHostnameVerifier) {
				final HostnameVerifier allHostsAreAllowed = (hostname, session) -> {
					return true;
				}; // just for testing, DO NOT USE this in a production environment
				socketFactory = new SSLConnectionSocketFactory(sslContext, allHostsAreAllowed);
			} else {
				socketFactory = new SSLConnectionSocketFactory(sslContext);
			}
			client = HttpClients.custom().setDefaultRequestConfig(createRequestConfig())
										 .setSSLSocketFactory(socketFactory)
										 .build();
		}
		
		return client;
	}
	
	private SSLContext createSSLContext() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
		Assert.isTrue(!Utilities.isEmpty(keyStoreType), CommonConstants.KEYSTORE_TYPE + " is not defined.");
		Assert.notNull(keyStore, CommonConstants.KEYSTORE_PATH + " is not defined.");
		Assert.isTrue(keyStore.exists(), CommonConstants.KEYSTORE_PATH + " file is not found.");
		Assert.notNull(keyStorePassword, CommonConstants.KEYSTORE_PASSWORD + " is not defined.");
		Assert.notNull(keyPassword, CommonConstants.KEY_PASSWORD + " is not defined.");
		Assert.notNull(trustStore, CommonConstants.TRUSTSTORE_PATH + " is not defined.");
		Assert.isTrue(trustStore.exists(), CommonConstants.TRUSTSTORE_PATH + " file is not found.");
		Assert.notNull(trustStorePassword, CommonConstants.TRUSTSTORE_PASSWORD + " is not defined.");
		
		final KeyStore keystore = KeyStore.getInstance(keyStoreType);
		keystore.load(keyStore.getInputStream(), keyStorePassword.toCharArray());
		final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
							   								 .loadKeyMaterial(keystore, keyPassword.toCharArray())
							   								 .setKeyStoreType(keyStoreType)
							   								 .build();

		return sslContext;
	}
	
	private RequestConfig createRequestConfig() {
		return RequestConfig.custom().setConnectTimeout(connectionTimeout)
									 .setSocketTimeout(socketTimeout)
									 .setConnectionRequestTimeout(connectionManagerTimeout)
									 .build();
	}
}