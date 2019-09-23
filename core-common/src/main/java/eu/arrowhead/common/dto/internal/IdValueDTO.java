package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class IdValueDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4139505993669240877L;

	private long id;
	private String value;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IdValueDTO() {}

	//-------------------------------------------------------------------------------------------------
	public IdValueDTO(final long id, final String value) {
		this.id = id;
		this.value = value;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getValue() { return value; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setValue(final String value) { this.value = value; }
}