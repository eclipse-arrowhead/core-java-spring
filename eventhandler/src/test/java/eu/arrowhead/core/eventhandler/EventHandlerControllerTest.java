package eu.arrowhead.core.eventhandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.EventTypeResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
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

		this.mockMvc.perform(get(CommonConstants.EVENT_HANDLER_URI + CommonConstants.ECHO_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());		
	}
	
	//=================================================================================================
	// Test of subscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionTest() throws Exception {
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullEventTypeTest() throws Exception {
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithEmptyEventTypeTest() throws Exception {
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullSubscriberSystemTest() throws Exception {
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSubscriberSystem( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullSubscriberSystemNameTest() throws Exception {
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithEmptySubscriberSystemNameTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( "   " );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullSubscriberSystemAddressTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithEmptySubscriberSystemAddressTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( "   " );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullSubscriberSystemPortTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithInvalidSubscriberSystemPortTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( -1 );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullNotifyUriTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithEmptyNotifyUriTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( "   " );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullMatchMetaDataTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestWithNullMetaDataDTOForTest();
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithMetaDataTrueButNullFilterMetaDataTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithMetaDataTrueButEmptyFilterMetaDataTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( new HashMap<String, String>( 0 ) );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithNullSourcesTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( null );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithEmptySourcesTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of() );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithSourcesWithValidSourceTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of( getSystemRequestDTO() ) );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void subscriptionWithSourcesWithInValidSourceTest() throws Exception {
		
		final SubscriptionResponseDTO dto = getSubscriptionResponseDTOForTest();
		
		final SystemRequestDTO invalidSource = getSystemRequestDTO();
		invalidSource.setSystemName( null );
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources( Set.of( invalidSource ) );
		
		when(eventHandlerService.subscribe(any())).thenReturn(dto);
		
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
	public void unSubscriptionTest() throws Exception {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		
		doNothing().when(eventHandlerService).unsubscribe(any());
		
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
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.Payload is null or blank."));	
	}
	
	//=================================================================================================
	//Assistant methods

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
	private SubscriptionResponseDTO getSubscriptionResponseDTOForTest() {
		
		return new SubscriptionResponseDTO(
				1L, 
				getEventType(), 
				getSystemResponseDTO(),//subscriberSystem, 
				null, //filterMetaData, 
				"notifyUri", 
				false, //matchMetaData, 
				null, //startDate, 
				null, //endDate, 
				null, //sources, 
				"createdAt", 
				"updatedAt" );
	}
	
	//-------------------------------------------------------------------------------------------------	
	private EventTypeResponseDTO getEventType() {
		
		return new EventTypeResponseDTO(
				1L, 
				"eventTypeName", 
				"createdAt", 
				"updatedAt");
				
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
	private SystemResponseDTO getSystemResponseDTO() {
		
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("systemName");
		systemResponseDTO.setAddress("localhost");
		systemResponseDTO.setPort(12345);	
		
		return systemResponseDTO;
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

}