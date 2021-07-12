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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
 
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
 
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
 
@Component
public class SslUtil
{
    private final Logger logger = LogManager.getLogger(SslUtil.class);

    //=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------

    public static SSLSocketFactory getSslSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final String password)
              throws
                     InvalidPathException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
                     UnrecoverableKeyException, KeyManagementException, Exception {
              Security.addProvider(new BouncyCastleProvider());
             
              // load CA certificate
              PEMParser parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
              X509CertificateHolder caCert = (X509CertificateHolder) parser.readObject();
              parser.close();

              // load client certificate
              parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
              X509CertificateHolder cert = (X509CertificateHolder) parser.readObject();
              parser.close();

              // load client private key
              parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(keyFile)))));
              Object obj = parser.readObject();
              KeyPair key = null;
              JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
              if (obj instanceof PEMEncryptedKeyPair) {
                     PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
                     converter = new JcaPEMKeyConverter().setProvider("BC");
                     key = converter.getKeyPair(((PEMEncryptedKeyPair) obj).decryptKeyPair(decProv));
              } else {
                     key = converter.getKeyPair((PEMKeyPair) obj);
              }
              parser.close();
            
              JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
              certConverter.setProvider("BC");

              // CA certificate is used to authenticate server
              KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
              caKs.load(null, null);
              caKs.setCertificateEntry("ca-certificate", certConverter.getCertificate(caCert));
              TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
              tmf.init(caKs);

              // Client key and certificates are sent to server so it can authenticate us
              KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
              ks.load(null, null);
              ks.setCertificateEntry("certificate", certConverter.getCertificate(cert));
              ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{certConverter.getCertificate(cert)});
              KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
              kmf.init(ks, password.toCharArray());

              // Finally, create SSL socket factory
              SSLContext context = SSLContext.getInstance("TLSv1.3");
              context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

              return context.getSocketFactory();
       }
}