package eu.arrowhead.core.eventhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.EventTypeResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventHandlerMain.class)
@ContextConfiguration(classes = { EventHandlerTestContext.class })
public class EventHandlerControllerTest {

	//=================================================================================================
	// members

	private static final String EVENT_HANDLER_MGMT_URI =  CommonConstants.EVENT_HANDLER_URI + CoreCommonConstants.MGMT_URI + "/subscriptions";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockEventHandlerService") 
	EventHandlerService eventHandlerService;
	
	@MockBean(name = "mockEventHandlerDBService") 
	EventHandlerDBService eventHandlerDBService;
	
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

		this.mockMvc.perform(get(CommonConstants.EVENT_HANDLER_URI + CommonConstants.ECHO_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());		
	}
	
	//=================================================================================================
	// Test of subscribe
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeTest() throws Exception {
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getSubscriptionRequestDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullEventTypeTest() throws Exception {
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getSubscriptionRequestDTOWithNullEventTypeForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithEmptyEventTypeTest() throws Exception {
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getSubscriptionRequestDTOWithEmptyEventTypeForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullSubscriberSystemTest() throws Exception {

		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSubscriberSystem( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullSubscriberSystemNameTest() throws Exception {

		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithEmptySubscriberSystemNameTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( "   " );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullSubscriberSystemAddressTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System address is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithEmptySubscriberSystemAddressTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( "   " );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System address is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullSubscriberSystemPortTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System port is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithInvalidSubscriberSystemPortTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( -1 );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System port must be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullNotifyUriTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.NotifyUri is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithEmptyNotifyUriTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( "   " );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.NotifyUri is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullMatchMetaDataTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestWithNullMetaDataDTOForTest();
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithMetaDataTrueButNullFilterMetaDataTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.MatchMetaData is true but Request.FilterMetaData is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithMetaDataTrueButEmptyFilterMetaDataTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( new HashMap<String, String>( 0 ) );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.MatchMetaData is true but Request.FilterMetaData is null or blank."));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithNullSourcesTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( null );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithEmptySourcesTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of() );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithSourcesWithValidSourceTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of( getSystemRequestDTO() ) );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscribeWithSourcesWithInValidSourceTest() throws Exception {
		
		final SystemRequestDTO invalidSource = getSystemRequestDTO();
		invalidSource.setSystemName( null );
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of( invalidSource ) );
		
		doNothing().when(eventHandlerService).subscribe(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//=================================================================================================
	// Test of unsubscribe
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeIsOkTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister("eventType", "systemName", "address", 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isOk() );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeNullEventTypeTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister(null, "systemName", "address", 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isBadRequest() );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeEmptyEventTypeTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister("   ", "systemName", "address", 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isBadRequest() );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeNullSystemNameTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister("eventType", null, "address", 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isBadRequest() );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeEmptySystemNameTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister("eventType", "   ", "address", 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isBadRequest() );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeNullAddressTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister("eventType", "systemName", null, 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isBadRequest() );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void unsubscribeEmptyAddressTest() throws Exception {
		
		final String queryStr = createQueryStringForUnregister("eventType", "systemName", "    ", 1);
		doNothing().when( eventHandlerService ).unsubscribe(any(), any(), any(), anyInt() );

		deleteUnregisterSubscription( queryStr, status().isBadRequest() );

	}
	//=================================================================================================
	// Test of publish
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishTest() throws Exception {
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishNullEventTypeTest() throws Exception {
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType( null );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyEventTypeTest() throws Exception {
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType( "   " );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishNulTimeStampTest() throws Exception {
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( null );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyTimeStampTest() throws Exception {

		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( "   " );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public  void publishInvalidSourceTest() throws Exception {
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setSystemName( null );
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource( systemRequestDTO );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("System name is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishNullPayloadTest() throws Exception {

		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload( null );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.Payload is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyPayloadTest() throws Exception {

		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload( "   " );
		
		doNothing().when(eventHandlerService).publishResponse(any());
		
		final MvcResult result = this.mockMvc.perform( post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.Payload is null or blank."));	
	}
	
	//=================================================================================================
	// Test of getSubscriptions
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionOkTest() throws Exception {

		final int resultSize = 3;
		final SubscriptionListResponseDTO result = createSubscriptionListResponseForDBMock( resultSize );
		
		when( eventHandlerDBService.getSubscriptionsResponse(anyInt(), anyInt(), any(), any()) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI)
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk())
				   .andReturn();
		
		Assert.assertNotNull( response );

		final SubscriptionListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), SubscriptionListResponseDTO.class);
		assertEquals( resultSize, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionWithPageAndSizeParameterOkTest() throws Exception {

		final int resultSize = 3;
		final SubscriptionListResponseDTO result = createSubscriptionListResponseForDBMock( resultSize );
		
		when( eventHandlerDBService.getSubscriptionsResponse(anyInt(), anyInt(), any(), any()) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI)
				   .param("page", "0")
				   .param("item_per_page", String.valueOf( resultSize ))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk())
				   .andReturn();
		
		Assert.assertNotNull( response );

		final SubscriptionListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), SubscriptionListResponseDTO.class);
		assertEquals( resultSize, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionWithNullPageButDefinedSizeParameterTest() throws Exception {

		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI)
				   .param("item_per_page", "1" )
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest())
				   .andReturn();
		
		Assert.assertNotNull( response );

		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains( "Defined page or size could not be with undefined size or page." ));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionWithDefinedPageButNullSizeParameterTest() throws Exception {

		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI)
				   .param("page", "0" )
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest())
				   .andReturn();
		
		Assert.assertNotNull( response );

		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains( "Defined page or size could not be with undefined size or page." ));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionWithInvalidSortDirectionFlagParameterTest() throws Exception {

		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI)
				   .param("direction", "invalid" )
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest())
				   .andReturn();
		
		Assert.assertNotNull( response );

		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains( "Invalid sort direction flag" ));	
	}
	
	//=================================================================================================
	// Test of getSubscriptionById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionByIdOkTest() throws Exception {

		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerDBService.getSubscriptionByIdResponse( anyLong() ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI + "/1")
				   .accept( MediaType.APPLICATION_JSON ))
				   .andExpect( status().isOk() )
				   .andReturn();
		
		Assert.assertNotNull( response );

		final SubscriptionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), SubscriptionResponseDTO.class);
		assertEquals( 1, responseBody.getId() );
		assertTrue( "testSubscriberSystemName".equalsIgnoreCase( responseBody.getSubscriberSystem().getSystemName() ) );
		assertTrue( "testEventType".equalsIgnoreCase( responseBody.getEventType().getEventTypeName() ) );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSubscriptionByIdInvalidIdTest() throws Exception {

		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerDBService.getSubscriptionByIdResponse( anyLong() ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( get(EVENT_HANDLER_MGMT_URI + "/-1")
				   .accept( MediaType.APPLICATION_JSON) )
				   .andExpect( status().isBadRequest() )
				   .andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains( "Id must be greater than 0." ));	
	}
	
	//=================================================================================================
	// Test of getSubscriptionById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void deleteSubscriptionOkTest() throws Exception {

		doNothing().when(eventHandlerDBService).deleteSubscriptionResponse( anyLong() );
		
		final MvcResult response = this.mockMvc.perform( delete(EVENT_HANDLER_MGMT_URI + "/1")
				   .accept( MediaType.APPLICATION_JSON ))
				   .andExpect( status().isOk() )
				   .andReturn();
		
		Assert.assertNotNull( response );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void deleteSubscriptionInvalidIdTest() throws Exception {

		doNothing().when(eventHandlerDBService).deleteSubscriptionResponse( anyLong() );
		
		final MvcResult response = this.mockMvc.perform( delete(EVENT_HANDLER_MGMT_URI + "/-1")
				   .accept( MediaType.APPLICATION_JSON) )
				   .andExpect( status().isBadRequest() )
				   .andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains( "Id must be greater than 0." ));	
	}
	
	//=================================================================================================
	// Test of updateSubscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionOkTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(request))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect( status().isOk() )
				   .andReturn();
		
		Assert.assertNotNull( response );

		final SubscriptionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), SubscriptionResponseDTO.class);
		assertEquals( 1, responseBody.getId() );
		assertTrue( "testSubscriberSystemName".equalsIgnoreCase( responseBody.getSubscriberSystem().getSystemName() ) );
		assertTrue( "testEventType".equalsIgnoreCase( responseBody.getEventType().getEventTypeName() ) );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionInvalidIdTest() throws Exception {

		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/-1")
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(request))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect( status().isBadRequest() )
				   .andReturn();
		
		Assert.assertNotNull( response );
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains( "Id must be greater than 0." ));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullEventTypeTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithNullEventTypeForTest();
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithEmptyEventTypeTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullSubscriberSystemTest() throws Exception {

		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.setSubscriberSystem( null );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullSubscriberSystemNameTest() throws Exception {

		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.getSubscriberSystem().setSystemName( null );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithEmptySubscriberSystemNameTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.getSubscriberSystem().setSystemName( "   " );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullSubscriberSystemAddressTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.getSubscriberSystem().setAddress( null );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System address is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithEmptySubscriberSystemAddressTest() throws Exception {
			
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.getSubscriberSystem().setAddress( "   " );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System address is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullSubscriberSystemPortTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.getSubscriberSystem().setPort( null );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System port is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithInvalidSubscriberSystemPortTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOWithEmptyEventTypeForTest();
		request.getSubscriberSystem().setPort( -1 );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System port must be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullNotifyUriTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();request.setNotifyUri( null );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("Request.NotifyUri is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithEmptyNotifyUriTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( "   " );
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("Request.NotifyUri"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullMatchMetaDataTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestWithNullMetaDataDTOForTest();
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( response );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithMetaDataTrueButNullFilterMetaDataTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( null );
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("Request.MatchMetaData is true but Request.FilterMetaData is null or blank."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithMetaDataTrueButEmptyFilterMetaDataTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( new HashMap<String, String>( 0 ) );
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("Request.MatchMetaData is true but Request.FilterMetaData is null or blank."));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithNullSourcesTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( null );
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( response );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithEmptySourcesTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of() );
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( response );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithSourcesWithValidSourceTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of( getSystemRequestDTO() ) );
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( response );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSubscriptionWithSourcesWithInValidSourceTest() throws Exception {
		
		final SystemRequestDTO invalidSource = getSystemRequestDTO();
		invalidSource.setSystemName( null );
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of( invalidSource ) );
		
		final SubscriptionResponseDTO result = createSubscriptionResponseForDBMock(  );
		
		when( eventHandlerService.updateSubscriptionResponse( anyLong(), any(SubscriptionRequestDTO.class) ) ).thenReturn( result );
		
		final MvcResult response = this.mockMvc.perform( put(EVENT_HANDLER_MGMT_URI + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( response );
		Assert.assertTrue(response.getResolvedException().getMessage().contains("System name is null or blank."));
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionListResponseDTO createSubscriptionListResponseForDBMock( final int resultSize ) {
		
		final List<SubscriptionResponseDTO> data = new ArrayList<>( resultSize );
		for (int i = 0; i < resultSize; i++) {
			
			data.add(createSubscriptionResponseForDBMock("eventType"+i, "subscriber"+i));
			
		}
		
		return new SubscriptionListResponseDTO(data, resultSize);
		
	}

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionResponseDTO createSubscriptionResponseForDBMock( ) {
		
		//return createSubscriptionResponseForDBMock( "testEventType", "testSubscriberSystemName");
		final SubscriptionResponseDTO response = createSubscriptionResponseForDBMock( "testEventType", "testSubscriberSystemName");
		response.setSources(Set.of());
		
		return response;
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private SubscriptionResponseDTO createSubscriptionResponseForDBMock( final String eventType, final String subscriberName ) {
		
		return DTOConverter.convertSubscriptionToSubscriptionResponseDTO( createSubscriptionForDBMock( eventType, subscriberName ));
	}

	//-------------------------------------------------------------------------------------------------	
	private Subscription createSubscriptionForDBMock(final String eventType, final String subscriberName) {
		
		final Subscription subscription = new Subscription(
				createEventTypeForDBMock( eventType ), 
				createSystemForDBMock( subscriberName ), 
				null, 
				"notifyUri", 
				false, 
				false,
				null, 
				null);
		
		subscription.setId( 1L );
		subscription.setCreatedAt( ZonedDateTime.now() );
		subscription.setUpdatedAt( ZonedDateTime.now() );
		
		return subscription;
	}

	//-------------------------------------------------------------------------------------------------	
	private EventTypeResponseDTO createEventTypeResponseDTO(final String eventType) {
		
		final EventType eventTypeFromDB = new EventType( eventType );
		eventTypeFromDB.setId( 1L );
		eventTypeFromDB.setCreatedAt( ZonedDateTime.now() );
		eventTypeFromDB.setUpdatedAt( ZonedDateTime.now() );
		
		return DTOConverter.convertEventTypeToEventTypeResponseDTO( eventTypeFromDB );
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private EventType createEventTypeForDBMock(final String eventType) {
		
		final EventType eventTypeFromDB = new EventType( eventType );
		eventTypeFromDB.setId( 1L );
		eventTypeFromDB.setCreatedAt( ZonedDateTime.now() );
		eventTypeFromDB.setUpdatedAt( ZonedDateTime.now() );
		
		return  eventTypeFromDB ;		
	}

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOForTest() {
		
		return new SubscriptionRequestDTO(
				"eventType", 
				getSystemRequestDTO(), 
				null, //filterMetaData
				"notifyUri", 
				false, //matchMetaData
				null, //startDate
				null, //endDate, 
				null); //sources)
	}
	
	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestWithNullMetaDataDTOForTest() {
		
		return new SubscriptionRequestDTO(
				"eventType", 
				getSystemRequestDTO(), 
				null, //filterMetaData
				"notifyUri", 
				null, //matchMetaData
				null, //startDate
				null, //endDate, 
				null); //sources)
	}

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOWithNullEventTypeForTest() {
		
		return new SubscriptionRequestDTO(
				null, //EventType
				getSystemRequestDTO(), 
				null, //filterMetaData
				"notifyUri", 
				false, //matchMetaData
				null, //startDate
				null, //endDate, 
				null); //sources)
	}
	
	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOWithEmptyEventTypeForTest() {
		
		return new SubscriptionRequestDTO(
				"   ", //EventType
				getSystemRequestDTO(), 
				null, //filterMetaData
				"notifyUri", 
				false, //matchMetaData
				null, //startDate
				null, //endDate, 
				null); //sources)
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
	private System createSystemForDBMock( final String systemName) {
		
		final System system = new System();
		system.setId( 1L );
		system.setSystemName( systemName );
		system.setAddress( "localhost" );
		system.setPort( 12345 );	
		system.setCreatedAt( ZonedDateTime.now() );
		system.setUpdatedAt( ZonedDateTime.now() );
		
		return system;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		
		return new EventPublishRequestDTO(
				"eventType", 
				getSystemRequestDTO(), //source, 
				null, //metaData, 
				"payload", 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private String createQueryStringForUnregister(final String eventType, final String subscriberName, final String subscriberAddress, final Integer subscriberPort) {
		final StringBuilder sb = new StringBuilder();
		
		if ( eventType != null) {
			sb.append(CommonConstants.OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE).append("=").append( eventType ).append("&");
		}                               
		                                
		if ( subscriberName != null) {      
			sb.append(CommonConstants.OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME).append("=").append( subscriberName ).append("&");
		}                                
		                                 
		if ( subscriberAddress != null) {   
			sb.append(CommonConstants.OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS).append("=").append( subscriberAddress ).append("&");
		}                                
		                                 
		if ( subscriberPort != null) {      
			sb.append(CommonConstants.OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT).append("=").append( subscriberPort.intValue()).append("&");
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult deleteUnregisterSubscription(final String queryStr, final ResultMatcher matcher) throws Exception {
		final String validatedQueryStr = Utilities.isEmpty(queryStr) ? "" : "?" + queryStr.trim();
		return this.mockMvc.perform(delete(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_UNSUBSCRIBE + validatedQueryStr)
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}

}