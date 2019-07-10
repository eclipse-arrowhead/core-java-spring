package eu.arrowhead.common.dto;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.System;

public class PreferredProviderDataDTO {

	
	//=================================================================================================
	// members
	
	private System providerSystem;
	private Cloud providerCloud;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public System getProviderSystem() { return providerSystem; }
	public Cloud getProviderCloud() { return providerCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public void setProviderSystem(System providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(Cloud providerCloud) { this.providerCloud = providerCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public boolean isLocal() {
		return providerSystem != null && providerCloud == null;
	}
	
	public boolean isGlobal() {
		return providerCloud != null;
	}
	
	public boolean isValid() {
		return isLocal() || isGlobal();
	}
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
	  return "PreferredProvider{" + "providerSystem=" + providerSystem + ", providerCloud=" + providerCloud + '}';
	}
}
