package eu.arrowhead.common.processor.model;

public class IpPortDetectionResult {
	
	//=================================================================================================
	// members
	
	//-------------------------------------------------------------------------------------------------
	private boolean skipped;
	private boolean detectionSuccess;
	private String detectedAddress;
	private int detectedPort;
	private String detectionMessage;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IpPortDetectionResult() {}

	//-------------------------------------------------------------------------------------------------
	public boolean isSkipped() { return skipped; }
	public boolean isDetectedSuccess() { return detectionSuccess; }
	public String getDetectedAddress() { return detectedAddress; }
	public int getDetectedPort() { return detectedPort; }
	public String getDetectionMessage() { return detectionMessage; }

	//-------------------------------------------------------------------------------------------------
	public void setSkipped(final boolean skipped) { this.skipped = skipped; }
	public void setDetectedSuccess(final boolean detectionSuccess) { this.detectionSuccess = detectionSuccess; }
	public void setDetectedAddress(final String detectedAddress) { this.detectedAddress = detectedAddress; }
	public void setDetectedPort(final int detectedPort) { this.detectedPort = detectedPort; }
	public void setDetectionMessage(final String detectionMessage) { this.detectionMessage = detectionMessage; }
}
