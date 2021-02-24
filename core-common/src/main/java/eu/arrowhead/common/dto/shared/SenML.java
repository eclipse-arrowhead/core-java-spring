package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


import eu.arrowhead.common.CommonConstants;

//https://tools.ietf.org/html/rfc8428

@JsonInclude(Include.NON_NULL)
public class SenML implements Serializable {

  public static final long RELATIVE_TIMESTAMP_INDICATOR = 268435456L;

  //=================================================================================================
  // members
  private String bn;
  private Double bt;
  private String bu;
  private Double bv;
  private Double bs;
  private Short bver;
  private String n;
  private String u;
  private Double v = null;
  private String vs = null;
  private Boolean vb = null;
  private String vd = null;
  private Double s = null;
  private Double t = null;
  private Double ut = null;

  //=================================================================================================
  // methods

  //-------------------------------------------------------------------------------------------------
  public SenML() {
  }
 
  public void setBn(String bn) {
    this.bn = bn;
  }

  public String getBn() {
    return bn;
  }

  public void setBt(Double bt) {
    this.bt = bt;
  }

  public Double getBt() {
    return bt;
  }

  public void setBu(String bu) {
    this.bu = bu;
  }

  public String getBu() {
    return bu;
  }

  public void setBv(Double bv) {
    this.bv = bv;
  }

  public Double getBv() {
    return bv;
  }

  public void setBs(Double bs) {
    this.bs = bs;
  }

  public Double getBs() {
    return bs;
  }

  public void setBver(Short bver) {
    this.bver = bver;
  }

  public Short getBver() {
    return bver;
  }

  public void setN(String n) {
    this.n = n;
  }

  public String getN() {
    return n;
  }

  public String getU() {
    return u;
  }

  public void setU(String u) {
    this.u = u;
  }

  public void setV(Double v) {
    this.v = v;
  }

  public Double getV() {
    return v;
  }

  public void setVs(String vs) {
    this.vs = vs;
  }

  public String getVs() {
    return vs;
  }

  public void setVb(Boolean vb) {
    this.vb = vb;
  }

  public Boolean getVb() {
    return vb;
  }

  public void setVd(String vd) {
    this.vd = vd;
  }

  public String getVd() {
    return vd;
  }

  public void setS(Double s) {
    this.s = s;
  }

  public Double getS() {
    return s;
  }
 
  public void setT(Double t) {
    this.t = t;
  }

  public Double getT() {
    return t;
  }

  public void setUt(Double ut) {
    this.ut = ut;
  }

  public Double getUt() {
    return ut;
  }
  
  @Override
  public String toString() {
    return "SenML [bn=" + bn + ", bt=" + bt + ", bu=" + bu + ", bv=" + bv
		+ ", bs=" + bs + ", bver=" + bver + ", n=" + n + ", u=" + u + ", v="
		+ v + ", vs=" + vs + ", vb=" + vb + ", vd=" + vd + ", s=" + s
		+ ", t=" + t + ", ut=" + ut + "]";
}

  //-------------------------------------------------------------------------------------------------
}
