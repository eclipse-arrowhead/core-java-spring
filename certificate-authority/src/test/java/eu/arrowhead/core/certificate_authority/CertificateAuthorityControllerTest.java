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
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CertificateAuthorityMain.class)
@ContextConfiguration(classes = {CertificateAuthorityServiceTestContext.class})
public class CertificateAuthorityControllerTest {

    public static final String SIGN_CERTIFICATE_URI = "/certificate-authority/sign";
    public static final String COMMON_NAME_URI = "/certificate-authority/name";
    public static final String ECHO_URI = "/certificate-authority/echo";

    @MockBean(name = "mockCertificateAuthorityService")
    CertificateAuthorityService serviceCertificateAuthorityService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                                      .build();
    }

    @Test
    public void testEcho() throws Exception {
        mockMvc.perform(get(ECHO_URI))
               .andExpect(status().isOk());
    }

    @Test
    public void testSignGet() throws Exception {
        mockMvc.perform(get(SIGN_CERTIFICATE_URI))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignPut() throws Exception {
        mockMvc.perform(put(SIGN_CERTIFICATE_URI))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignPatch() throws Exception {
        mockMvc.perform(patch(SIGN_CERTIFICATE_URI))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignDelete() throws Exception {
        mockMvc.perform(delete(SIGN_CERTIFICATE_URI))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignHead() throws Exception {
        mockMvc.perform(head(SIGN_CERTIFICATE_URI))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignOptions() throws Exception {
        mockMvc.perform(options(SIGN_CERTIFICATE_URI))
               .andExpect(status().isOk());
    }

    @Test
    public void testSignPostInvalidContentType() throws Exception {
        mockMvc.perform(post(SIGN_CERTIFICATE_URI))
               .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testSignPostWithNullCSR() throws Exception {
        mockMvc.perform(post(SIGN_CERTIFICATE_URI).contentType(MediaType.APPLICATION_JSON)
                                                  .accept(MediaType.APPLICATION_JSON)
                                                  .content(asJsonString(new CertificateSigningRequestDTO(null))))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignPostWithInvalidCSR() throws Exception {
        mockMvc.perform(post(SIGN_CERTIFICATE_URI).contentType(MediaType.APPLICATION_JSON)
                                                  .accept(MediaType.APPLICATION_JSON)
                                                  .content(asJsonString(new CertificateSigningRequestDTO("INVALID"))))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignPostWithValidCSR() throws Exception {

        final CertificateSigningResponseDTO response = new CertificateSigningResponseDTO(0, new ArrayList<>());
        when(serviceCertificateAuthorityService.signCertificate(any(), anyString())).thenReturn(response);

        mockMvc.perform(post(SIGN_CERTIFICATE_URI).secure(true)
                                                  .with(x509("certificates/sysop.pem"))
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .accept(MediaType.APPLICATION_JSON)
                                                  .content(asJsonString(new CertificateSigningRequestDTO(getResourceContent("certificates/sysop.csr")))))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.certificateChain").isArray());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getResourceContent(String resourcePath) throws IOException {
        File resource = new ClassPathResource(resourcePath).getFile();
        return new String(Files.readAllBytes(resource.toPath())).trim();
    }
}
