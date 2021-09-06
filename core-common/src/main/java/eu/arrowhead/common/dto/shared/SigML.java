package eu.arrowhead.common.dto.shared;

import java.io.Serializable;


public class SigML implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7792814405188244770L;
	
	private Integer x;
	private String xs;

  //=================================================================================================
  // methods

  //-------------------------------------------------------------------------------------------------
  public SigML(Integer x) {
	  this.x = x;
  }
  public SigML(Integer x, String xs) {
	  this.x = x;
	  this.xs = xs;
  }
 
  public void setX(Integer x) {
    this.x = x;
  }
  public Integer getX() {
    return x;
  }

  public void setXs(String xs) {
    this.xs = xs;
  }

  public String getXs() {
    return xs;
  }


  //-------------------------------------------------------------------------------------------------
}
