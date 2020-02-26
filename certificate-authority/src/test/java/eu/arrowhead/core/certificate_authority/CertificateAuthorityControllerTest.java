package eu.arrowhead.core.certificate_authority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CertificateAuthorityMain.class)
@AutoConfigureMockMvc
public class CertificateAuthorityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEchoWithoutCert() throws Exception {
        mockMvc.perform(get("/certificate-authority/echo")
                .secure(true))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testEchoWithInvalidCert() throws Exception {
        mockMvc.perform(get("/certificate-authority/echo")
                .secure(true)
                .with(x509("certificates/notvalid.pem")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testEchoWithValidCert() throws Exception {
        mockMvc.perform(get("/certificate-authority/echo")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCloudCommonNameWithoutCert() throws Exception {
        mockMvc.perform(get("/certificate-authority/name")
                .secure(true))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetCloudCommonNameWithInvalidCert() throws Exception {
        mockMvc.perform(get("/certificate-authority/name")
                .secure(true)
                .with(x509("certificates/notvalid.pem")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetCloudCommonNameWithValidCert() throws Exception {
        mockMvc.perform(get("/certificate-authority/name")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("testcloud2.aitia.arrowhead.eu"));
    }

    @Test
    public void testSignWithoutCert() throws Exception {
        mockMvc.perform(post("/certificate-authority/sign")
                .secure(true))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSignWithInvalidCert() throws Exception {
        mockMvc.perform(post("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/notvalid.pem")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSignGet() throws Exception {
        mockMvc.perform(get("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignPut() throws Exception {
        mockMvc.perform(put("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignPatch() throws Exception {
        mockMvc.perform(patch("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignDelete() throws Exception {
        mockMvc.perform(delete("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignHead() throws Exception {
        mockMvc.perform(head("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSignOptions() throws Exception {
        mockMvc.perform(options("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isOk());
    }

    @Test
    public void testSignPostInvalidContentType() throws Exception {
        mockMvc.perform(post("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem")))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testSignPostWithValidCSR() throws Exception {
        mockMvc.perform(post("/certificate-authority/sign")
                .secure(true)
                .with(x509("certificates/valid.pem"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CertificateSigningRequestDTO(getResourceContent("certificates/valid.csr")))))
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
