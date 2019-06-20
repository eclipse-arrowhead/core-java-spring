package eu.arrowhead.common;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.exception.AuthException;

@Component
public class ApplicationInitListener {
	
	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(ApplicationInitListener.class);

	@Autowired
	private SSLProperties sslProperties;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	public void onApplicationEvent(final ContextRefreshedEvent event) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		logger.debug("Initialization in onApplicationEvent()...");
		
		logger.info("Server mode: {}", sslProperties.isSslEnabled() ? "SECURED" : "NOT SECURED");
		if (sslProperties.isSslEnabled()) {
			final KeyStore keyStore = initializeKeyStore();
			checkServerCertificate(keyStore, event.getApplicationContext());
			obtainKeys(keyStore, event.getApplicationContext());
		}
		
		logger.debug("Initialization in onApplicationEvent() is done.");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CommonConstants.ARROWHEAD_CONTEXT)
	public Map<String,Object> getArrowheadContext() {
		return new ConcurrentHashMap<>();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private KeyStore initializeKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		logger.debug("initializeKeyStore started...");
		Assert.isTrue(sslProperties.isSslEnabled(), "SSL is not enabled.");
		final String messageNotDefined = " is not defined.";
		Assert.isTrue(!Utilities.isEmpty(sslProperties.getKeyStoreType()), CommonConstants.KEYSTORE_TYPE + messageNotDefined);
		Assert.notNull(sslProperties.getKeyStore(), CommonConstants.KEYSTORE_PATH + messageNotDefined);
		Assert.isTrue(sslProperties.getKeyStore().exists(), CommonConstants.KEYSTORE_PATH + " file is not found.");
		Assert.notNull(sslProperties.getKeyStorePassword(), CommonConstants.KEYSTORE_PASSWORD + messageNotDefined);
		
		final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
		keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());

		return keystore;
	}

	//-------------------------------------------------------------------------------------------------
	private void checkServerCertificate(final KeyStore keyStore, final ApplicationContext appContext) {
		logger.debug("checkServerCertificate started...");
		final X509Certificate serverCertificate = Utilities.getFirstCertFromKeyStore(keyStore);
		final String serverCN = Utilities.getCertCNFromSubject(serverCertificate.getSubjectDN().getName());
		if (!Utilities.isKeyStoreCNArrowheadValid(serverCN)) {
			logger.info("Server CN ({}) is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".", serverCN);
			throw new AuthException("Server CN (" + serverCN + ") is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".");
		}
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		context.put(CommonConstants.SERVER_COMMON_NAME, serverCN);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void obtainKeys(final KeyStore keyStore, final ApplicationContext appContext) {
		logger.debug("obtainKeys started...");
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		
		final PublicKey publicKey = Utilities.getFirstCertFromKeyStore(keyStore).getPublicKey();
		context.put(CommonConstants.SERVER_PUBLIC_KEY, publicKey);
		
		final PrivateKey privateKey = Utilities.getPrivateKey(keyStore, sslProperties.getKeyPassword());
		context.put(CommonConstants.SERVER_PRIVATE_KEY, privateKey);
	}
}