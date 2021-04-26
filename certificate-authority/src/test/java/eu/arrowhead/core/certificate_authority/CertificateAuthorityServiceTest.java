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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.CaCertificate;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.AddTrustedKeyResponseDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckResponseDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.internal.IssuedCertificateStatus;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.certificate_authority.database.CACertificateDBService;
import eu.arrowhead.core.certificate_authority.database.CACertificateDBServiceTestContext;
import eu.arrowhead.core.certificate_authority.database.CATrustedKeyDBService;
import eu.arrowhead.core.certificate_authority.database.CATrustedKeyDBServiceTestContext;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CACertificateDBServiceTestContext.class, CATrustedKeyDBServiceTestContext.class})
public class CertificateAuthorityServiceTest {

    public static final long CA_CERT_ID = 999;
    public static final long TRUSTED_KEY_ID = 99;
    private static final Pattern PEM_PATTERN = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
    private static final String CLOUD_CN = "testcloud2.aitia.arrowhead.eu";
    private static final String CONSUMER_CN = "consumer." + CLOUD_CN;
    private static final String SYSOP_CN = "sysop." + CLOUD_CN;
    private static final String SIGN_REQUESTER_DUMMY = "dummy";
    private static final String SIGN_REQUESTER_VALID = "valid." + CLOUD_CN;
    private static final String SIGN_REQUESTER_SYSOP = SYSOP_CN;

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    @MockBean(name = "mockCACertificateDBService")
    CACertificateDBService caCertificateDBService;

    @MockBean(name = "mockCATrustedKeyDBService")
    CATrustedKeyDBService caTrustedKeyDBService;

    @InjectMocks
    CertificateAuthorityService service;

    private CAProperties caProperties;

    private X509Certificate rootCertificate;
    private X509Certificate cloudCertificate;

    // =================================================================================================
    // methods

    @BeforeClass
    public static void globalSetup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static String getResourceContent(final String resourcePath) throws IOException {
        final File resource = new ClassPathResource(resourcePath).getFile();
        return new String(Files.readAllBytes(resource.toPath())).trim();
    }

    private static CertificateSigningRequestDTO buildRequest(final String csrResourcePath,
                                                             final ZonedDateTime validAfter,
                                                             final ZonedDateTime validBefore) throws IOException {
        final String encodedCSR = getResourceContent(csrResourcePath);
        return new CertificateSigningRequestDTO(encodedCSR,
                Utilities.convertZonedDateTimeToUTCString(validAfter),
                Utilities.convertZonedDateTimeToUTCString(validBefore));
    }

    private static CertificateSigningRequestDTO buildRequest(final String csrResourcePath) throws IOException {
        return buildRequest(csrResourcePath, null, null);
    }

    @Before
    public void setUp() {
        caProperties = getCAProperties();
        ReflectionTestUtils.setField(service, "caProperties", caProperties);
        ReflectionTestUtils.setField(service, "certificateDbService", caCertificateDBService);
        ReflectionTestUtils.setField(service, "trustedKeyDbService", caTrustedKeyDBService);

        service.init();

        rootCertificate = (X509Certificate) ReflectionTestUtils.getField(service, "rootCertificate");
        cloudCertificate = (X509Certificate) ReflectionTestUtils.getField(service, "cloudCertificate");

        when(caCertificateDBService.saveCertificateInfo(anyString(), any(), anyString(), any(), any()))
                .thenReturn(new CaCertificate(CA_CERT_ID));
    }

    private CAProperties getCAProperties() {
        CAProperties caProperties = mock(CAProperties.class);
        when(caProperties.getCertValidityNegativeOffsetMinutes()).thenReturn(1L);
        when(caProperties.getCertValidityPositiveOffsetMinutes()).thenReturn(60L);
        when(caProperties.getCloudKeyPassword()).thenReturn("123456");
        when(caProperties.getCloudKeyStorePassword()).thenReturn("123456");
        when(caProperties.getCloudKeyStoreType()).thenReturn("PKCS12");
        when(caProperties.getCloudKeyStorePath()).thenReturn(new ClassPathResource("certificates/cloud.p12"));

        return caProperties;
    }

    // -------------------------------------------------------------------------------------------------

    @Test
    public void testGetCloudCommonName() {
        assertEquals(service.getCloudCommonName(), "testcloud2.aitia.arrowhead.eu");
    }

