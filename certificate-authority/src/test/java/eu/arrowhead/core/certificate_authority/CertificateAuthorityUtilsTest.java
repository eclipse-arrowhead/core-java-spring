package eu.arrowhead.core.certificate_authority;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ServiceConfigurationError;

import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;

@RunWith(SpringRunner.class)
public class CertificateAuthorityUtilsTest {

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

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

    @Test
    public void testEncodeCertificateNull() {
        thrownException.expect(IsInstanceOf.instanceOf(AuthException.class));
        thrownException.expectCause(IsInstanceOf.instanceOf(NullPointerException.class));
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
        final JcaPKCS10CertificationRequest csr = getResourceCSR("certificates/valid.csr");
        assertNotNull(csr);
        CertificateAuthorityUtils.checkCommonName(csr, null);
    }

    @Test(expected = BadPayloadException.class)
    public void testCheckCsrSignatureNull() {
        CertificateAuthorityUtils.checkCsrSignature(null);
    }

    @Test
    public void testGetClientKeyNull() {
        thrownException.expect(DataNotFoundException.class);
        thrownException.expectCause(IsInstanceOf.instanceOf(NullPointerException.class));
        CertificateAuthorityUtils.getClientKey(null);
    }

    private static String getResourceContentAsString(final String resourcePath) throws IOException {
        final File resource = new ClassPathResource(resourcePath).getFile();
        return new String(Files.readAllBytes(resource.toPath())).trim();
    }

    private static JcaPKCS10CertificationRequest getResourceCSR(
            final String resourcePath)
            throws IOException, NullPointerException {
        final String resourceBytes = getResourceContentAsString(resourcePath);
        final byte[] csrBytes = Base64.getDecoder().decode(resourceBytes);
        return new JcaPKCS10CertificationRequest(csrBytes);
    }
}
