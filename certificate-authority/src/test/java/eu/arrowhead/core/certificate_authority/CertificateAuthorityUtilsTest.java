/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.certificate_authority;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ServiceConfigurationError;

import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
public class CertificateAuthorityUtilsTest {

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    @Test(expected = ServiceConfigurationError.class)
    public void testGetKeystoreNull() {
        CertificateAuthorityUtils.getCertificateAuthorityKeyStore(null);
    }

    @Test(expected = BadPayloadException.class)
    public void testDecodePKCS10CSRNull() {
        CertificateAuthorityUtils.decodePKCS10CSR(null);
    }

    @Test(expected = BadPayloadException.class)
    public void testDecodePKCS10CSREmpty() {
        CertificateAuthorityUtils.decodePKCS10CSR(new CertificateSigningRequestDTO());
    }

    @Test(expected = BadPayloadException.class)
    public void testDecodePKCS10CSRInvalidString() {
        CertificateAuthorityUtils.decodePKCS10CSR(new CertificateSigningRequestDTO("Invalid CSR String"));
    }

    @Test(expected = BadPayloadException.class)
    public void testDecodeCertificateNull() {
        CertificateAuthorityUtils.decodeCertificate(null);
    }

    @Test(expected = BadPayloadException.class)
    public void testDecodeCertificateEmpty() {
        CertificateAuthorityUtils.decodeCertificate("");
    }

    @Test(expected = InvalidParameterException.class)
    public void testDecodeCertificateInvalidString() {
        CertificateAuthorityUtils.decodeCertificate("Invalid Certificate");
    }

    @Test(expected = InvalidParameterException.class)
    public void testEncodeCertificateNull() {
        CertificateAuthorityUtils.encodeCertificate(null);
    }

    @Test(expected = ServiceConfigurationError.class)
    public void testGetCloudCommonNameNullCert() {
        CertificateAuthorityUtils.getCloudCommonName(null);
    }

    @Test(expected = BadPayloadException.class)
    public void testCheckCommonNameNullCSR() {
        CertificateAuthorityUtils.checkCommonName(null, "cloudCN");
    }

    @Test(expected = BadPayloadException.class)
    public void testCheckCommonNameNullCloudCN() throws NullPointerException, IOException {
        final JcaPKCS10CertificationRequest csr = getResourceCSR("certificates/sysop.csr");
        assertNotNull(csr);
        CertificateAuthorityUtils.checkCommonName(csr, null);
    }

    @Test(expected = BadPayloadException.class)
    public void testCheckCsrSignatureNull() {
        CertificateAuthorityUtils.checkCsrSignature(null);
    }

    @Test(expected = BadPayloadException.class)
    public void testGetClientKeyNull() {
        CertificateAuthorityUtils.getClientKey(null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetSubjectAlternativeNamesNull() {
        CertificateAuthorityUtils.getSubjectAlternativeNames(null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testSha256Null() {
        CertificateAuthorityUtils.sha256(null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetRequesterCommonNameNull() {
        CertificateAuthorityUtils.getRequesterCommonName(null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetProtectedCommonNamesNull() {
        CertificateAuthorityUtils.getProtectedCommonNames(null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testGetProtectedCommonNamesEmpty() {
        CertificateAuthorityUtils.getProtectedCommonNames("");
    }

    private static String getResourceContentAsString(final String resourcePath) throws IOException {
        final File resource = new ClassPathResource(resourcePath).getFile();
        return new String(Files.readAllBytes(resource.toPath())).trim();
    }

    private static JcaPKCS10CertificationRequest getResourceCSR(final String resourcePath)
            throws IOException, NullPointerException {
        final String resourceBytes = getResourceContentAsString(resourcePath);
        final byte[] csrBytes = Base64.getDecoder()
                                      .decode(resourceBytes);
        return new JcaPKCS10CertificationRequest(csrBytes);
    }
}
