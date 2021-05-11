package eu.arrowhead.common.processor.model;

public class AddressDetectionResult {
	
	//=================================================================================================
	// members
	
	//-------------------------------------------------------------------------------------------------
	private boolean skipped;
	private boolean detectionSuccess;
	private String detectedAddress;
	private String detectionMessage;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AddressDetectionResult() {}

	//-------------------------------------------------------------------------------------------------
	public boolean isSkipped() { return skipped; }
	public boolean isDetectionSuccess() { return detectionSuccess; }
	public String getDetectedAddress() { return detectedAddress; }
	public String getDetectionMessage() { return detectionMessage; }

	//-------------------------------------------------------------------------------------------------
	public void setSkipped(final boolean skipped) { this.skipped = skipped; }
	public void setDetectionSuccess(final boolean detectionSuccess) { this.detectionSuccess = detectionSuccess; }
	public void setDetectedAddress(final String detectedAddress) { this.detectedAddress = detectedAddress; }
	public void setDetectionMessage(final String detectionMessage) { this.detectionMessage = detectionMessage; }
}
