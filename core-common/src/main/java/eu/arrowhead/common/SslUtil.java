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

package eu.arrowhead.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.exception.ArrowheadException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ServiceConfigurationError;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class SslUtil
{

    //=================================================================================================
    // members 

    private static final String BOUNCYCASTLE_IDENTIFIER = "BC";
    private static final String CA_CERTIFICATE = "ca-certificate";
    private static final String CERTIFICATE = "certificate";
    private static final String PRIVATE_KEY = "private-key";
    private static final String TLS_VERSION = "TLSv1.3";

    private static final Logger logger = LogManager.getLogger(SslUtil.class);

    //=================================================================================================
    // methods


    //-------------------------------------------------------------------------------------------------
    /**
     * Returns an SSLfactory from the system's key and trust stores
     *
     * @return An SSLfactory
     * @throws Exception
     */
    public static SSLSocketFactory getSSLSocketFactory(final SSLProperties sslProperties) throws Exception {
	try {
	    X509Certificate caCert = null;
	    final KeyStore trustStore = getTrustStore(sslProperties);
	    final KeyStore keyStore = getKeyStore(sslProperties);
	    final TrustStrategy acceptingTrustStrategy = (final X509Certificate[] chain, final String authType) -> true;
	    final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, acceptingTrustStrategy).loadKeyMaterial(keyStore, 
		    sslProperties.getKeyStorePassword().toCharArray()).build();
	    return sslContext.getSocketFactory();
	} catch (final Exception e) {
	    throw new ArrowheadException("Certificate failure");
	    }
    }

    //-------------------------------------------------------------------------------------------------
    private static KeyStore getKeyStore(final SSLProperties sslProperties) {
	try {
	    final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
	    keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
	    return keystore;
	} catch (final KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
	    throw new ServiceConfigurationError("Cannot open keystore: " + e.getMessage());
	}
    }

    //-------------------------------------------------------------------------------------------------
    private static KeyStore getTrustStore(final SSLProperties sslProperties) {
	try {
	    final KeyStore truststore = KeyStore.getInstance(sslProperties.getKeyStoreType());
	    truststore.load(sslProperties.getTrustStore().getInputStream(), sslProperties.getTrustStorePassword().toCharArray());
	    return truststore;
	} catch (final KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
	    throw new ServiceConfigurationError("Cannot open truststore: " + e.getMessage());
	}
    }

    //-------------------------------------------------------------------------------------------------
    public static SSLSocketFactory getSSLSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final String password)
	    throws
	    InvalidPathException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
	UnrecoverableKeyException, KeyManagementException, Exception {

	    PEMParser parser = null;
	    try {
		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
		final X509CertificateHolder caCert = (X509CertificateHolder) parser.readObject();
		parser.close();

		// load client certificate
		parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
		final X509CertificateHolder cert = (X509CertificateHolder) parser.readObject();
		parser.close();

		// load client private key
		parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(keyFile)))));
		Object obj = parser.readObject();
		parser.close();
		KeyPair key = null;
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BOUNCYCASTLE_IDENTIFIER);
		if (obj instanceof PEMEncryptedKeyPair) {
		    PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
		    converter = new JcaPEMKeyConverter().setProvider(BOUNCYCASTLE_IDENTIFIER);
		    key = converter.getKeyPair(((PEMEncryptedKeyPair) obj).decryptKeyPair(decProv));
		} else {
		    key = converter.getKeyPair((PEMKeyPair) obj);
		}

		final JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
		certConverter.setProvider(BOUNCYCASTLE_IDENTIFIER);

		// CA certificate is used to authenticate server
		final KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry(CA_CERTIFICATE, certConverter.getCertificate(caCert));
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(caKs);

		// Client key and certificates are sent to server so it can authenticate us
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry(CERTIFICATE, certConverter.getCertificate(cert));
		ks.setKeyEntry(PRIVATE_KEY, key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{certConverter.getCertificate(cert)});
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());

		final SSLContext context = SSLContext.getInstance(TLS_VERSION);
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	    } catch(Exception e) {
		logger.debug("Unhandled exception: " + e.toString());
		throw e;
              } finally {
                     if (parser != null) {
                            parser.close();
                     }
              }
       }

    
}
