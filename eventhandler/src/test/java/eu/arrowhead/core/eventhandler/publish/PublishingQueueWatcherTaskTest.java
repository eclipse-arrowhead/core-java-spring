package eu.arrowhead.core.eventhandler.publish;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@RunWith(SpringRunner.class)
public class PublishingQueueWatcherTaskTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private final PublishingQueueWatcherTask testingObject = new PublishingQueueWatcherTask();
	
	@Mock
	private PublishingQueue publishingQueue;
	
	@Mock
	private PublishRequestFixedExecutor expressExecutor;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	//@Test
	//public void testRunExpressExecutorOk() {
	//	
	//	final Thread runLoop  = new Thread( new Thread(() -> {
	//		
	//		ReflectionTestUtils.setField( testingObject, "interrupted", false);
	//		ReflectionTestUtils.setField( testingObject, "timeStampTolerance", 120 );
	//		ReflectionTestUtils.setField( testingObject, "maxExpressSubscribers", 10);
	//		
	//		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
	//		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions( 3 );
	//		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
	//		
	//		try {
	//			when( publishingQueue.take() ).thenReturn( publishStartDTO );
	//		
	//		} catch (InterruptedException e) {
	//			
	//			assertTrue( false );
	//		}
	//		
	//		doNothing().when( expressExecutor ).shutdownExecutionNow();
	//		
	//		testingObject.run();
	//		
	//      }) , "test");
	//	
	//	runLoop.start();
	//	
	//	try {
	//		
	//		Thread.sleep( 5000 );
	//	
	//	} catch (InterruptedException e) {
	//		
	//		assertTrue( false );
	//	}
	//	
	//	runLoop.interrupt();
	//	
	//	verify( expressExecutor, times( 0 )).shutdownExecutionNow();
	//	verify( expressExecutor, atLeast( 1 )).execute( any(), any());
	//	
	//	
	//}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunInteruptedOk() {
		
		ReflectionTestUtils.setField( testingObject, "interrupted", false);
		ReflectionTestUtils.setField( testingObject, "timeStampTolerance", 120 );
		ReflectionTestUtils.setField( testingObject, "maxExpressSubscribers", 10);
		
		try {
			when( publishingQueue.take() ).thenThrow( InterruptedException.class );
		
		} catch (InterruptedException e) {
			
			assertTrue( false );
		}
		
		doNothing().when( expressExecutor ).shutdownExecutionNow();
		
		testingObject.run();
		
		verify( expressExecutor, times( 1 )).shutdownExecutionNow();
		
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
	
}
