package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class CloudAccessResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2559154226729243387L;
	
	private String cloudName;
	private String cloudOperator;
	private boolean directAccess;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudAccessResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudAccessResponseDTO(final String cloudName, final String cloudOperator, final boolean directAccess) {
		this.cloudName = cloudName;
		this.cloudOperator = cloudOperator;
		this.directAccess = directAccess;
	}

	//-------------------------------------------------------------------------------------------------
	public String getCloudName() { return cloudName; }
	public String getCloudOperator() { return cloudOperator; }
	public boolean isDirectAccess() { return directAccess; }

	//-------------------------------------------------------------------------------------------------
	public void setCloudName(final String cloudName) { this.cloudName = cloudName; }
	public void setCloudOperator(final String cloudOperator) { this.cloudOperator = cloudOperator; }
	public void setDirectAccess(final boolean directAccess) { this.directAccess = directAccess; }
	
}
