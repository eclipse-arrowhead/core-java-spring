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

package eu.arrowhead.core.eventhandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventHandlerMain.class)
@ContextConfiguration(classes = { EventHandlerTestContext.class })
public class EventHandlerControllerTest {

	//=================================================================================================
	// members

	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockEventHandlerService") 
	EventHandlerService eventHandlerService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void echoTest() throws Exception {
		this.mockMvc.perform(get(CommonConstants.EVENTHANDLER_URI + CommonConstants.ECHO_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());		
	}
	
	//=================================================================================================
	// Test of subscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionTest() throws Exception {
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(getSubscriptionRequestDTOForTest()))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isOk())
											 .andReturn();
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullEventTypeTest() throws Exception {
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(getSubscriptionRequestDTOWithNullEventTypeForTest()))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithEmptyEventTypeTest() throws Exception {
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(getSubscriptionRequestDTOWithEmptyEventTypeForTest()))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullSubscriberSystemTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSubscriberSystem(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullSubscriberSystemNameTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithEmptySubscriberSystemNameTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName("   ");
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullSubscriberSystemAddressTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System address is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithEmptySubscriberSystemAddressTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress("   ");
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
										 	 .andExpect(status().isBadRequest())
										 	 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System address is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullSubscriberSystemPortTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System port is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithInvalidSubscriberSystemPortTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort(-1);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System port must be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullNotifyUriTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.NotifyUri is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithEmptyNotifyUriTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri("   ");
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.NotifyUri is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithMetaDataTrueButNullFilterMetaDataTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData(true);
		request.setFilterMetaData(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
										 	 .andExpect(status().isBadRequest())
										 	 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.MatchMetaData is true but Request.FilterMetaData is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithMetaDataTrueButEmptyFilterMetaDataTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData(true);
		request.setFilterMetaData(new HashMap<>(0));
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.MatchMetaData is true but Request.FilterMetaData is null or blank."));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullSourcesTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(null);
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isOk())
											 .andReturn();
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithEmptySourcesTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of());
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isOk())
											 .andReturn();
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithSourcesWithValidSourceTest() throws Exception {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of(getSystemRequestDTO()));
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											.contentType(MediaType.APPLICATION_JSON)
											.content(objectMapper.writeValueAsBytes(request))
											.accept(MediaType.APPLICATION_JSON))
											.andExpect(status().isOk())
											.andReturn();
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithSourcesWithInValidSourceTest() throws Exception {
		final SystemRequestDTO invalidSource = getSystemRequestDTO();
		invalidSource.setSystemName(null);
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of(invalidSource));
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscriptionIsOkTest() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", "a", 1);
		doNothing().when(eventHandlerService).unsubscribe(any(), any(), any(), anyInt() );

		deleteSubscription(queryStr, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isOk())
											 .andReturn();
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishNullEventTypeTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType(null);
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyEventTypeTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType("   ");
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishNullTimeStampTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp(null);
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyTimeStampTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("   ");
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isOk())
											 .andReturn();
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishInvalidSourceTest() throws Exception {
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setSystemName(null);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishNullPayloadTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload(null);
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
										 	 .contentType(MediaType.APPLICATION_JSON)
										 	 .content(objectMapper.writeValueAsBytes(request))
										 	 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.Payload is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyPayloadTest() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload("   ");
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH)
											 .contentType(MediaType.APPLICATION_JSON)
											 .content(objectMapper.writeValueAsBytes(request))
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.Payload is null or blank."));	
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOForTest() {
		return new SubscriptionRequestDTO(
				"eventType", 
				getSystemRequestDTO(), 
				null, // filterMetaData
				"notifyUri", 
				false, // matchMetaData
				null, // startDate
				null, // endDate, 
				null); // sources
	}
	
	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOWithNullEventTypeForTest() {
		return new SubscriptionRequestDTO(
				null, // eventType
				getSystemRequestDTO(), 
				null, // filterMetaData
				"notifyUri", 
				false, // matchMetaData
				null, // startDate
				null, // endDate, 
				null); //sources
	}
	
	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOWithEmptyEventTypeForTest() {
		return new SubscriptionRequestDTO(
				"   ", // eventType
				getSystemRequestDTO(), 
				null, // filterMetaData
				"notifyUri", 
				false, // matchMetaData
				null, // startDate
				null, // endDate, 
				null); // sources
	}

	//-------------------------------------------------------------------------------------------------	
	private SystemRequestDTO getSystemRequestDTO() {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setSystemName("systemName");
		systemRequestDTO.setAddress("localhost");
		systemRequestDTO.setPort(12345);	
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		return new EventPublishRequestDTO(
				"eventType", 
				getSystemRequestDTO(), // source, 
				null, // metaData, 
				"payload", 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private String createQueryStringForUnregister(final String eventType, final String subscriberName, final String subscriberAddress, final Integer subscriberPort) {
		final StringBuilder sb = new StringBuilder();
		
		if (eventType != null) {
			sb.append(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE).append("=").append( eventType ).append("&");
		}                               
		                                
		if (subscriberName != null) {      
			sb.append(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME).append("=").append( subscriberName ).append("&");
		}                                
		                                 
		if (subscriberAddress != null) {   
			sb.append(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS).append("=").append( subscriberAddress ).append("&");
		}                                
		                                 
		if (subscriberPort != null) {      
			sb.append(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT).append("=").append( subscriberPort.intValue()).append("&");
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult deleteSubscription(final String queryStr, final ResultMatcher matcher) throws Exception {
		final String validatedQueryStr = Utilities.isEmpty(queryStr) ? "" : "?" + queryStr.trim();
		return this.mockMvc.perform(delete(CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE + validatedQueryStr)
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
}