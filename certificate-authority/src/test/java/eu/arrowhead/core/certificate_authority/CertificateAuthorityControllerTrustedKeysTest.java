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
import eu.arrowhead.common.database.entity.CaTrustedKey;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
public class CertificateAuthorityControllerTrustedKeysTest {

    // =================================================================================================
    // members

    private static final String TRUSTED_KEYS_URL = "/certificate-authority/mgmt/keys/";
    private static final String CHECK_TRUSTED_KEY_URL = "/certificate-authority/checkTrustedKey";
    private static final String MOCKED_PUBLIC_KEY = "mockedPublicKey";
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
    public void testTrustedKeyListPost() throws Exception {
        mockMvc.perform(post(TRUSTED_KEYS_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyListPatch() throws Exception {
        mockMvc.perform(patch(TRUSTED_KEYS_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyListDelete() throws Exception {
        mockMvc.perform(delete(TRUSTED_KEYS_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyListHead() throws Exception {
        mockMvc.perform(head(TRUSTED_KEYS_URL))
               .andExpect(status().isOk());
    }

    @Test
    public void testTrustedKeyListOptions() throws Exception {
        mockMvc.perform(options(TRUSTED_KEYS_URL))
               .andExpect(status().isOk());
    }

    @Test
    public void testTrustedKeyListGetWithoutParameter() throws Exception {
        final int PAGE_SIZE = 5;
        final Page<CaTrustedKey> trustedKeyEntryList = createTrustedKeysPageForDBMocking(PAGE_SIZE);
        final TrustedKeysResponseDTO trustedKeyEntriesDTO = DTOConverter.convertCaTrustedKeyListToTrustedKeysResponseDTO(trustedKeyEntryList);

        when(serviceCertificateAuthorityService.getTrustedKeys(anyInt(), anyInt(), any(), any())).thenReturn(trustedKeyEntriesDTO);

        final MvcResult response = this.mockMvc.perform(get(TRUSTED_KEYS_URL).accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final TrustedKeysResponseDTO responseBody =
                objectMapper.readValue(response.getResponse()
                                               .getContentAsString(), TrustedKeysResponseDTO.class);

        assertEquals(PAGE_SIZE, responseBody.getCount());
        assertEquals(PAGE_SIZE, responseBody.getTrustedKeys()
                                            .size());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testTrustedKeyListGetWithPageAndSizeParameter() throws Exception {
        final int PAGE_SIZE = 5;
        final Page<CaTrustedKey> trustedKeyEntryList = createTrustedKeysPageForDBMocking(PAGE_SIZE);
        final TrustedKeysResponseDTO trustedKeyEntriesDTO = DTOConverter.convertCaTrustedKeyListToTrustedKeysResponseDTO(trustedKeyEntryList);

        when(serviceCertificateAuthorityService.getTrustedKeys(anyInt(), anyInt(), any(), any()))
                .thenReturn(trustedKeyEntriesDTO);

        final MvcResult response = this.mockMvc.perform(get(TRUSTED_KEYS_URL).param(PAGE, "0")
                                                                             .param(ITEM_PER_PAGE, "" + PAGE_SIZE)
                                                                             .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final TrustedKeysResponseDTO responseBody = objectMapper.readValue(response.getResponse()
                                                                                   .getContentAsString(), TrustedKeysResponseDTO.class);

        assertEquals(PAGE_SIZE, responseBody.getCount());
        assertEquals(PAGE_SIZE, responseBody.getTrustedKeys()
                                            .size());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testTrustedKeyListGetWithNullPageButDefinedSizeParameter() throws Exception {
        this.mockMvc.perform(get(TRUSTED_KEYS_URL).param(ITEM_PER_PAGE, "1")
                                                  .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testTrustedKeyListGetWithDefinedPageButNullSizeParameter() throws Exception {
        this.mockMvc.perform(get(TRUSTED_KEYS_URL).param(PAGE, "0")
                                                  .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testTrustedKeyListGetWithInvalidSortDirectionFlagParametert() throws Exception {
        this.mockMvc.perform(get(TRUSTED_KEYS_URL).param("direction", "invalid")
                                                  .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddTrustedKeyInvalid() throws Exception {
        doThrow(new ArrowheadException("dummy")).when(serviceCertificateAuthorityService)
                                                .addTrustedKey(any());

        mockMvc.perform(put(TRUSTED_KEYS_URL))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddTrustedKeyValid() throws Exception {
        final String now = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        final AddTrustedKeyRequestDTO requestDTO = new AddTrustedKeyRequestDTO(MOCKED_PUBLIC_KEY, "dummy", now, now);

        mockMvc.perform(put(TRUSTED_KEYS_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                             .content(asJsonString(requestDTO)))
               .andExpect(status().isCreated());
    }

    @Test
    public void testTrustedKeyByIdGet() throws Exception {
        mockMvc.perform(get(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyByIdPost() throws Exception {
        mockMvc.perform(post(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyByIdPut() throws Exception {
        mockMvc.perform(put(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyByIdPatch() throws Exception {
        mockMvc.perform(patch(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyByIdHead() throws Exception {
        mockMvc.perform(head(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testTrustedKeyByIdOptions() throws Exception {
        mockMvc.perform(options(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isOk());
    }

    @Test
    public void testTrustedKeyByIdDeleteValid() throws Exception {
        mockMvc.perform(delete(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isNoContent());
    }

    @Test
    public void testTrustedKeyByIdDeleteInvalid() throws Exception {

        doThrow(new InvalidParameterException("dummy")).when(serviceCertificateAuthorityService)
                                                       .deleteTrustedKey(anyLong());

        mockMvc.perform(delete(TRUSTED_KEYS_URL + 1))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void testTrustedKeyByIdDeleteInvalidZeroOrNegative() throws Exception {

        verify(serviceCertificateAuthorityService, never()).deleteTrustedKey(anyLong());

        mockMvc.perform(delete(TRUSTED_KEYS_URL + 0))
               .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------

    @Test
    public void testCheckTrustedKeyGet() throws Exception {
        mockMvc.perform(get(CHECK_TRUSTED_KEY_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckTrustedKeyPut() throws Exception {
        mockMvc.perform(put(CHECK_TRUSTED_KEY_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckTrustedKeyPatch() throws Exception {
        mockMvc.perform(patch(CHECK_TRUSTED_KEY_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckTrustedKeyHead() throws Exception {
        mockMvc.perform(head(CHECK_TRUSTED_KEY_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckTrustedKeyOptions() throws Exception {
        mockMvc.perform(options(CHECK_TRUSTED_KEY_URL))
               .andExpect(status().isOk());
    }

    @Test
    public void testCheckTrustedKeyDelete() throws Exception {
        mockMvc.perform(delete(CHECK_TRUSTED_KEY_URL))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCheckTrustedKeyWithEmptyParam() throws Exception {

        this.mockMvc.perform(post(CHECK_TRUSTED_KEY_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                                        .content(""))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckTrustedKeyWithEmptyCertParam() throws Exception {

        final TrustedKeyCheckRequestDTO request = new TrustedKeyCheckRequestDTO("");

        this.mockMvc.perform(post(CHECK_TRUSTED_KEY_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                                        .content(asJsonString(request)))
                    .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testCheckTrustedKeyWithValidParam() throws Exception {

        final TrustedKeyCheckRequestDTO request = new TrustedKeyCheckRequestDTO(MOCKED_PUBLIC_KEY);

        final TrustedKeyCheckResponseDTO responseDTO = new TrustedKeyCheckResponseDTO(1,
                Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()), "");

        when(serviceCertificateAuthorityService.checkTrustedKey(any())).thenReturn(responseDTO);

        final MvcResult response =
                this.mockMvc.perform(post(CHECK_TRUSTED_KEY_URL).contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                .accept(MediaType.APPLICATION_JSON_UTF8)
                                                                .content(asJsonString(request)))
                            .andExpect(status().isOk())
                            .andReturn();
        @SuppressWarnings("unused")
		final TrustedKeyCheckResponseDTO responseBody =
                objectMapper.readValue(response.getResponse()
                                               .getContentAsString(), TrustedKeyCheckResponseDTO.class);
    }

    // =================================================================================================
    // assistant methods

    // -------------------------------------------------------------------------------------------------
    private Page<CaTrustedKey> createTrustedKeysPageForDBMocking(final int amountOfEntry) {
        final List<CaTrustedKey> trustedKeyList = new ArrayList<>(amountOfEntry);

        for (int i = 0; i < amountOfEntry; ++i) {
            final ZonedDateTime timeStamp = ZonedDateTime.now();
            final CaTrustedKey key = new CaTrustedKey();
            key.setId(i);
            key.setPublicKey(MOCKED_PUBLIC_KEY + i);
            key.setHash("" + i);
            key.setDescription("" + i);
            key.setCreatedAt(timeStamp);
            key.setUpdatedAt(timeStamp);
            key.setValidAfter(timeStamp);
            key.setValidBefore(timeStamp);
            trustedKeyList.add(key);
        }

        return new PageImpl<>(trustedKeyList);
    }

    // -------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
	private CaTrustedKey createTrustedKeyForDBMocking() {
        final ZonedDateTime timeStamp = ZonedDateTime.now();
        final CaTrustedKey key = new CaTrustedKey();
        key.setId(1);
        key.setPublicKey(MOCKED_PUBLIC_KEY);
        key.setHash("");
        key.setDescription("");
        key.setCreatedAt(timeStamp);
        key.setUpdatedAt(timeStamp);
        key.setValidAfter(timeStamp);
        key.setValidBefore(timeStamp);
        return key;
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
