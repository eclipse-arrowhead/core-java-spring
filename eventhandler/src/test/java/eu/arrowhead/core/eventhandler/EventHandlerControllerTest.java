package eu.arrowhead.core.eventhandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
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
import eu.arrowhead.common.dto.EventFilterRequestDTO;
import eu.arrowhead.common.dto.EventFilterResponseDTO;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.dto.EventPublishResponseDTO;
import eu.arrowhead.common.dto.EventTypeResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
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

		this.mockMvc.perform(get(CommonConstants.ECHO_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());		
	}
	
	//=================================================================================================
	// Test of subscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionTest() throws Exception {
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getEventFilterRequestDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		Assert.assertNotNull( result );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullEventTypeTest() throws Exception {
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getEventFilterRequestDTOWithNullEventTypeForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithEmptyEventTypeTest() throws Exception {
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getEventFilterRequestDTOWithEmptyEventTypeForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.EventType"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithNullSubscriberSystemTest() throws Exception {
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setSubscriberSystem( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( "   " );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.getSubscriberSystem().setAddress( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.getSubscriberSystem().setAddress( "   " );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.getSubscriberSystem().setPort( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.getSubscriberSystem().setPort( -1 );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setNotifyUri( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setNotifyUri( "   " );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setMatchMetaData( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.MatchMetaData is null."));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void subscriptionWithMetaDataTrueButNullFilterMetaDataTest() throws Exception {
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( new HashMap<String, String>( 0 ) );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setSources( null );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setSources( Set.of() );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setSources( Set.of( getSystemRequestDTO() ) );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterResponseDTO dto = getEventFilterResponseDTOForTest();
		
		final SystemRequestDTO invalidSource = getSystemRequestDTO();
		invalidSource.setSystemName( null );
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		request.setSources( Set.of( invalidSource ) );
		
		when(eventHandlerService.subscriptionRequest(any())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventFilterRequestDTO request = getEventFilterRequestDTOForTest();
		
		doNothing().when(eventHandlerService).unSubscriptionRequest(any());
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION)
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
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
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
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType( null );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
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
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType( "   " );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
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
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( null );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.TimeStamp is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyTimeStampTest() throws Exception {
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( "   " );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.TimeStamp is null or blank."));	
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public  void publishInvalidSourceTest() throws Exception {
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setSystemName( null );
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource( systemRequestDTO );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
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
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload( null );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
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
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload( "   " );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
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
	public void publishNullDeliveryCompleteUriTest() throws Exception {
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setDeliveryCompleteUri( null );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.DeliveryCompleteUri is null or blank."));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void publishEmptyDeliveryCompleteUriTest() throws Exception {
		
		final EventPublishResponseDTO dto = getEventPublishResponseDTO();
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setDeliveryCompleteUri( "   " );
		
		when(eventHandlerService.publishRequest(any())).thenReturn( dto );
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.OP_EVENTHANDLER_PUBLISH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes( request ))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		Assert.assertNotNull( result );
		Assert.assertTrue(result.getResolvedException().getMessage().contains("Request.DeliveryCompleteUri is null or blank."));	
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------	
	private EventFilterRequestDTO getEventFilterRequestDTOForTest() {
		
		return new EventFilterRequestDTO(
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
	private EventFilterRequestDTO getEventFilterRequestDTOWithNullEventTypeForTest() {
		
		return new EventFilterRequestDTO(
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
	private EventFilterRequestDTO getEventFilterRequestDTOWithEmptyEventTypeForTest() {
		
		return new EventFilterRequestDTO(
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
	private EventFilterResponseDTO getEventFilterResponseDTOForTest() {
		
		return new EventFilterResponseDTO(
				1L, 
				getEventType(), 
				getSystemRequestDTO(),//subscriberSystem, 
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
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		
		return new EventPublishRequestDTO(
				"eventType", 
				getSystemRequestDTO(), //source, 
				null, //metaData, 
				"deliveryCompleteUri", 
				"payload", 
				"timeStamp");
	}

	//-------------------------------------------------------------------------------------------------	
	private EventPublishResponseDTO getEventPublishResponseDTO() {
		
		return new EventPublishResponseDTO( Map.of("subscriberUri", true));
	}

}
