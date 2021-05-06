package eu.arrowhead.core.qos.service.event.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.arrowhead.core.qos.dto.event.FinishedMonitoringMeasurementEventDTO;

public class FinishedMonitoringMeasurementEventQueue {

	//=================================================================================================
	// members

	private static final BlockingQueue<FinishedMonitoringMeasurementEventDTO> finishedMonitoringMeasurementEventQueue = new LinkedBlockingQueue<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void put(final FinishedMonitoringMeasurementEventDTO toPut) throws InterruptedException {
		finishedMonitoringMeasurementEventQueue.put(toPut);
	}

	//-------------------------------------------------------------------------------------------------
	public FinishedMonitoringMeasurementEventDTO take() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.take();
	}

	//-------------------------------------------------------------------------------------------------
	public FinishedMonitoringMeasurementEventDTO poll() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.poll();
	}
}
