package eu.arrowhead.core.qos.dto;

import java.io.Serializable;

public class IcmpPingRequest implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 8327872537376847255L;

	private String host;
	private int ttl;
	private int packetSize;
	private long timeout;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequest() {}

	//-------------------------------------------------------------------------------------------------
	public String getHost() { return host; }
	public int getTtl() { return ttl; }
	public int getPacketSize() { return packetSize; }
	public long getTimeout() { return timeout; }

	//-------------------------------------------------------------------------------------------------
	public void setHost(final String host) { this.host = host; }
	public void setTtl(final int ttl) { this.ttl = ttl; }
	public void setPacketSize(final int packetSize) { this.packetSize = packetSize; }
	public void setTimeout(final long timeout) { this.timeout = timeout; }
}
