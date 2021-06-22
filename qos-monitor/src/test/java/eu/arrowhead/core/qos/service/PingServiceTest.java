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

package eu.arrowhead.core.qos.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

@RunWith(SpringRunner.class)
public class PingServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private PingService pingService;
	
	@Mock
	private PingMeasurementProperties pingMeasurementProperties;
	
	@Mock
	private QoSDBService qosDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingResponseListNullAddress() {
		pingService.getPingResponseList(null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingResponseListBlankAddress() {
		pingService.getPingResponseList("    ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMedianIntraPingMeasurementNullAttribute() {
		pingService.getMedianIntraPingMeasurement(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementWithDefaults() {
		ReflectionTestUtils.setField(pingService, "defaultReferenceMinResponseTime", 30);
		ReflectionTestUtils.setField(pingService, "defaultReferenceMaxResponseTime", 34);
		ReflectionTestUtils.setField(pingService, "defaultReferenceMeanResponseTimeWithTimeout", 40);
		ReflectionTestUtils.setField(pingService, "defaultReferenceMeanResponseTimeWithoutTimeout", 32);
		ReflectionTestUtils.setField(pingService, "defaultReferenceJitterWithTimeout", 10);
		ReflectionTestUtils.setField(pingService, "defaultReferenceJitterWithoutTimeout", 4);
		ReflectionTestUtils.setField(pingService, "defaultReferenceLostPerMeasurementPercent", 0);	
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(new ArrayList<>(), 0));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
		
		assertEquals(30, measurement.getMinResponseTime().intValue());
		assertEquals(34, measurement.getMaxResponseTime().intValue());
		assertEquals(40, measurement.getMeanResponseTimeWithTimeout().intValue());
		assertEquals(32, measurement.getMeanResponseTimeWithoutTimeout().intValue());
		assertEquals(10, measurement.getJitterWithTimeout().intValue());
		assertEquals(4, measurement.getJitterWithoutTimeout().intValue());
		assertEquals(0, measurement.getLostPerMeasurementPercent().intValue());
		assertTrue(measurement.isAvailable());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementMinResponseTime() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setMinResponseTime(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setMinResponseTime(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setMinResponseTime(4);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(median);
		data.add(higher);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.MIN_RESPONSE_TIME);
		
		assertEquals(median.getMinResponseTime(), measurement.getMinResponseTime());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementMaxResponseTime() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setMaxResponseTime(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setMaxResponseTime(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setMaxResponseTime(4);
		final QoSIntraPingMeasurementResponseDTO lowest = new QoSIntraPingMeasurementResponseDTO();
		lowest.setMaxResponseTime(1);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(higher);
		data.add(median);
		data.add(lowest);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.MAX_RESPONSE_TIME);
		
		assertEquals(median.getMaxResponseTime(), measurement.getMaxResponseTime());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementMeanResponseTimeWithTimeout() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setMeanResponseTimeWithTimeout(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setMeanResponseTimeWithTimeout(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setMeanResponseTimeWithTimeout(4);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(median);
		data.add(higher);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITH_TIMEOUT);
		
		assertEquals(median.getMeanResponseTimeWithTimeout(), measurement.getMeanResponseTimeWithTimeout());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementMeanResponseTimeWithoutTimeout() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setMeanResponseTimeWithoutTimeout(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setMeanResponseTimeWithoutTimeout(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setMeanResponseTimeWithoutTimeout(4);
		final QoSIntraPingMeasurementResponseDTO lowest = new QoSIntraPingMeasurementResponseDTO();
		lowest.setMeanResponseTimeWithoutTimeout(1);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(higher);
		data.add(median);
		data.add(lowest);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
		
		assertEquals(median.getMeanResponseTimeWithoutTimeout(), measurement.getMeanResponseTimeWithoutTimeout());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementJitterWithTimeout() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setJitterWithTimeout(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setJitterWithTimeout(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setJitterWithTimeout(4);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(median);
		data.add(higher);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.JITTER_WITH_TIMEOUT);
		
		assertEquals(median.getJitterWithTimeout(), measurement.getJitterWithTimeout());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementJitterWithoutTimeout() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setJitterWithoutTimeout(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setJitterWithoutTimeout(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setJitterWithoutTimeout(4);
		final QoSIntraPingMeasurementResponseDTO lowest = new QoSIntraPingMeasurementResponseDTO();
		lowest.setJitterWithoutTimeout(1);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(higher);
		data.add(median);
		data.add(lowest);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.JITTER_WITHOUT_TIMEOUT);
		
		assertEquals(median.getJitterWithoutTimeout(), measurement.getJitterWithoutTimeout());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMedianIntraPingMeasurementLostPerMeasurementPercent() {
		final QoSIntraPingMeasurementResponseDTO median = new QoSIntraPingMeasurementResponseDTO();
		median.setLostPerMeasurementPercent(5);
		final QoSIntraPingMeasurementResponseDTO higher = new QoSIntraPingMeasurementResponseDTO();
		higher.setLostPerMeasurementPercent(8);
		final QoSIntraPingMeasurementResponseDTO lower = new QoSIntraPingMeasurementResponseDTO();
		lower.setLostPerMeasurementPercent(4);
		
		final List<QoSIntraPingMeasurementResponseDTO> data = new ArrayList<>(3);
		data.add(median);
		data.add(higher);
		data.add(lower);
		
		when(qosDBService.getIntraPingMeasurementResponse(anyInt(), anyInt(), any(), any())).thenReturn(new QoSIntraPingMeasurementListResponseDTO(data, data.size()));		
		final QoSIntraPingMeasurementResponseDTO measurement = pingService.getMedianIntraPingMeasurement(QoSMeasurementAttribute.LOST_PER_MEASUREMENT_PERCENT);
		
		assertEquals(median.getLostPerMeasurementPercent(), measurement.getLostPerMeasurementPercent());
	}
}
