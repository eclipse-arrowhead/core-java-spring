package eu.arrowhead.core.qos.service.event.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.arrowhead.core.qos.dto.event.InteruptedMonitoringMeasurementEventDTO;

public class InteruptedMonitoringMeasurementEventQueue {

	//=================================================================================================
	// members

	private static final BlockingQueue<InteruptedMonitoringMeasurementEventDTO> finishedMonitoringMeasurementEventQueue = new LinkedBlockingQueue<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void put(final InteruptedMonitoringMeasurementEventDTO toPut) throws InterruptedException {
		finishedMonitoringMeasurementEventQueue.put(toPut);
	}

	//-------------------------------------------------------------------------------------------------
	public InteruptedMonitoringMeasurementEventDTO take() throws InterruptedException {
		return finishedMonitoringMeasurementEventQueue.take();
	}
}
