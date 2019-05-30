package eu.arrowhead.common;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

	private Logger logger = LogManager.getLogger(ApplicationInitListener.class);

	@Autowired
	private SSLProperties sslProperties;
	
	@EventListener
	public void onApplicationEvent(final ContextRefreshedEvent event) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		logger.debug("Initialization in onApplicationEvent()...");
		
		if (sslProperties.isSslEnabled()) {
			checkServerCertificate(event.getApplicationContext());
		}
		
		logger.debug("Initialization in onApplicationEvent() is done.");
	}
	
	@Bean(CommonConstants.ARROWHEAD_CONTEXT)
	public Map<String,String> getArrowheadContext() {
		return new ConcurrentHashMap<>();
	}
	
	private void checkServerCertificate(final ApplicationContext appContext) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		Assert.isTrue(sslProperties.isSslEnabled(), "SSL is not enabled.");
		final String messageNotDefined = " is not defined.";
		Assert.isTrue(!Utilities.isEmpty(sslProperties.getKeyStoreType()), CommonConstants.KEYSTORE_TYPE + messageNotDefined);
		Assert.notNull(sslProperties.getKeyStore(), CommonConstants.KEYSTORE_PATH + messageNotDefined);
		Assert.isTrue(sslProperties.getKeyStore().exists(), CommonConstants.KEYSTORE_PATH + " file is not found.");
		Assert.notNull(sslProperties.getKeyStorePassword(), CommonConstants.KEYSTORE_PASSWORD + messageNotDefined);

		final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
		keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
		final X509Certificate serverCertificate = Utilities.getFirstCertFromKeyStore(keystore);
		final String serverCN = Utilities.getCertCNFromSubject(serverCertificate.getSubjectDN().getName());
		if (!Utilities.isKeyStoreCNArrowheadValid(serverCN)) {
			logger.info("Server CN ({}) is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".", serverCN);
			throw new AuthException("Server CN (" + serverCN + ") is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".");
		}
		
		@SuppressWarnings("unchecked")
		final Map<String,String> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		context.put(CommonConstants.SERVER_COMMON_NAME, serverCN);
	}
}