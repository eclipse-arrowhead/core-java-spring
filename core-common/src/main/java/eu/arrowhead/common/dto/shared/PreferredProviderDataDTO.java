package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PreferredProviderDataDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6954138371746479685L;
	
	private SystemRequestDTO providerSystem;
	private CloudRequestDTO providerCloud;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getProviderSystem() { return providerSystem; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public void setProviderSystem(final SystemRequestDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean isLocal() {
		return providerSystem != null && providerCloud == null;
	}
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean isGlobal() {
		return providerCloud != null;
	}
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean isValid() {
		return isLocal() || isGlobal();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
	  return "PreferredProvider{" + "providerSystem=" + providerSystem + ", providerCloud=" + providerCloud + '}';
	}
}
