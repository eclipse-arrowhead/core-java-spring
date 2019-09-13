package eu.arrowhead.client.skeleton.common.util;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;

public class CoreServiceUrl {
	
	//=================================================================================================
	// members
	
	private String address;
	private int port;
	private String uri;
	
	//-------------------------------------------------------------------------------------------------
	public CoreServiceUrl(final String address, final int port, final String uri) {
		Assert.isTrue(!Utilities.isEmpty(address), "address is null or blank");
		Assert.isTrue(!Utilities.isEmpty(uri), "uri is null or blank");
		
		this.address = address;
		this.port = port;
		this.uri = uri;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getUri() { return uri; }
	
	//-------------------------------------------------------------------------------------------------
	public void setAddress(final String address) { this.address = address; } 
	public void setPort(final int port) { this.port = port; }
	public void setUri(final String uri) { this.uri = uri; }		
}
