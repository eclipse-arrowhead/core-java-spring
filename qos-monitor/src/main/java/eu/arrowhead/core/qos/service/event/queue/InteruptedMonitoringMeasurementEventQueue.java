package eu.arrowhead.core.qos.service.event.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.arrowhead.core.qos.dto.event.InterruptedMonitoringMeasurementEventDTO;

public class InteruptedMonitoringMeasurementEventQueue {

	//=================================================================================================
	// members

	private static final BlockingQueue<InterruptedMonitoringMeasurementEventDTO> finishedMonitoringMeasurementEventQueue = new LinkedBlockingQueue<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void put(final InterruptedMonitoringMeasurementEventDTO toPut) throws InterruptedException {
		finishedMonitoringMeasurementEventQueue.put(toPut);
	}

	//-------------------------------------------------------------------------------------------------
	public InterruptedMonitoringMeasurementEventDTO take() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.take();
	}

	//-------------------------------------------------------------------------------------------------
	public InterruptedMonitoringMeasurementEventDTO poll() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.poll();
	}
}
