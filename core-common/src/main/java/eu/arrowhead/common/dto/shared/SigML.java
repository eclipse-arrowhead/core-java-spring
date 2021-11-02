package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SigML implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7792814405188244770L;
	
	private Integer x;
	private String xs;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SigML() {}

	//-------------------------------------------------------------------------------------------------
	public SigML(final Integer x) {
		this.x = x;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SigML(final Integer x, final String xs) {
	  this.x = x;
	  this.xs = xs;
	}
 
	//-------------------------------------------------------------------------------------------------
	public Integer getX() { return x; }
	public String getXs() { return xs; }
	
	//-------------------------------------------------------------------------------------------------
	public void setX(final Integer x) { this.x = x; }
	public void setXs(final String xs) { this.xs = xs; }
  
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}