package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class MqttRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2128696165217136429L;
	
	private String method;
	private Map<String, String> queryParameters = new HashMap<>();
	private String replyTo;
	private Object payload;

		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public MqttRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public MqttRequestDTO(final String method, Map<String, String> queryParameters, final String replyTo, final Object payload) {
        	this.method  = method;
        	this.queryParameters = queryParameters;
        	this.replyTo  = replyTo;
		this.payload = payload;
	}	

	//-------------------------------------------------------------------------------------------------
	public String getMethod() {	return method; }
	public Map<String, String> getQueryParameters() { return queryParameters; }
	public String getReplyTo() { return replyTo; }
	public Object getPayload() { return payload; }

	//-------------------------------------------------------------------------------------------------
	public void setMethod(final String method) { this.method = method; }
	public void setQueryParameters(final Map<String, String> queryParameters) { this.queryParameters = queryParameters; }
	public void setReplyTo(final String replyTo) { this.replyTo = replyTo; }
	public void setPayload(final Object payload) { this.payload = payload; }
}
