package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Set;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;

public class EventPublishStartDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 882133275790725633L;
	
	private EventPublishRequestDTO request;
	private Set<Subscription> involvedSubscriptions;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishStartDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishStartDTO(final EventPublishRequestDTO request, final Set<Subscription> involvedSubscriptions) {
		this.request = request;
		this.involvedSubscriptions = involvedSubscriptions;
	}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishRequestDTO getRequest() { return request; }
	public Set<Subscription> getInvolvedSubscriptions() { return involvedSubscriptions; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequest(final EventPublishRequestDTO request) { this.request = request; }
	public void setInvolvedSubscriptions(final Set<Subscription> involvedSubscriptions) { this.involvedSubscriptions = involvedSubscriptions; }
}