/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QoSMeasurementAttributesFormDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7212471265314450784L;
	
	private ServiceRegistryResponseDTO serviceRegistryEntry;
	private boolean isProviderAvailable = false;
	private String lastAccessAt;
	private Integer minResponseTime;
	private Integer maxResponseTime;
	private Integer meanResponseTimeWithTimeout;
	private Integer meanResponseTimeWithoutTimeout;
	private Integer jitterWithTimeout;
	private Integer jitterWithoutTimeout;
	private long sent;
	private long received;
	private long sentAll;
	private long receivedAll;
	private Integer lostPerMeasurementPercent;	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSMeasurementAttributesFormDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSMeasurementAttributesFormDTO(final ServiceRegistryResponseDTO serviceRegistryEntry, final boolean isProviderAvailable, final String lastAccessAt, final Integer minResponseTime,
										   final Integer maxResponseTime, final Integer meanResponseTimeWithTimeout, final Integer meanResponseTimeWithoutTimeout, final Integer jitterWithTimeout,
										   final Integer jitterWithoutTimeout, final long sent, final long received, final long sentAll, final long receivedAll, final Integer lostPerMeasurementPercent) {
		this.serviceRegistryEntry = serviceRegistryEntry;
		this.isProviderAvailable = isProviderAvailable;
		this.lastAccessAt = lastAccessAt;
		this.minResponseTime = minResponseTime;
		this.maxResponseTime = maxResponseTime;
		this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout;
		this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout;
		this.jitterWithTimeout = jitterWithTimeout;
		this.jitterWithoutTimeout = jitterWithoutTimeout;
		this.sent = sent;
		this.received = received;
		this.sentAll = sentAll;
		this.receivedAll = receivedAll;
		this.lostPerMeasurementPercent = lostPerMeasurementPercent;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryEntry() { return serviceRegistryEntry; } 	
	public boolean isProviderAvailable() { return isProviderAvailable; }
	public String getLastAccessAt() { return lastAccessAt; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public Integer getMeanResponseTimeWithoutTimeout() { return meanResponseTimeWithoutTimeout; }
	public Integer getJitterWithTimeout() { return jitterWithTimeout; }
	public Integer getJitterWithoutTimeout() { return jitterWithoutTimeout; } 
	public long getSent() { return sent; }
	public long getReceived() { return received; }
	public long getSentAll() { return sentAll; }
	public long getReceivedAll() { return receivedAll; }
	public Integer getLostPerMeasurementPercent() { return lostPerMeasurementPercent; }

	//-------------------------------------------------------------------------------------------------
	public void setServiceRegistryEntry(final ServiceRegistryResponseDTO serviceRegistryEntry) { this.serviceRegistryEntry = serviceRegistryEntry; }	
	public void setProviderAvailable(final boolean isProviderAvailable) { this.isProviderAvailable = isProviderAvailable; }
	public void setLastAccessAt(final String lastAccessAt) { this.lastAccessAt = lastAccessAt; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTimeWithTimeout(final Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithoutTimeout(final Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setJitterWithTimeout(final Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setLostPerMeasurementPercent(final Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
	public void setSent(final long sent) { this.sent = sent; }
	public void setReceived(final long received) { this.received = received; }
	public void setSentAll(final long sentAll) { this.sentAll = sentAll; }
	public void setReceivedAll(final long receivedAll) { this.receivedAll = receivedAll; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}