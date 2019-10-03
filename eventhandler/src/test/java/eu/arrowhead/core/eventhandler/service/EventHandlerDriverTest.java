package eu.arrowhead.core.eventhandler.service;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.EventTypeResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.metadatafiltering.MetadataFilteringAlgorithm;
import eu.arrowhead.core.eventhandler.publish.PublishingQueue;

@RunWith(SpringRunner.class)
public class EventHandlerDriverTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private EventHandlerDriver eventHandlerDriver;

	@Mock
	private HttpService httpService;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private PublishingQueue publishingQueue;
	
	@Mock
	private Logger logger;
	
	private static final String AUTH_SUBSCRIPTION_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of getAuthorizedPublishers
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizedPublishersOk() {
		//final Object relayClient = ReflectionTestUtils.getField(testingObject, "relayClient");
		
		final SystemRequestDTO request = getSystemRequestDTO();
		
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString( "authSubscriptionCheckUri" );
 		final UriComponents checkUri = uriBuilder.build();
 		
		final SystemResponseDTO subscriber = getSystemResponseDTO( "SubscriberSystemName" );
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet( 7 );
		
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO( subscriber, publishers);
		
		when( httpService.sendRequest(
				eq( checkUri ), 
				eq( HttpMethod.POST ), 
				eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
				any( AuthorizationSubscriptionCheckRequestDTO.class )
				)).thenReturn( new ResponseEntity< AuthorizationSubscriptionCheckResponseDTO >(
						httpResponse,
						HttpStatus.OK));

		when( arrowheadContext.containsKey( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( true );
		when( arrowheadContext.get( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( checkUri );
	
		final Set<SystemResponseDTO> response = eventHandlerDriver.getAuthorizedPublishers( request );
		
		verify( httpService, times(1) ).sendRequest(
				eq( checkUri ), 
				eq( HttpMethod.POST ), 
				eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
				any( AuthorizationSubscriptionCheckRequestDTO.class ) );
		verify( arrowheadContext, times(1) ).containsKey( anyString() );
		verify( arrowheadContext, times(1) ).get( anyString() );
		
		assertNotNull( response );
		assertTrue( !response.isEmpty() );
		assertTrue( response.size() == 7 );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test( expected = ArrowheadException.class )
	public void testGetAuthorizedPublishersContexNotContainsKey() {

		final SystemRequestDTO request = getSystemRequestDTO();
		
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString( "authSubscriptionCheckUri" );
 		final UriComponents checkUri = uriBuilder.build();
 		
		final SystemResponseDTO subscriber = getSystemResponseDTO( "SubscriberSystemName" );
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet( 7 );
		
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO( subscriber, publishers);
		
		when( httpService.sendRequest(
				eq( checkUri ), 
				eq( HttpMethod.POST ), 
				eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
				any( AuthorizationSubscriptionCheckRequestDTO.class )
				)).thenReturn( new ResponseEntity< AuthorizationSubscriptionCheckResponseDTO >(
						httpResponse,
						HttpStatus.OK));

		when( arrowheadContext.containsKey( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( false );
		when( arrowheadContext.get( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( checkUri );
	
		try {
			
			eventHandlerDriver.getAuthorizedPublishers( request );
			
		} catch (Exception ex) {
			verify( httpService, times( 0 ) ).sendRequest(
					eq( checkUri ), 
					eq( HttpMethod.POST ), 
					eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
					any( AuthorizationSubscriptionCheckRequestDTO.class ) );
			verify( arrowheadContext, times( 1 ) ).containsKey( anyString() );
			verify( arrowheadContext, times( 0 ) ).get( anyString() );
			
			assertTrue( ex.getMessage().contains( "EventHandler can't find subscription authorization check URI." ));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test( expected = ArrowheadException.class )
	public void testGetAuthorizedPublishersCanNotCastContextValue() {

		final SystemRequestDTO request = getSystemRequestDTO();
		
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString( "authSubscriptionCheckUri" );
 		final UriComponents checkUri = uriBuilder.build();
 		
		final SystemResponseDTO subscriber = getSystemResponseDTO( "SubscriberSystemName" );
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet( 7 );
		
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO( subscriber, publishers);
		
		when( httpService.sendRequest(
				eq( checkUri ), 
				eq( HttpMethod.POST ), 
				eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
				any( AuthorizationSubscriptionCheckRequestDTO.class )
				)).thenReturn( new ResponseEntity< AuthorizationSubscriptionCheckResponseDTO >(
						httpResponse,
						HttpStatus.OK));

		when( arrowheadContext.containsKey( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( true );
		when( arrowheadContext.get( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( Exception.class );
	
		try {
			
			eventHandlerDriver.getAuthorizedPublishers( request );
			
		} catch ( ClassCastException ex ) {
			verify( httpService, times( 0 ) ).sendRequest(
					eq( checkUri ), 
					eq( HttpMethod.POST ), 
					eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
					any( AuthorizationSubscriptionCheckRequestDTO.class ) );
			verify( arrowheadContext, times( 1 ) ).containsKey( anyString() );
			verify( arrowheadContext, times( 1 ) ).get( anyString() );
			
			assertTrue( ex.getMessage().contains( "EventHandler can't find subscription authorization check URI." ));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test( expected = IllegalArgumentException.class )
	public void testGetAuthorizedPublishersRequestNull() {

		final SystemRequestDTO request = null;
		
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString( "authSubscriptionCheckUri" );
 		final UriComponents checkUri = uriBuilder.build();
 		
		final SystemResponseDTO subscriber = getSystemResponseDTO( "SubscriberSystemName" );
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet( 7 );
		
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO( subscriber, publishers);
		
		when( httpService.sendRequest(
				eq( checkUri ), 
				eq( HttpMethod.POST ), 
				eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
				any( AuthorizationSubscriptionCheckRequestDTO.class )
				)).thenReturn( new ResponseEntity< AuthorizationSubscriptionCheckResponseDTO >(
						httpResponse,
						HttpStatus.OK));

		when( arrowheadContext.containsKey( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( true );
		when( arrowheadContext.get( AUTH_SUBSCRIPTION_CHECK_URI_KEY ) ).thenReturn( checkUri );
	
		try {
			
			eventHandlerDriver.getAuthorizedPublishers( request );
			
		} catch (Exception ex) {
			verify( httpService, times(0) ).sendRequest(
					eq( checkUri ), 
					eq( HttpMethod.POST ), 
					eq( AuthorizationSubscriptionCheckResponseDTO.class ), 
					any( AuthorizationSubscriptionCheckRequestDTO.class ) );
			verify( arrowheadContext, times(0) ).containsKey( anyString() );
			verify( arrowheadContext, times(0) ).get( anyString() );
			
			assertTrue( ex.getMessage().contains( "subscriberSystem is null." ));
			
			throw ex;
		}
	}
	
	//=================================================================================================
	//Tests of publishEvent
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishEventOk() {
		
		ReflectionTestUtils.setField( eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField( eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = Set.of( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ));

		try {
			
			doNothing().when( publishingQueue ).put( any() );
		
		} catch (InterruptedException e1) {
			
			assertTrue( false );
		}
		
		eventHandlerDriver.publishEvent( request, involvedSubscriptions );
		
		try {
			
			verify( publishingQueue, times( 1 ) ).put( any() );
		
		} catch (InterruptedException e) {
			
			assertTrue( false );
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishEventInterruptedExceptionOk() {
		
		ReflectionTestUtils.setField( eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField( eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = Set.of( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ));

		final ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass( String.class);
		
		try {
			
			doThrow( InterruptedException.class ).when( publishingQueue ).put( any() );
		
		} catch (InterruptedException ex) {
			
			assertNotNull( ex );
		}
		
		doNothing().when( logger ).debug( valueCapture.capture() );
		
		eventHandlerDriver.publishEvent( request, involvedSubscriptions );
		
		try {
			
			verify( publishingQueue, times( 1 ) ).put( any() );
		
		} catch (InterruptedException ex) {
			
			assertTrue( false );
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	@Ignore
	public void testPublishEventReturnsUnder10Milliseconds() {
		
		ReflectionTestUtils.setField( eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField( eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptionsLargeSet = createLargeSetOfSubscriptions( 100000 );
		final Set<Subscription> involvedSubscriptionsSmallSet = createLargeSetOfSubscriptions( 10 );
		
		final List<Integer> notValidTimeDifferents = new ArrayList<>();
		final List<Integer> validTimeDifferents = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			
			final long beforeLarge = java.lang.System.currentTimeMillis();
			
			eventHandlerDriver.publishEvent( request, involvedSubscriptionsLargeSet );
			
			final long afterLarge = java.lang.System.currentTimeMillis();
			
			final long diffInLargeSet = afterLarge - beforeLarge;
			
			final long beforeSmall = java.lang.System.currentTimeMillis();
			
			eventHandlerDriver.publishEvent( request, involvedSubscriptionsSmallSet );
			
			final long afterSmall = java.lang.System.currentTimeMillis();
			
			final long diffInSmallSet = afterSmall - beforeSmall;
			
			final int x = (int) (diffInLargeSet - diffInSmallSet);
			
			if ( x > 10 ) {
				
				notValidTimeDifferents.add( x );
			
			} else {
				 
				validTimeDifferents.add( x );
			}
			
		}
		
		assertTrue( notValidTimeDifferents.isEmpty() ||   notValidTimeDifferents.size() < 100 );
		assertTrue( !validTimeDifferents.isEmpty() );
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------	
	private Set<Subscription> createLargeSetOfSubscriptions( final int size ) {
		
		final Set<Subscription> involvedSubscriptions = new HashSet<Subscription>();
		
		for (int i = 0; i < size; i++) {
			
			involvedSubscriptions.add( createSubscriptionForDBMock( i+1, "eventType"+i, "subscriberName")); 
			
		}
		
		return involvedSubscriptions;
	}

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
		
		return DTOConverter.convertSubscriptionToSubscriptionResponseDTO( createSubscriptionForDBMock(1, eventType, subscriberName ));
	}

	//-------------------------------------------------------------------------------------------------	
	private Subscription createSubscriptionForDBMock(final int i, final String eventType, final String subscriberName) {
		
		final Subscription subscription = new Subscription(
				createEventTypeForDBMock( eventType ), 
				createSystemForDBMock( subscriberName ), 
				null, 
				"notifyUri", 
				false, 
				false,
				null, 
				null);
		
		subscription.setId( i );
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
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
	
	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest() {
		
		return new EventPublishRequestDTO(
				CoreCommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE, 
				getSystemRequestDTO(), //source, 
				null, //metaData, 
				"1", 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSet(final int size) {
		
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; i++) {
			
			subscriptionSet.add( createSubscriptionForDBMock( i + 1,  "eventType" + i , "subscriberName" + i));
			
		}
		
		return subscriptionSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSetWithStartDateInPast(final int size) {
		
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; i++) {
			
			final Subscription subscription = createSubscriptionForDBMock( i + 1,  "eventType" + i , "subscriberName" + i);
			subscription.setStartDate( ZonedDateTime.now().minusMinutes( 5 ) );
			
			subscriptionSet.add( subscription );
			
		}
		
		final Subscription subscription = createSubscriptionForDBMock( size + 1,  "eventType" + size , "subscriberName" + size);
		subscription.setStartDate( ZonedDateTime.now() );
		subscriptionSet.add( subscription );
		
		return subscriptionSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSetWithEndDateInPast(final int size) {
		
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; i++) {
			
			final Subscription subscription = createSubscriptionForDBMock( i + 1,  "eventType" + i , "subscriberName" + i);
			subscription.setEndDate( ZonedDateTime.now().minusMinutes( 5 ) );
			
			subscriptionSet.add( subscription );
			
		}
		
		final Subscription subscription = createSubscriptionForDBMock( size + 1,  "eventType" + size , "subscriberName" + size );
		subscription.setEndDate( ZonedDateTime.now() );
		
		subscriptionSet.add( subscription );
		
		return subscriptionSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSetWithMatchMetaData(final int size) {
		
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; i++) {
			
			final Subscription subscription = createSubscriptionForDBMock( i + 1,  "eventType" + i , "subscriberName" + i);
			subscription.setMatchMetaData( true );
			subscription.setFilterMetaData( Utilities.map2Text( Map.of("1", "a")) );
			
			subscriptionSet.add( subscription );
			
		}
		
		return subscriptionSet;
	}
	
}
