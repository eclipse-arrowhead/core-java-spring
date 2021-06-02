package eu.arrowhead.core.qos.service.ping.monitor;

import java.io.Serializable;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.event.monitoringevents.MeasurementMonitoringEvent;

public class PingEventBufferElement implements Serializable {

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------

	private static final long serialVersionUID = -4236505546657946368L;

	private final UUID id;
	private final long createdAt;

	private final MeasurementMonitoringEvent eventList[];

	private Logger logger = LogManager.getLogger(PingEventBufferElement.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingEventBufferElement(final UUID id) {
		this.id = id;
		this.createdAt = System.currentTimeMillis();
		this.eventList = new MeasurementMonitoringEvent[QosMonitorConstants.EVENT_LIST_SIZE];

	}

	//-------------------------------------------------------------------------------------------------
	public UUID getId() {return id;}
	public long getCreatedAt() {return createdAt;}
	public MeasurementMonitoringEvent[] getEventlist() {return eventList;}

	//-------------------------------------------------------------------------------------------------
	public void addEvent( final int position, final MeasurementMonitoringEvent event) {
		logger.debug("addEvent started...");

		Assert.isTrue(position < QosMonitorConstants.EVENT_LIST_SIZE, "Invalid eventList position.");

		eventList[position] = event;
	}

}
