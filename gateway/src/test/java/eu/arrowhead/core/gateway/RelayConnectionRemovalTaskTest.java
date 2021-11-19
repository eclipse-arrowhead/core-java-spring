package eu.arrowhead.core.gateway;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.quartz.JobExecutionException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.core.gateway.quartz.RelayConnectionRemovalTask;
import eu.arrowhead.core.gateway.thread.ConsumerSideServerSocketThread;
import eu.arrowhead.core.gateway.thread.ProviderSideSocketThreadHandler;

@RunWith(SpringRunner.class)
public class RelayConnectionRemovalTaskTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private RelayConnectionRemovalTask testingObject;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecute_CommunicationStarted_Expired() throws JobExecutionException {
		final long threshold = 60;
		ReflectionTestUtils.setField(testingObject, "consumerSideThreshold", threshold);
		
		final String consumerQueueId = "ndfdaojmvfa";
		final ConsumerSideServerSocketThread consumerThread = Mockito.mock(ConsumerSideServerSocketThread.class);
		ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		activeConsumerSideSocketThreads.put(consumerQueueId, consumerThread);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(consumerThread.isCommunicationStarted()).thenReturn(true);
		when(consumerThread.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold + 5));
		
		final String providerQueueId = "dsagfdhzt";
		final ProviderSideSocketThreadHandler providerThreadHandler = Mockito.mock(ProviderSideSocketThreadHandler.class);
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = new ConcurrentHashMap<>();
		activeProviderSideSocketThreadHandlers.put(providerQueueId, providerThreadHandler);
		ReflectionTestUtils.setField(testingObject, "activeProviderSideSocketThreadHandlers", activeProviderSideSocketThreadHandlers);
		when(providerThreadHandler.isCommunicationStarted()).thenReturn(true);
		when(providerThreadHandler.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold + RelayConnectionRemovalTask.PERIOD + 5));
		
		testingObject.execute(null);
		
		verify(consumerThread, times(1)).setInterrupted(true);
		verify(providerThreadHandler, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecute_CommunicationStarted_NotExpired() throws JobExecutionException {
		final long threshold = 60;
		ReflectionTestUtils.setField(testingObject, "consumerSideThreshold", threshold);
		
		final String consumerQueueId = "ndfdaojmvfa";
		final ConsumerSideServerSocketThread consumerThread = Mockito.mock(ConsumerSideServerSocketThread.class);
		ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		activeConsumerSideSocketThreads.put(consumerQueueId, consumerThread);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(consumerThread.isCommunicationStarted()).thenReturn(true);
		when(consumerThread.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold - 5));
		
		final String providerQueueId = "dsagfdhzt";
		final ProviderSideSocketThreadHandler providerThreadHandler = Mockito.mock(ProviderSideSocketThreadHandler.class);
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = new ConcurrentHashMap<>();
		activeProviderSideSocketThreadHandlers.put(providerQueueId, providerThreadHandler);
		ReflectionTestUtils.setField(testingObject, "activeProviderSideSocketThreadHandlers", activeProviderSideSocketThreadHandlers);
		when(providerThreadHandler.isCommunicationStarted()).thenReturn(true);
		when(providerThreadHandler.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold + RelayConnectionRemovalTask.PERIOD - 5));
		
		testingObject.execute(null);
		
		verify(consumerThread, never()).setInterrupted(true);
		verify(providerThreadHandler, never()).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecute_CommunicationNotStarted_Expired() throws JobExecutionException {
		final long threshold = 60;
		ReflectionTestUtils.setField(testingObject, "consumerSideThreshold", threshold);
		
		final String consumerQueueId = "ndfdaojmvfa";
		final ConsumerSideServerSocketThread consumerThread = Mockito.mock(ConsumerSideServerSocketThread.class);
		ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		activeConsumerSideSocketThreads.put(consumerQueueId, consumerThread);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(consumerThread.isCommunicationStarted()).thenReturn(true);
		when(consumerThread.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold * 2 + 5));
		
		final String providerQueueId = "dsagfdhzt";
		final ProviderSideSocketThreadHandler providerThreadHandler = Mockito.mock(ProviderSideSocketThreadHandler.class);
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = new ConcurrentHashMap<>();
		activeProviderSideSocketThreadHandlers.put(providerQueueId, providerThreadHandler);
		ReflectionTestUtils.setField(testingObject, "activeProviderSideSocketThreadHandlers", activeProviderSideSocketThreadHandlers);
		when(providerThreadHandler.isCommunicationStarted()).thenReturn(true);
		when(providerThreadHandler.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold * 4 + 5));
		
		testingObject.execute(null);
		
		verify(consumerThread, times(1)).setInterrupted(true);
		verify(providerThreadHandler, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecute_CommunicationNotStarted_NotExpired() throws JobExecutionException {
		final long threshold = 60;
		ReflectionTestUtils.setField(testingObject, "consumerSideThreshold", threshold);
		
		final String consumerQueueId = "ndfdaojmvfa";
		final ConsumerSideServerSocketThread consumerThread = Mockito.mock(ConsumerSideServerSocketThread.class);
		ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		activeConsumerSideSocketThreads.put(consumerQueueId, consumerThread);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(consumerThread.isCommunicationStarted()).thenReturn(true);
		when(consumerThread.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold * 2 - 5));
		
		final String providerQueueId = "dsagfdhzt";
		final ProviderSideSocketThreadHandler providerThreadHandler = Mockito.mock(ProviderSideSocketThreadHandler.class);
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = new ConcurrentHashMap<>();
		activeProviderSideSocketThreadHandlers.put(providerQueueId, providerThreadHandler);
		ReflectionTestUtils.setField(testingObject, "activeProviderSideSocketThreadHandlers", activeProviderSideSocketThreadHandlers);
		when(providerThreadHandler.isCommunicationStarted()).thenReturn(true);
		when(providerThreadHandler.getLastInteractionTime()).thenReturn(ZonedDateTime.now().minusSeconds(threshold * 4 - 5));
		
		testingObject.execute(null);
		
		verify(consumerThread, times(1)).setInterrupted(true);
		verify(providerThreadHandler, times(1)).close();
	}
}
