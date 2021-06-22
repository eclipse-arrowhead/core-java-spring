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

package eu.arrowhead.core.certificate_authority.security;

import eu.arrowhead.common.CommonConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the
 * src/main/resources folder.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class CAAccessControlFilterTest {

    // =================================================================================================
    // members

    private static final String CA_ECHO = CommonConstants.CERTIFICATEAUTHRORITY_URI + CommonConstants.ECHO_URI;
    private static final String CA_MGMT_CERTIFICATES = CommonConstants.CERTIFICATEAUTHRORITY_URI
            + CommonConstants.OP_CA_MGMT_CERTIFICATES_URI;
    private static final String CA_MGMT_TRUSTED_KEYS = CommonConstants.CERTIFICATEAUTHRORITY_URI
            + CommonConstants.OP_CA_MGMT_TRUSTED_KEYS_URI;
    private static final String CA_CHECK_CERTIFICATE = CommonConstants.CERTIFICATEAUTHRORITY_URI
            + CommonConstants.OP_CA_CHECK_CERTIFICATE_URI;
    private static final String CA_CHECK_TRUSTED_KEY = CommonConstants.CERTIFICATEAUTHRORITY_URI
            + CommonConstants.OP_CA_CHECK_TRUSTED_KEY_URI;
    private static final String CA_SIGN = CommonConstants.CERTIFICATEAUTHRORITY_URI
            + CommonConstants.OP_CA_SIGN_CERTIFICATE_URI;

    @Autowired
    private ApplicationContext appContext;

    @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
    private boolean secure;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    // -------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        assumeTrue(secure);

        final CAAccessControlFilter acFilter = appContext.getBean(CAAccessControlFilter.class);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                                      .apply(springSecurity())
                                      .addFilters(acFilter)
                                      .build();
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testEchoCertificateInvalid() throws Exception {
        this.mockMvc.perform(get(CA_ECHO).secure(true)
                                         .with(x509("certificates/notvalid.pem"))
                                         .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testEchoCertificateValid() throws Exception {
        this.mockMvc.perform(get(CA_ECHO).secure(true)
                                         .with(x509("certificates/valid.pem"))
                                         .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckCertificateCertificateInvalid() throws Exception {
        this.mockMvc.perform(post(CA_CHECK_CERTIFICATE).secure(true)
                                                       .with(x509("certificates/notvalid.pem"))
                                                       .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckCertificateCertificateValid() throws Exception {
        // Filter enables the access but we use ill-formed input to make sure real operation never happens
        this.mockMvc.perform(post(CA_CHECK_CERTIFICATE).secure(true)
                                                       .with(x509("certificates/valid.pem"))
                                                       .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                       .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testSignCertificateCertificateInvalid() throws Exception {
        this.mockMvc.perform(post(CA_SIGN).secure(true)
                                          .with(x509("certificates/notvalid.pem"))
                                          .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testSignCertificateCertificateValid() throws Exception {
        this.mockMvc.perform(post(CA_SIGN).secure(true)
                                          .with(x509("certificates/valid.pem"))
                                          .contentType(MediaType.APPLICATION_JSON_UTF8)
                                          .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testSignCertificateCertificateSysop() throws Exception {
        // Filter enables the access but we use ill-formed input to make sure real operation never happens
        this.mockMvc.perform(post(CA_SIGN).secure(true)
                                          .with(x509("certificates/sysop.pem"))
                                          .contentType(MediaType.APPLICATION_JSON_UTF8)
                                          .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckTrustedKeyCertificateInvalid() throws Exception {
        this.mockMvc.perform(post(CA_CHECK_TRUSTED_KEY).secure(true)
                                                       .with(x509("certificates/notvalid.pem"))
                                                       .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckTrustedKeyCertificateValid() throws Exception {
        this.mockMvc.perform(post(CA_CHECK_TRUSTED_KEY).secure(true)
                                                       .with(x509("certificates/valid.pem"))
                                                       .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                       .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }


    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckTrustedKeyCertificateSysop() throws Exception {
        // Filter enables the access but we use ill-formed input to make sure real operation never happens
        this.mockMvc.perform(post(CA_CHECK_TRUSTED_KEY).secure(true)
                                                       .with(x509("certificates/sysop.pem"))
                                                       .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                       .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListCertificateInvalid() throws Exception {
        this.mockMvc.perform(get(CA_MGMT_CERTIFICATES).secure(true)
                                                      .with(x509("certificates/notvalid.pem"))
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListCertificateValid() throws Exception {
        this.mockMvc.perform(get(CA_MGMT_CERTIFICATES).secure(true)
                                                      .with(x509("certificates/valid.pem"))
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCertificatesListCertificateSysop() throws Exception {
        // Filter enables the access but we use ill-formed input to make sure real operation never happens
        this.mockMvc.perform(get(CA_MGMT_CERTIFICATES).secure(true)
                                                      .with(x509("certificates/sysop.pem"))
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testRevokeCertificateCertificateInvalid() throws Exception {
        this.mockMvc.perform(delete(CA_MGMT_CERTIFICATES + "/0").secure(true)
                                                                .with(x509("certificates/notvalid.pem"))
                                                                .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testRevokeCertificateCertificateValid() throws Exception {
        this.mockMvc.perform(delete(CA_MGMT_CERTIFICATES + "/0").secure(true)
                                                                .with(x509("certificates/valid.pem"))
                                                                .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testRevokeCertificateCertificateSysop() throws Exception {
        // Filter enables the access but we use ill-formed input to make sure real operation never happens
        this.mockMvc.perform(delete(CA_MGMT_CERTIFICATES + "/0").secure(true)
                                                                .with(x509("certificates/sysop.pem"))
                                                                .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testGetTrustedKeysCertificateInvalid() throws Exception {
        this.mockMvc.perform(get(CA_MGMT_TRUSTED_KEYS).secure(true)
                                                      .with(x509("certificates/notvalid.pem"))
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testGetTrustedKeysCertificateValid() throws Exception {
        this.mockMvc.perform(get(CA_MGMT_TRUSTED_KEYS).secure(true)
                                                      .with(x509("certificates/valid.pem"))
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testGetTrustedKeysCertificateSysop() throws Exception {
        this.mockMvc.perform(get(CA_MGMT_TRUSTED_KEYS).secure(true)
                                                      .with(x509("certificates/sysop.pem"))
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testAddTrustedKeyCertificateInvalid() throws Exception {
        this.mockMvc.perform(put(CA_MGMT_TRUSTED_KEYS).secure(true)
                                                      .with(x509("certificates/notvalid.pem"))
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testAddTrustedKeyCertificateValid() throws Exception {
        this.mockMvc.perform(put(CA_MGMT_TRUSTED_KEYS).secure(true)
                                                      .with(x509("certificates/valid.pem"))
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testAddTrustedKeyCertificateSysop() throws Exception {
        this.mockMvc.perform(put(CA_MGMT_TRUSTED_KEYS).secure(true)
                                                      .with(x509("certificates/sysop.pem"))
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                      .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testDeleteTrustedKeyCertificateInvalid() throws Exception {
        this.mockMvc.perform(delete(CA_MGMT_TRUSTED_KEYS + "/1").secure(true)
                                                                .with(x509("certificates/notvalid.pem"))
                                                                .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testDeleteTrustedKeyCertificateValid() throws Exception {
        this.mockMvc.perform(delete(CA_MGMT_TRUSTED_KEYS + "/1").secure(true)
                                                                .with(x509("certificates/valid.pem"))
                                                                .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testDeleteTrustedKeyCertificateSysop() throws Exception {
        this.mockMvc.perform(delete(CA_MGMT_TRUSTED_KEYS + "/0").secure(true)
                                                                .with(x509("certificates/sysop.pem"))
                                                                .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest());
    }
}
