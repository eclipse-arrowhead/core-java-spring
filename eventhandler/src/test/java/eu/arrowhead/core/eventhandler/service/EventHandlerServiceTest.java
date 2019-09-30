package eu.arrowhead.core.eventhandler.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.metadatafiltering.MetadataFilteringAlgorithm;

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
	
	@Mock
	private MetadataFilteringAlgorithm metadataFilter;
	

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
		
		verify( eventHandlerDriver, times(1) ).getAuthorizedPublishers( any() );
		verify( eventHandlerDBService, times(1) ).registerSubscription(  any(), any() );
	
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
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSubscriberSystem( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains("System is null."));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemSystemNameNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains("System name is null or blank."));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemSystemNameEmpty() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( "  " );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains("System name is null or blank."));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemAddressNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains("System address is null or blank."));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemAddressEmpty() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( "   " );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "System address is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemPortNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( null );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains("System port is null."));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidReuestPrameterSubscriberSystemPortLessThenOne() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( -1 );
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any() )).thenReturn( authorizedPublishers );
		doNothing().when( eventHandlerDBService ).registerSubscription( any(), any());
	
		try {
			
			eventHandlerService.subscribe( request );
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "System port is less than one." ));
			throw ex;
		}
	
	}
	
	//=================================================================================================
	//Tests of unsubscribe
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnsubscribeOK() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		eventHandlerService.unsubscribe( 
				request.getEventType(), 
				request.getSubscriberSystem().getSystemName(),
				request.getSubscriberSystem().getAddress(),
				request.getSubscriberSystem().getPort());
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterEventTypeNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType( null );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterEventTypeEmpty() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType( "   " );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterSubscriberNameNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( null );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "SubscriberName is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterSubscriberNameEmpty() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName( "    " );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "SubscriberName is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterSubscriberAddressNull() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( null );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "SubscriberAddress is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterSubscriberAddressEmpty() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress( "    " );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "SubscriberAddress is null or blank." ));
			throw ex;
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidReuestPrameterSubscriberPort() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort( -1 );

		doNothing().when( eventHandlerDBService ).deleteSubscription( any(), any());
		
		try {
			
			eventHandlerService.unsubscribe( 
					request.getEventType(), 
					request.getSubscriberSystem().getSystemName(),
					request.getSubscriberSystem().getAddress(),
					request.getSubscriberSystem().getPort());
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "System port is less than one." ));
			throw ex;
		}
	
	}
	
	//=================================================================================================
	//Tests of publishResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseOK() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		eventHandlerService.publishResponse( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterRequestNull() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		
		final EventPublishRequestDTO request = null;
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventPublishRequestDTO is null." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterEventTypeNull() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType( null );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterEventTypeEmpty() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType( "   " );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterPayloadNull() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload( null );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "Payload is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterPayloadEmpty() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload( "    " );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "Payload is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterTimeStampNull() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( null );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "TimeStamp is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterTimeStampEmpty() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( "   " );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "TimeStamp is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterTimeStampWrongFormat() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( "2007.12.03T10:15:30+01:00" );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "TimeStamp is not valid." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterTimeStampInFuture() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( "3019-09-27 09:40:34" );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "TimeStamp is further in the future than the tolerated time difference" ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidReuestPrameterTimeStampInPast() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp( "1019-09-27 09:40:34" );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		try {
			
			eventHandlerService.publishResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "TimeStamp is further in the past than the tolerated time difference" ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testPublishResponseFilterStartDate() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithStartDateInPast( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		final ArgumentCaptor<Set> valueCapture = ArgumentCaptor.forClass( Set.class);
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), valueCapture.capture());	
		
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		eventHandlerService.publishResponse( request );
		
		final Set<Subscription> filterednvolvedSubscriptions = valueCapture.getValue();
		assertEquals( 1, filterednvolvedSubscriptions.size());

	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testPublishResponseFilterEndDate() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithEndDateInPast( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		final ArgumentCaptor<Set> valueCapture = ArgumentCaptor.forClass( Set.class);
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		doNothing().when( eventHandlerDriver ).publishEvent( any(), valueCapture.capture());	
		
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		eventHandlerService.publishResponse( request );
		
		final Set<Subscription> filterednvolvedSubscriptions = valueCapture.getValue();
		assertEquals( 1, filterednvolvedSubscriptions.size());

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseMatchMetaDataEventMetaDataNull() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithMatchMetaData( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setMetaData( null );

		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		verify(eventHandlerDriver, never()).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		eventHandlerService.publishResponse( request );

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseMatchMetaDataEventMetaDataEmpty() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithMatchMetaData( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setMetaData( Map.of() );
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		verify(eventHandlerDriver, never()).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( true );
		
		eventHandlerService.publishResponse( request );
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseMetaDataNoMatch() {
		
		ReflectionTestUtils.setField( eventHandlerService, "timeStampTolerance", 120);
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithMatchMetaData( 7 );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setMetaData( Map.of( "a", "1"));
		
		when( eventHandlerDBService.getInvolvedSubscriptions( any())).thenReturn( involvedSubscriptions );
		verify(eventHandlerDriver, never()).publishEvent( any(), any());
		when( metadataFilter.doFiltering( any() ) ).thenReturn( false );
		
		eventHandlerService.publishResponse( request );
		
	}
	
	//=================================================================================================
	//Tests of publishSubscriberAuthorizationUpdateResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishSubscriberAuthorizationUpdateResponseOK() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidReuestPrameterEventTypeNull() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setEventType( null );
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		try {
			
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidReuestPrameterEventTypeEmpty() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setEventType( "    " );
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		try {
			
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is null or blank." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidReuestPrameterEventTypeNotValid() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setEventType( CoreCommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE + "-" );
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		try {
			
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "EventType is not valid." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidReuestPrameterPayloadNull() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setPayload( null );		
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		try {
			
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "Payload is not valid." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidReuestPrameterPayloadEmpty() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setPayload( "   " );		
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		try {
			
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "Payload is not valid." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidReuestPrameterPayloadInvalid() {
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		final List<Subscription> involvedSubscriptions = List.copyOf( getSubscriptionSet( 7 ) );
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setPayload( "1a" );		
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		when( eventHandlerDriver.getAuthorizedPublishers( any() ) ).thenReturn( authorizedPublishers );	
		doNothing().when( eventHandlerDBService ).updateSubscriberAuthorization( any(), any() );

		try {
			
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "Payload is not valid." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishSubscriberAuthorizationUpdateEmptyInvolvedSubscriptions() {
		
		final List<Subscription> involvedSubscriptions = List.of();
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();	
		
		when( eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( any())).thenReturn( involvedSubscriptions );
		verify(eventHandlerDriver, never()).getAuthorizedPublishers( any());
		verify(eventHandlerDBService, never()).updateSubscriberAuthorization( any(), any());
		
		eventHandlerService.publishSubscriberAuthorizationUpdateResponse( request );

	}
	
	//=================================================================================================
	//Tests of updateSubscriptionResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionResponseOK() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any())).thenReturn( authorizedPublishers );
		when( eventHandlerDBService.updateSubscription( anyLong(), any(), any())).thenReturn( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ) );
		
		final SubscriptionResponseDTO response = eventHandlerService.updateSubscriptionResponse( 1L, request);
		
		assertTrue( response != null);
	}
	
	//=================================================================================================
	//Tests of updateSubscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionOK() {
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any())).thenReturn( authorizedPublishers );
		when( eventHandlerDBService.updateSubscription( anyLong(), any(), any())).thenReturn( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ) );
		
		final Subscription response = eventHandlerService.updateSubscription( 1L, request );
		
		assertTrue( response != null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateSubscriptionInvalidReuestPrameterRequestNull() {
		
		final SubscriptionRequestDTO request = null;
		
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet( 7 );
		
		when( eventHandlerDriver.getAuthorizedPublishers( any())).thenReturn( authorizedPublishers );
		when( eventHandlerDBService.updateSubscription( anyLong(), any(), any())).thenReturn( createSubscriptionForDBMock( 1, "eventType", "subscriberName" ) );
		
		try {
			
			eventHandlerService.updateSubscription( 1L, request );
			
		} catch (Exception ex) {
			
			Assert.assertTrue( ex.getMessage().contains( "SubscriptionRequestDTO is null." ));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	// skipped other testUpdateSubscriptionInvalidReuestPrameter - method using the same checkSubscriptionRequestDTO as tested at testSubcribe
	
	
	
	//=================================================================================================
	//Assistant methods
	
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
		
		final Set<SystemResponseDTO> systemResponseDTOSet = new HashSet<>();
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
