package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KeyValuesDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5843236299108271323L;
	
	private Map<String,String> map = new HashMap<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public KeyValuesDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public KeyValuesDTO(final Map<String, String> map) {
		this.map = map;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<String, String> getMap() { return map; }
	
	//-------------------------------------------------------------------------------------------------
	public void setMap(final Map<String, String> map) { this.map = map; }
}
