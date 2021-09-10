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
    public static SSLSocketFactory getSslSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final String password)
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