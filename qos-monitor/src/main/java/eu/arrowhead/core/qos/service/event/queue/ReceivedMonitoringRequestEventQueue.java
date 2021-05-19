package eu.arrowhead.core.qos.service.event.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.arrowhead.common.dto.shared.ReceivedMonitoringRequestEventDTO;

public class ReceivedMonitoringRequestEventQueue {

	//=================================================================================================
	// members

	private static final BlockingQueue<ReceivedMonitoringRequestEventDTO> finishedMonitoringMeasurementEventQueue = new LinkedBlockingQueue<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void put(final ReceivedMonitoringRequestEventDTO toPut) throws InterruptedException {
		finishedMonitoringMeasurementEventQueue.put(toPut);
	}

	//-------------------------------------------------------------------------------------------------
	public ReceivedMonitoringRequestEventDTO take() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.take();
	}

	//-------------------------------------------------------------------------------------------------
	public ReceivedMonitoringRequestEventDTO poll() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.poll();
	}
}
