package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;

@RunWith(SpringRunner.class)
public class GSDPollRequestExecutorTest {

	//=================================================================================================
	// members
	
	private GSDPollRequestExecutor testingObject;

	private GatekeeperRelayClient relayClient;
	
	private final BlockingQueue<GSDPollResponseDTO> queue = new LinkedBlockingQueue<>(1);
	
	private ThreadPoolExecutor threadPool;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {		
		relayClient = mock(GatekeeperRelayClient.class, "relayClient");
		testingObject = new GSDPollRequestExecutor(queue, relayClient, new GSDPollRequestDTO(), createGatekeeperRelayPerCloudMap());
		threadPool = mock(ThreadPoolExecutor.class, "threadPool");
		ReflectionTestUtils.setField(testingObject, "threadPool",  threadPool);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteWithThrowingRejectedExecutionException() throws InterruptedException {
		doThrow(RejectedExecutionException.class).when(threadPool).execute(any());
		
		testingObject.execute();
		
		final GSDPollResponseDTO gsdPollResponseDTO = queue.take();
		
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Map<Cloud, Relay> createGatekeeperRelayPerCloudMap() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-cloud-auth-info");
		
		final Relay relay = new Relay();
		relay.setId(1);
		relay.setAddress("test-address");
		relay.setPort(1000);
		relay.setSecure(true);
		relay.setExclusive(false);
		relay.setType(RelayType.GATEKEEPER_RELAY);
		
		final CloudGatekeeperRelay conn = new CloudGatekeeperRelay(cloud, relay);
		cloud.getGatekeeperRelays().add(conn);
		relay.getCloudGatekeepers().add(conn);
		
		return Map.of(cloud, relay);
	}
}
