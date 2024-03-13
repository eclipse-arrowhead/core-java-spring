package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class ChoreographerRunPlanRequestByClientDTO extends ChoreographerRunPlanRequestDTO implements Serializable {
	
	//=================================================================================================
	// methods
	
	private static final long serialVersionUID = -7679624305702873335L;
	
	private String name;

	//-------------------------------------------------------------------------------------------------
	public String getName() { return name; }

	//-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
}
