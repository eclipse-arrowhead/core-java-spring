package eu.arrowhead.common.dto;

import java.io.Serializable;

public class GSDPollRequestDTO implements Serializable {

	//TODO: implement this class
	//TODO: need change in ActiveMQGatekeeperRelayTest if you changed the members
	
	private String requestedServiceDefinition;

	public String getRequestedServiceDefinition() {
		return requestedServiceDefinition;
	}

	public void setRequestedServiceDefinition(String requestedServiceDefinition) {
		this.requestedServiceDefinition = requestedServiceDefinition;
	}
}