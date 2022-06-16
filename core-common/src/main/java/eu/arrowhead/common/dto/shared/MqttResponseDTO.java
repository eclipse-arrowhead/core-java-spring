package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;

public class MqttResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7233496112738400439L;
	
	private String code;
	private String contentType;
	private Object payload;

		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public MqttResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public MqttResponseDTO(final String code, final String contentType, final Object payload) {
        	this.code  = code;
        	this.contentType  = contentType;
		this.payload = payload;
	}	

	//-------------------------------------------------------------------------------------------------
	public String getCode() { return code; }
	public String getContentType() { return contentType; }
	public Object getPayload() { return payload; }

	//-------------------------------------------------------------------------------------------------
	public void setCode(final String code) { this.code = code; }
	public void setContentType(final String contentType) { this.contentType = contentType; }
	public void setPayload(final Object payload) { this.payload = payload; }
}
