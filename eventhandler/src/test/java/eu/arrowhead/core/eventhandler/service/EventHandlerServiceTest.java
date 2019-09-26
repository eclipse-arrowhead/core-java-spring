package eu.arrowhead.core.eventhandler.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

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
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;

@RunWith(SpringRunner.class)
public class EventHandlerServiceTest {
	//=================================================================================================
	// members
	
	@InjectMocks
	private EventHandlerService eventHandlerService; 	
	
	@Mock
	private EventHandlerDBService eventHandlerDBService;
	
	@Mock
	private EventHandlerDriver eventHandlerDriver;
	

	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of subscribe
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSubscribeOK() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestNull() {
		
		final SubscriptionRequestDTO request = null;
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterNullEventType() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterEmptyEventType() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType( "   " );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterNullNotifyUri() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterEmptyNotifyUri() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri( "   " );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterMatchMetaDataTrueAndFilterMetaDataNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterMatchMetaDataTrueAndFilterMetaDataEmpty() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData( true );
		request.setFilterMetaData( Map.of() );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterStartDateInPast() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final ZonedDateTime past = ZonedDateTime.now().minusMinutes( 10 );
		request.setStartDate( Utilities.convertZonedDateTimeToUTCString( past ));
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterEndDateInPast() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final ZonedDateTime past = ZonedDateTime.now().minusMinutes( 10 );
		request.setEndDate( Utilities.convertZonedDateTimeToUTCString( past ));
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		eventHandlerService.subscribe( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterEndDateIsBeforeStarDate() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final ZonedDateTime end = ZonedDateTime.now().plusMinutes( 5 );
		final ZonedDateTime start = ZonedDateTime.now().plusMinutes( 10 );
		request.setEndDate( Utilities.convertZonedDateTimeToUTCString( end ));
		request.setStartDate( Utilities.convertZonedDateTimeToUTCString( start ));
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains("Start Date sould be before End Date"));
			throw ex;
		}
	
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionListResponseDTO createSubscriptionListResponseForDBMock( final int resultSize ) {
		
		final List<SubscriptionResponseDTO> data = new ArrayList<>( resultSize );
		for (int i = 0; i < resultSize; i++) {
			
			data.add( createSubscriptionResponseForDBMock( "eventType" + i, "subscriber" + i ) );
			
		}
		
		return new SubscriptionListResponseDTO(data, resultSize);
		
	}

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionResponseDTO createSubscriptionResponseForDBMock( ) {
		
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
	private EventTypeResponseDTO createEventTypeResponseDTO( final String eventType ) {
		
		final EventType eventTypeFromDB = new EventType( eventType );
		eventTypeFromDB.setId( 1L );
		eventTypeFromDB.setCreatedAt( ZonedDateTime.now() );
		eventTypeFromDB.setUpdatedAt( ZonedDateTime.now() );
		
		return DTOConverter.convertEventTypeToEventTypeResponseDTO( eventTypeFromDB );
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private EventType createEventTypeForDBMock( final String eventType ) {
		
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
	private SystemResponseDTO getSystemResponseDTO( final String systemName ) {
		
		return DTOConverter.convertSystemToSystemResponseDTO(createSystemForDBMock( systemName ));
	}
	
	//-------------------------------------------------------------------------------------------------	
	private Set<SystemResponseDTO> getSystemResponseDTOSet( final int size ) {
		
		final Set<SystemResponseDTO> systemResponseDTOSet = new HashSet();
		for (int i = 0; i < size; i++) {
			
			systemResponseDTOSet.add( getSystemResponseDTO( "systemName" + i));
			
		}
		
		return systemResponseDTOSet;
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
