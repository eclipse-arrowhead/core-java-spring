package eu.arrowhead.core.qos.service.ping.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.QosMonitorConstants;

public class PingEventBufferElement implements Serializable {

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------

	private static final long serialVersionUID = -4236505546657946368L;

	private final UUID id;
	private final long createdAt;

	private final List<EventDTO> eventList;

	private Logger logger = LogManager.getLogger(PingEventBufferElement.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingEventBufferElement(final UUID id) {
		this.id = id;
		this.createdAt = System.currentTimeMillis();
		this.eventList = new ArrayList<>();
	}

	//-------------------------------------------------------------------------------------------------
	public UUID getId() {return id;}
	public long getCreatedAt() {return createdAt;}
	public List<EventDTO> getEventlist() {return eventList;}

	//-------------------------------------------------------------------------------------------------
	public void addEvent(final EventDTO event) {
		logger.debug("addEvent started...");

		validateEvent(event);
		eventList.add(event);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------

	private void validateEvent(final EventDTO event) {
		logger.debug("validateEvent started...");

		Assert.notNull(event, "Event is null");
		Assert.notNull(event.getMetaData(), "Event metadata is null");

		if (event.getMetaData().containsKey(QosMonitorConstants.PROCESS_ID_KEY)) {
			try {
				final UUID eventId = UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY));
				if(!eventId.equals(id)) {
					throw new ArrowheadException("Event id is not equals the PingBufferElement id.");
				}
			} catch (final IllegalArgumentException ex) {

				throw new ArrowheadException("Event id could not be parsed to UUID.");
			}
		}else {
			throw new ArrowheadException("Event metadata must contain : " + QosMonitorConstants.PROCESS_ID_KEY);
		}
	}
}
