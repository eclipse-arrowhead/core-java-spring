package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class IcmpPingRequestDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 8327872537376847255L;

	private String host;
	private Integer ttl;
	private Integer packetSize;
	private Long timeout;
	private Integer timeToRepeat;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public String getHost() { return host; }
	public Integer getTtl() { return ttl; }
	public Integer getPacketSize() { return packetSize; }
	public Long getTimeout() { return timeout; }
	public Integer getTimeToRepeat() { return timeToRepeat; }

	//-------------------------------------------------------------------------------------------------
	public void setHost(final String host) { this.host = host; }
	public void setTtl(final Integer ttl) { this.ttl = ttl; }
	public void setPacketSize(final Integer packetSize) { this.packetSize = packetSize; }
	public void setTimeout(final Long timeout) { this.timeout = timeout; }
	public void setTimeToRepeat(final Integer timeToRepeat) { this.timeToRepeat = timeToRepeat; }
}
