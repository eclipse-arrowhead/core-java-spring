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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.CaCertificate;
import eu.arrowhead.common.dto.internal.CertificateCheckRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.IssuedCertificateStatus;
import eu.arrowhead.common.dto.internal.IssuedCertificatesResponseDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CertificateAuthorityMain.class)
@ContextConfiguration(classes = {CertificateAuthorityServiceTestContext.class})
public class CertificateAuthorityControllerCertificatesTest {

    // =================================================================================================
    // members

    private static final String CERTIFICATES_URL = "/certificate-authority/mgmt/certificates/";
    private static final String CHECK_CERTIFICATE_URL = "/certificate-authority/checkCertificate";
    private static final String MOCKED_CERT_COMMON_NAME = "mockedSystemName";
    private static final String PAGE = "page";
    private static final String ITEM_PER_PAGE = "item_per_page";

    @MockBean(name = "mockCertificateAuthorityService")
    CertificateAuthorityService serviceCertificateAuthorityService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =================================================================================================
    // methods
    // -------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                                      .build();
    }

    @Test
    public void testCertificatesListPost() throws Exception {
        mockMvc.perform(post(CERTIFICATES_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificatesListPut() throws Exception {
        mockMvc.perform(put(CERTIFICATES_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificatesListPatch() throws Exception {
        mockMvc.perform(patch(CERTIFICATES_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificatesListDelete() throws Exception {
        mockMvc.perform(delete(CERTIFICATES_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificatesListHead() throws Exception {
        mockMvc.perform(head(CERTIFICATES_URL))
               .andExpect(status().isOk());
    }

    @Test
    public void testCertificatesListOptions() throws Exception {
        mockMvc.perform(options(CERTIFICATES_URL))
               .andExpect(status().isOk());
    }

    @Test
    public void testCertificatesListGetWithoutParameter() throws Exception {
        final int PAGE_SIZE = 5;
        final Page<CaCertificate> certificateEntryList = createCertificatesPageForDBMocking(PAGE_SIZE);
        final IssuedCertificatesResponseDTO certificateEntriesDTO = DTOConverter.convertCaCertificateListToIssuedCertificatesResponseDTO(certificateEntryList);

        when(serviceCertificateAuthorityService.getCertificates(anyInt(), anyInt(), any(), any())).thenReturn(certificateEntriesDTO);

        final MvcResult response = this.mockMvc.perform(get(CERTIFICATES_URL).accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final IssuedCertificatesResponseDTO responseBody = objectMapper.readValue(response.getResponse()
                                                                                          .getContentAsString(), IssuedCertificatesResponseDTO.class);

        assertEquals(PAGE_SIZE, responseBody.getCount());
        assertEquals(PAGE_SIZE, responseBody.getIssuedCertificates()
                                            .size());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListGetWithPageAndSizeParameter() throws Exception {
        final int PAGE_SIZE = 5;
        final Page<CaCertificate> certificateEntryList = createCertificatesPageForDBMocking(PAGE_SIZE);
        final IssuedCertificatesResponseDTO certificateEntriesDTO = DTOConverter.convertCaCertificateListToIssuedCertificatesResponseDTO(certificateEntryList);

        when(serviceCertificateAuthorityService.getCertificates(anyInt(), anyInt(), any(), any())).thenReturn(certificateEntriesDTO);

        final MvcResult response = this.mockMvc.perform(get(CERTIFICATES_URL).param(PAGE, "0")
                                                                             .param(ITEM_PER_PAGE, "" + PAGE_SIZE)
                                                                             .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final IssuedCertificatesResponseDTO responseBody = objectMapper.readValue(response.getResponse()
                                                                                          .getContentAsString(), IssuedCertificatesResponseDTO.class);

        assertEquals(PAGE_SIZE, responseBody.getCount());
        assertEquals(PAGE_SIZE, responseBody.getIssuedCertificates()
                                            .size());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListGetWithNullPageButDefinedSizeParameter() throws Exception {
        this.mockMvc.perform(get(CERTIFICATES_URL).param(ITEM_PER_PAGE, "1")
                                                  .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListGetWithDefinedPageButNullSizeParameter() throws Exception {
        this.mockMvc.perform(get(CERTIFICATES_URL).param(PAGE, "0")
                                                  .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListGetWithInvalidSortDirectionFlagParametert() throws Exception {
        this.mockMvc.perform(get(CERTIFICATES_URL).param("direction", "invalid")
                                                  .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    @Test
    public void testCertificateByIdGet() throws Exception {
        mockMvc.perform(get(CERTIFICATES_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificateByIdPost() throws Exception {
        mockMvc.perform(post(CERTIFICATES_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificateByIdPut() throws Exception {
        mockMvc.perform(put(CERTIFICATES_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificateByIdPatch() throws Exception {
        mockMvc.perform(patch(CERTIFICATES_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificateByIdHead() throws Exception {
        mockMvc.perform(head(CERTIFICATES_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCertificateByIdOptions() throws Exception {
        mockMvc.perform(options(CERTIFICATES_URL + 1))
               .andExpect(status().isOk());
    }

    @Test
    public void testCertificateByIdDeleteValid() throws Exception {

        when(serviceCertificateAuthorityService.revokeCertificate(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(delete(CERTIFICATES_URL + 1).secure(true)
                                                    .with(x509("certificates/sysop.pem")))
               .andExpect(status().isOk());
    }

    @Test
    public void testCertificateByIdDeleteInvalid() throws Exception {

        when(serviceCertificateAuthorityService.revokeCertificate(anyLong(), anyString())).thenReturn(false);

        mockMvc.perform(delete(CERTIFICATES_URL + 1))
               .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------

    @Test
    public void testCheckCertificateGet() throws Exception {
        mockMvc.perform(get(CHECK_CERTIFICATE_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckCertificatePut() throws Exception {
        mockMvc.perform(put(CHECK_CERTIFICATE_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckCertificatePatch() throws Exception {
        mockMvc.perform(patch(CHECK_CERTIFICATE_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckCertificateHead() throws Exception {
        mockMvc.perform(head(CHECK_CERTIFICATE_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckCertificateOptions() throws Exception {
        mockMvc.perform(options(CHECK_CERTIFICATE_URL))
               .andExpect(status().isOk());
    }

    @Test
    public void testCheckCertificateDelete() throws Exception {
        mockMvc.perform(delete(CHECK_CERTIFICATE_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckCertificateWithEmptyParam() throws Exception {

        this.mockMvc.perform(post(CHECK_CERTIFICATE_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                                        .content(""))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckCertificateWithEmptyCertParam() throws Exception {

        final CertificateCheckRequestDTO request = new CertificateCheckRequestDTO(1, "");

        this.mockMvc.perform(post(CHECK_CERTIFICATE_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                                        .content(asJsonString(request)))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckCertificateWithValidParam() throws Exception {

        final CertificateCheckRequestDTO request = new CertificateCheckRequestDTO(1, MOCKED_CERT_COMMON_NAME);

        final String now = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        final CertificateCheckResponseDTO responseDTO = new CertificateCheckResponseDTO(1, now, now,
                MOCKED_CERT_COMMON_NAME, BigInteger.ONE, IssuedCertificateStatus.GOOD);

        when(serviceCertificateAuthorityService.checkCertificate(any())).thenReturn(responseDTO);

        final MvcResult response = this.mockMvc.perform(post(CHECK_CERTIFICATE_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                                   .accept(MediaType.APPLICATION_JSON_UTF8)
                                                                                   .content(asJsonString(request)))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final CertificateCheckResponseDTO responseBody = objectMapper.readValue(response.getResponse()
                                                                                        .getContentAsString(), CertificateCheckResponseDTO.class);

        assertEquals(request.getVersion(), responseBody.getVersion());
    }

    // =================================================================================================
    // assistant methods

    // -------------------------------------------------------------------------------------------------
    private Page<CaCertificate> createCertificatesPageForDBMocking(final int amountOfEntry) {
        final List<CaCertificate> certificateList = new ArrayList<>(amountOfEntry);

        for (int i = 0; i < amountOfEntry; ++i) {
            final ZonedDateTime timeStamp = ZonedDateTime.now();
            final CaCertificate cert = new CaCertificate();
            cert.setId(i);
            cert.setCommonName(MOCKED_CERT_COMMON_NAME + i);
            cert.setSerial(BigInteger.valueOf(i));
            cert.setCreatedBy(MOCKED_CERT_COMMON_NAME);
            cert.setCreatedAt(timeStamp);
            cert.setUpdatedAt(timeStamp);
            cert.setValidAfter(timeStamp);
            cert.setValidBefore(timeStamp);
            certificateList.add(cert);
        }

        final Page<CaCertificate> entries = new PageImpl<>(certificateList);

        return entries;
    }

    // -------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
	private CaCertificate createCertificateInfoForDBMocking() {
        final ZonedDateTime timeStamp = ZonedDateTime.now();
        final CaCertificate cert = new CaCertificate();
        cert.setId(1);
        cert.setCommonName(MOCKED_CERT_COMMON_NAME);
        cert.setSerial(BigInteger.valueOf(1));
        cert.setCreatedBy(MOCKED_CERT_COMMON_NAME);
        cert.setCreatedAt(timeStamp);
        cert.setUpdatedAt(timeStamp);
        cert.setValidAfter(timeStamp);
        cert.setValidBefore(timeStamp);
        return cert;
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
