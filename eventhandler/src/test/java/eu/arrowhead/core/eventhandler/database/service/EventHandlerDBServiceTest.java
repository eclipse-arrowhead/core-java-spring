package eu.arrowhead.core.eventhandler.database.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.EventTypeRepository;
import eu.arrowhead.common.database.repository.SubscriptionPublisherConnectionRepository;
import eu.arrowhead.common.database.repository.SubscriptionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.EventTypeResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.eventhandler.service.EventHandlerDriver;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;

@RunWith(SpringRunner.class)
public class EventHandlerDBServiceTest {
	//=================================================================================================
	// members
	
	@InjectMocks
	private EventHandlerDBService eventHandlerDBService;
	
	@Mock
	private EventHandlerService eventHandlerService;
	
	@Mock
	private EventHandlerDriver eventHandlerDriver;
	
	@Mock
	private SubscriptionRepository subscriptionRepository;
	
	@Mock
	private SubscriptionPublisherConnectionRepository subscriptionPublisherConnectionRepository;
	
	@Mock
	private EventTypeRepository eventTypeRepository;
	
	@Mock
	private SystemRepository systemRepository;

	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of getSubscriptionByIdResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionByIdResponseOK() {
		
		when( subscriptionRepository.findById( anyLong() ) ).thenReturn(Optional.of( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ) ) );
		
		final SubscriptionResponseDTO response = eventHandlerDBService.getSubscriptionByIdResponse( 1L );
		
		verify( subscriptionRepository, times( 1 )).findById( anyLong() );
		assertNotNull( response );
		assertNotNull( response.getSubscriberSystem() );
		assertNotNull( response.getEventType() );
		assertNotNull( response.getNotifyUri() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdResponseInvalidParameterId() {
		
		try {
			
			eventHandlerDBService.getSubscriptionByIdResponse( -1L );
		
		} catch (Exception ex) {
						
			Assert.assertTrue( ex.getMessage().contains( "SubscriberSystemId must be greater than zero." ));
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdResponseInvalidParameterSubscriptionByIdNotInDB() {

		final Subscription subscription =  null;
		when( subscriptionRepository.findById( anyLong() ) ).thenReturn(Optional.ofNullable( subscription ) );
		
		try {
			
			eventHandlerDBService.getSubscriptionByIdResponse( 1L );
		
		} catch (Exception ex) {
						
			Assert.assertTrue( ex.getMessage().contains( "not exists" ));
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = Exception.class)
	public void testGetSubscriptionByIdResponseExceptionInDBCall() {

		when( subscriptionRepository.findById( anyLong() ) ).thenThrow( Exception.class );
		
		try {
			
			eventHandlerDBService.getSubscriptionByIdResponse( 1L );
		
		} catch (Exception ex) {
						
			Assert.assertTrue( ex.getMessage().contains( "Database operation exception" ));
			throw ex;
		}
		
	}
	
	//=================================================================================================
	//Tests of getSubscriptionById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionByIdOK() {
		
		when( subscriptionRepository.findById( anyLong() ) ).thenReturn(Optional.of( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ) ) );
		
		final Subscription response = eventHandlerDBService.getSubscriptionById( 1L );
		
		verify( subscriptionRepository, times( 1 )).findById( anyLong() );
		assertNotNull( response );
		assertNotNull( response.getSubscriberSystem() );
		assertNotNull( response.getEventType() );
		assertNotNull( response.getNotifyUri() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdInvalidParameterId() {
		
		try {
			
			eventHandlerDBService.getSubscriptionById( -1L );
		
		} catch (Exception ex) {
						
			Assert.assertTrue( ex.getMessage().contains( "SubscriberSystemId must be greater than zero." ));
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdInvalidParameterSubscriptionByIdNotInDB() {

		final Subscription subscription =  null;
		when( subscriptionRepository.findById( anyLong() ) ).thenReturn(Optional.ofNullable( subscription ) );
		
		try {
			
			eventHandlerDBService.getSubscriptionById( 1L );
		
		} catch (Exception ex) {
						
			Assert.assertTrue( ex.getMessage().contains( "not exists" ));
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = Exception.class)
	public void testGetSubscriptionByIdExceptionInDBCall() {

		when( subscriptionRepository.findById( anyLong() ) ).thenThrow( Exception.class );
		
		try {
			
			eventHandlerDBService.getSubscriptionById( 1L );
		
		} catch (Exception ex) {
						
			Assert.assertTrue( ex.getMessage().contains( "Database operation exception" ));
			throw ex;
		}
		
	}

	//=================================================================================================
	//Tests of getSubscriptionsResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponseOK() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getData() );
		assertTrue( !response.getData().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponsePageLessThanZeroOK() {
		
		final int page = -1;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getData() );
		assertTrue( !response.getData().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponseSizeLessThanZeroOK() {
		
		final int page = 0;
		final int size = -1;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getData() );
		assertTrue( !response.getData().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponseDirectionNullOK() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = null;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getData() );
		assertTrue( !response.getData().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionsResponseInvalidParameterSortField() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "invalidSortField";
		
		try {
			
			eventHandlerDBService.getSubscriptionsResponse( page, size, direction, sortField );
		
		} catch (Exception ex) {
			
			verify( subscriptionRepository, never()).findAll( any( PageRequest.class ) );
			
			Assert.assertTrue( ex.getMessage().contains( " sortable field  is not available." ));
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = Exception.class)
	public void testGetSubscriptionsResponseExceptionInDBCall() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenThrow( Exception.class );
		
		try {
			
			eventHandlerDBService.getSubscriptionsResponse( page, size, direction, sortField );
		
		} catch (Exception ex) {
			
			verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
	
			Assert.assertTrue( ex.getMessage().contains( "Database operation exception" ));
			throw ex;
		}
		
	}
	
	//=================================================================================================
	//Tests of getSubscriptions
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsOK() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getContent() );
		assertTrue( !response.getContent().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsPageLessThanZeroOK() {
		
		final int page = -1;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getContent() );
		assertTrue( !response.getContent().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsSizeLessThanZeroOK() {
		
		final int page = 0;
		final int size = -1;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getContent() );
		assertTrue( !response.getContent().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsDirectionNullOK() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = null;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions( page, size, direction, sortField );
		
		verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
		assertNotNull( response );
		assertNotNull( response.getContent() );
		assertTrue( !response.getContent().isEmpty() );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionsInvalidParameterSortField() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = null;
		final String sortField = "invalidSortField";
		
		final Subscription subscription = createSubscriptionForDBMock( 1, "eventType", "subscriberName" );
		final Page<Subscription> subscriptionPage = new PageImpl<>( List.of( subscription ) );
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenReturn( subscriptionPage );
		
		try {
			
			eventHandlerDBService.getSubscriptions( page, size, direction, sortField );
		
		} catch (Exception ex) {
			
			verify( subscriptionRepository, never() ).findAll( any( PageRequest.class ) );
	
			Assert.assertTrue( ex.getMessage().contains( " sortable field  is not available." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = Exception.class)
	public void testGetSubscriptionsExceptionInDBCall() {
		
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		when( subscriptionRepository.findAll( any( PageRequest.class ) ) ).thenThrow( Exception.class );
		
		try {
			
			eventHandlerDBService.getSubscriptions( page, size, direction, sortField );
		
		} catch (Exception ex) {
			
			verify( subscriptionRepository, times( 1 )).findAll( any( PageRequest.class ) );
	
			Assert.assertTrue( ex.getMessage().contains( "Database operation exception" ));
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