    @Test(expected = InvalidParameterException.class)
    public void testCheckCertificateNull() {
        service.checkCertificate(null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testCheckCertificateEmpty() {
        service.checkCertificate(new CertificateCheckRequestDTO());
    }

    @Test(expected = InvalidParameterException.class)
    public void testCheckCertificateInvalidString() {
        service.checkCertificate(new CertificateCheckRequestDTO(0, "Invalid CSR String"));
    }

    @Test(expected = InvalidParameterException.class)
    public void testCheckCertificateValidPEM() throws IOException {
        final String pemCert = getResourceContent("certificates/sysop.pem");
        service.checkCertificate(new CertificateCheckRequestDTO(0, pemCert));
    }

    @Test(expected = InvalidParameterException.class)
    public void testCheckCertificateValidBase64DERNotFound() throws IOException {
        final String pemCert = getResourceContent("certificates/sysop.pem");
        final String encodedCert = PEM_PATTERN.matcher(pemCert)
                                              .replaceFirst("$1");
        final CertificateCheckRequestDTO request = new CertificateCheckRequestDTO(0, encodedCert);

        when(caCertificateDBService.isCertificateValidNow(any())).thenThrow(DataNotFoundException.class);

        final CertificateCheckResponseDTO response = service.checkCertificate(request);

        verify(caCertificateDBService, times(1)).isCertificateValidNow(any());
        assertEquals(response.getCommonName(), SYSOP_CN);
        assertEquals(response.getStatus(), IssuedCertificateStatus.UNKNOWN);
    }

    @Test()
    public void testCheckCertificateValidBase64DERFound() throws IOException {
        final String pemCert = getResourceContent("certificates/sysop.pem");
        final String encodedCert = PEM_PATTERN.matcher(pemCert)
                                              .replaceFirst("$1")
                                              .replace("\n", "")
                                              .replace("\r", "");
        final CertificateCheckRequestDTO request = new CertificateCheckRequestDTO(0, encodedCert);

        final String now = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        final CertificateCheckResponseDTO responseDTO = new CertificateCheckResponseDTO(0, now, now, SYSOP_CN,
                BigInteger.ONE, IssuedCertificateStatus.GOOD);

        when(caCertificateDBService.isCertificateValidNow(any())).thenReturn(responseDTO);

        final CertificateCheckResponseDTO response = service.checkCertificate(request);

        verify(caCertificateDBService, times(1)).isCertificateValidNow(any());
        assertEquals(response.getCommonName(), SYSOP_CN);
    }

    // ------------------------------------------------------------------------

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateNull1() {
        service.signCertificate(null, null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateNull2() {
        service.signCertificate(null, SIGN_REQUESTER_DUMMY);
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateNull3() {
        service.signCertificate(new CertificateSigningRequestDTO(), null);
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateEmpty1() {
        service.signCertificate(new CertificateSigningRequestDTO(), "");
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateEmpty2() {
        service.signCertificate(new CertificateSigningRequestDTO(SIGN_REQUESTER_DUMMY), "");
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateEmpty3() {
        service.signCertificate(new CertificateSigningRequestDTO(""), SIGN_REQUESTER_DUMMY);
    }

    @Test(expected = BadPayloadException.class)
    public void testSignCertificateInvalidString() {
        service.signCertificate(new CertificateSigningRequestDTO("Invalid CSR"), SIGN_REQUESTER_DUMMY);
    }

    @Test
    public void testSignCertificateValidBase64DerCsr() throws IOException {
        final CertificateSigningRequestDTO request = buildRequest("certificates/consumer.csr");

        @SuppressWarnings("unused")
		final CertificateSigningResponseDTO response = service.signCertificate(request, SIGN_REQUESTER_VALID);

        verify(caCertificateDBService).saveCertificateInfo(eq(CONSUMER_CN), any(), eq(SIGN_REQUESTER_VALID), any(), any());
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateProtectedBase64DerCsr() throws IOException {
        final CertificateSigningRequestDTO request = buildRequest("certificates/sysop.csr");

        service.signCertificate(request, SIGN_REQUESTER_VALID);

        verify(caCertificateDBService, never()).saveCertificateInfo(anyString(), any(), anyString(), any(), any());
    }

    @Test
    public void testSignCertificateProtectedBase64DerCsrSysopRequest() throws IOException {
        final CertificateSigningRequestDTO request = buildRequest("certificates/sysop.csr");

        final CertificateSigningResponseDTO response = service.signCertificate(request, SIGN_REQUESTER_SYSOP);

        verify(caCertificateDBService).saveCertificateInfo(eq(SYSOP_CN), any(), eq(SIGN_REQUESTER_SYSOP), any(), any());
        verifyCertSigningResponse(response, SYSOP_CN);
    }

    @Test
    public void testSignCertificateValidBase64DerCsrValidityOK() throws IOException {
        final ZonedDateTime now = ZonedDateTime.now();
        final CertificateSigningRequestDTO request = buildRequest("certificates/consumer.csr", now, now.plusMinutes(1));

        final CertificateSigningResponseDTO response = service.signCertificate(request, SIGN_REQUESTER_VALID);

        verify(caCertificateDBService).saveCertificateInfo(eq(CONSUMER_CN), any(), eq(SIGN_REQUESTER_VALID), any(), any());
        verifyCertSigningResponse(response, CONSUMER_CN);
    }

    @Test(expected = InvalidParameterException.class)
    public void testSignCertificateValidBase64DerCsrValidityNotOK() throws IOException {
        final ZonedDateTime now = ZonedDateTime.now();
        final CertificateSigningRequestDTO request = buildRequest("certificates/consumer.csr", now, now.plusYears(1));

        service.signCertificate(request, SIGN_REQUESTER_VALID);

        verify(caCertificateDBService, never()).saveCertificateInfo(anyString(), any(), anyString(), any(), any());
    }

    private X509Certificate verifyCertSigningResponse(final CertificateSigningResponseDTO response, final String commonName) {
        assertNotNull(response);
        assertNotNull(commonName);
        assertEquals(response.getId(), CA_CERT_ID);

        final List<String> certificateChain = response.getCertificateChain();
        assertNotNull(certificateChain);
        assertEquals(certificateChain.size(), 3);

        final X509Certificate clientCert = CertificateAuthorityUtils.decodeCertificate(certificateChain.get(0));
        assertEquals(CertificateAuthorityUtils.getCommonName(clientCert), commonName);
        assertEquals(CertificateAuthorityUtils.decodeCertificate(certificateChain.get(1)), cloudCertificate);
        assertEquals(CertificateAuthorityUtils.decodeCertificate(certificateChain.get(2)), rootCertificate);

        return clientCert;
    }

    // ------------------------------------------------------------------------

    @Test
    public void testCheckTrustedKey() {
        final TrustedKeyCheckRequestDTO request = new TrustedKeyCheckRequestDTO();
        final TrustedKeyCheckResponseDTO responseDTO = new TrustedKeyCheckResponseDTO();
        when(caTrustedKeyDBService.isTrustedKeyValidNow(request)).thenReturn(responseDTO);

        final TrustedKeyCheckResponseDTO response = service.checkTrustedKey(request);
        verify(caTrustedKeyDBService).isTrustedKeyValidNow(eq(request));
        assertEquals(response, responseDTO);
    }

    // ------------------------------------------------------------------------

    @Test
    public void testAddTrustedKey() {
        final AddTrustedKeyRequestDTO request = new AddTrustedKeyRequestDTO();
        final AddTrustedKeyResponseDTO responseDTO = new AddTrustedKeyResponseDTO(TRUSTED_KEY_ID);
        doReturn(responseDTO).when(caTrustedKeyDBService)
                             .addTrustedKey(request);

        final AddTrustedKeyResponseDTO response = service.addTrustedKey(request);
        verify(caTrustedKeyDBService).addTrustedKey(eq(request));

        assertNotNull(response);
        assertEquals(response, responseDTO);
    }

    // ------------------------------------------------------------------------

    @Test
    public void testGetTrustedKeys() {
        final TrustedKeysResponseDTO responseDTO = new TrustedKeysResponseDTO();
        when(caTrustedKeyDBService.getTrustedKeyEntries(anyInt(), anyInt(), any(), anyString()))
                .thenReturn(responseDTO);

        final TrustedKeysResponseDTO response = service.getTrustedKeys(1, 10, Sort.Direction.ASC, "id");
        verify(caTrustedKeyDBService).getTrustedKeyEntries(anyInt(), anyInt(), any(), anyString());

        assertNotNull(response);
        assertEquals(response, responseDTO);
    }

    // ------------------------------------------------------------------------

    @Test
    public void testDeleteTrustedKeys() {
        service.deleteTrustedKey(TRUSTED_KEY_ID);
        verify(caTrustedKeyDBService).deleteTrustedKey(eq(TRUSTED_KEY_ID));
    }
}
