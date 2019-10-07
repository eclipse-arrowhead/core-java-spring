package eu.arrowhead.core.eventhandler.publish;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class PublishRequestExecutorTest {
	
	//=================================================================================================
	// members
	
	private PublishRequestExecutor testingObject;
	
	private  ThreadPoolExecutor threadPool;
	
	@Mock
	private HttpService httpService;
	
	private final int numberOfSubscribers = 17;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
        
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions( numberOfSubscribers );

		testingObject = new PublishRequestExecutor( request, involvedSubscriptions, httpService );
       
		threadPool = mock(ThreadPoolExecutor.class, "threadPool");
		ReflectionTestUtils.setField( testingObject, "threadPool", threadPool);
    }
	
	//=================================================================================================
	//Tests of execute
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteOk() {
		
		final Subscription subscription0 = createSubscriptionForDBMock( 1 , "eventType1", "subscriberName1" );
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		final PublishEventTask publishEventTask = new PublishEventTask( subscription0, request, httpService );
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass( PublishEventTask.class);
		doNothing().when( threadPool ).execute( valueCapture.capture() );
		
		testingObject.execute();
		
		verify( threadPool, times( numberOfSubscribers ) ).execute( any() );
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField( publishEventTask, "subscription");
		
		assertNotNull( subscriptionInTask );
		
		verify( threadPool, times( 1 ) ).shutdown();
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test( expected = IllegalArgumentException.class )
	public void testExecuteInvalidFieldInvolvedSubscriptionsNullCoseNoExecution() {
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();

		ReflectionTestUtils.setField( testingObject, "publishRequestDTO", request );
		ReflectionTestUtils.setField( testingObject, "involvedSubscriptions", null );

		doNothing().when( threadPool ).execute( any() );
		
		try {
			
			testingObject.execute();
			
		} catch (Exception ex) {
			
			verify( threadPool, times( 0 ) ).execute( any() );

			assertTrue( ex.getMessage().contains( "involvedSubscriptions is null" ) );
		
			throw ex;
		}
	
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test( expected = IllegalArgumentException.class )
	public void testExecuteInvalidFieldRequestNullCoseNoExecution() {
		
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions( numberOfSubscribers );
	
		ReflectionTestUtils.setField( testingObject, "publishRequestDTO", null );
		ReflectionTestUtils.setField( testingObject, "involvedSubscriptions", involvedSubscriptions );

		doNothing().when( threadPool ).execute( any() );
		
		try {
			
			testingObject.execute();
			
		} catch (Exception ex) {
			
			verify( threadPool, times( 0 ) ).execute( any() );

			assertTrue( ex.getMessage().contains( "publishRequestDTO is null" ) );
		
			throw ex;
		}
	
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
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		
		return new EventPublishRequestDTO(
				"eventType", 
				getSystemRequestDTO(), //source, 
				null, //metaData, 
				"payload", 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
	
}
