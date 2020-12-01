package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.arrowhead.common.CommonConstants;


public class SigML implements Serializable {

  //=================================================================================================
  // members
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
