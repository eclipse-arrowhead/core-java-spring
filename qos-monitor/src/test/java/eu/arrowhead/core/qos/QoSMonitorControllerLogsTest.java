/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.qos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = QoSMonitorMain.class)
@ContextConfiguration (classes = { QoSMonitorTestContext.class })
public class QoSMonitorControllerLogsTest {

	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
		
	@MockBean(name = "mockCommonDBService") 
	private CommonDBService commonDBService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final String LOGS_URL = "/qosmonitor/mgmt/logs";
	private static final String PAGE = "page";
	private static final String ITEM_PER_PAGE = "item_per_page";
	private static final String DIRECTION = "direction";
	private static final String FROM = "from";
	private static final String TO = "to";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithNullPageButDefinedSizeParameter() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(ITEM_PER_PAGE, "1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(LOGS_URL, error.getOrigin());
		Assert.assertEquals("Defined page or size could not be with undefined size or page.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithDefinedPageButNullSizeParameter() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(PAGE, "0")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(LOGS_URL, error.getOrigin());
		Assert.assertEquals("Defined page or size could not be with undefined size or page.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithInvalidDirectionParameter() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(DIRECTION, "invalid")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(LOGS_URL, error.getOrigin());
		Assert.assertEquals("Invalid sort direction flag", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithInvalidFromParameter() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(FROM, "invalid")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(LOGS_URL, error.getOrigin());
		Assert.assertEquals("Invalid time parameter", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithInvalidToParameter() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(TO, "invalid")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(LOGS_URL, error.getOrigin());
		Assert.assertEquals("Invalid time parameter", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithInvalidInterval() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(FROM, "2021-11-24T14:22:11Z")
											   .param(TO, "2021-11-24T14:12:11Z")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(LOGS_URL, error.getOrigin());
		Assert.assertEquals("Invalid time interval", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithoutParameter() throws Exception  {
		final Logs entry = new Logs("log", ZonedDateTime.now(), "logger", CoreSystem.QOSMONITOR, LogLevel.INFO, "test", null);
		final Page<Logs> page = new PageImpl<>(List.of(entry));

		when(commonDBService.getLogEntriesResponse(anyInt(), anyInt(), any(Direction.class), anyString(), eq(CoreSystem.QOSMONITOR), isNull(), isNull(), isNull(), isNull())).thenReturn(DTOConverter.convertLogsPageToLogEntryListResponseDTO(page));
		
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
						 					   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final LogEntryListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), LogEntryListResponseDTO.class);

		Assert.assertEquals(1, responseBody.getData().size());
		Assert.assertEquals("log", responseBody.getData().get(0).getLogId());
		
		verify(commonDBService, times(1)).getLogEntriesResponse(0, Integer.MAX_VALUE, Direction.ASC, "logId", CoreSystem.QOSMONITOR, null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void testGetLogEntriesWithAllParameters() throws Exception  {
		final ZonedDateTime from = Utilities.parseUTCStringToLocalZonedDateTime("2021-11-24T14:12:11Z");
		final ZonedDateTime to = Utilities.parseUTCStringToLocalZonedDateTime("2021-11-24T14:22:11Z");
		final List<LogLevel> logLevels = List.of(LogLevel.ERROR, LogLevel.FATAL, LogLevel.OFF);
		
		final Logs entry = new Logs("log", from.plusSeconds(10), "logger", CoreSystem.QOSMONITOR, LogLevel.INFO, "test", null);
		final Page<Logs> page = new PageImpl<>(List.of(entry));

		when(commonDBService.getLogEntriesResponse(anyInt(), anyInt(), any(Direction.class), anyString(), eq(CoreSystem.QOSMONITOR), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class), anyString())).thenReturn(DTOConverter.convertLogsPageToLogEntryListResponseDTO(page));
		
		final MvcResult response = this.mockMvc.perform(get(LOGS_URL)
											   .param(PAGE, "0")
											   .param(ITEM_PER_PAGE, "5")
											   .param(DIRECTION, "desc")
											   .param("sort_field", "entryDate")
											   .param("level", "ERROR")
											   .param(FROM, "2021-11-24T14:12:11Z")
											   .param(TO, "2021-11-24T14:22:11Z")
											   .param("logger", "logger")
						 					   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final LogEntryListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), LogEntryListResponseDTO.class);

		Assert.assertEquals(1, responseBody.getData().size());
		Assert.assertEquals("log", responseBody.getData().get(0).getLogId());
		
		verify(commonDBService, times(1)).getLogEntriesResponse(0, 5, Direction.DESC, "entryDate", CoreSystem.QOSMONITOR, logLevels, from, to, "logger");
	}
}