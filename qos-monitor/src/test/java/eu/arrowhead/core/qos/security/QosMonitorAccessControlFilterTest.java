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

package eu.arrowhead.core.qos.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.CloudRelayFormDTO;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.QoSBestRelayRequestDTO;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

/**
* IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
*
*/
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class QosMonitorAccessControlFilterTest {

	//=================================================================================================
	// members

	private static final String ECHO_URI = CommonConstants.QOSMONITOR_URI + CommonConstants.ECHO_URI;
	private static final String PUBLIC_KEY_URI = CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_KEY_URI;
	private static final String INTRA_PING_MEASUREMENTS_MGMT_URI = CommonConstants.QOSMONITOR_URI + CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT;
	private static final String INTRA_PING_MEDIAN_URI = "/measurements/intracloud/ping_median/";
	private static final String INTER_PING_MEASUREMENTS_MGMT_URI = CommonConstants.QOSMONITOR_URI + CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT;
	private static final String INTER_PING_PAIR_MEASUREMENTS_MGMT_URI = CommonConstants.QOSMONITOR_URI + CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT + "/pair_results";
	private static final String INTER_RELAY_MEASUREMENTS_MGMT_URI = CommonConstants.QOSMONITOR_URI + CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT;
	private static final String INTER_RELAY_PAIR_MEASUREMENTS_MGMT_URI = CommonConstants.QOSMONITOR_URI + CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT + "/pair_results";
	private static final String INTER_RELAY_BEST_URI = "/mgmt/measurements/intercloud/relay_echo/best_relay";
	
	@Autowired
	private ApplicationContext appContext;

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;

	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		assumeTrue(secure);

		final QosMonitorAccessControlFilter qoSFilter = appContext.getBean(QosMonitorAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(qoSFilter)
									  .build();
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateSameCloud() throws Exception {
		this.mockMvc.perform(get(ECHO_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(ECHO_URI)
					.secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetIntraPingMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(INTRA_PING_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetIntraPingMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(INTRA_PING_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT)
				.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMeasurementsCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/1")
					.secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMeasurementsCertificateGatekeeper() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/1")
					.secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMeasurementsCertificateNotOrchestratorOrGatekeeper() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/1")
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMedianMeasurementsCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + INTRA_PING_MEDIAN_URI + "fake_attribute")
					.secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetIntraPingMedianMeasurementsCertificateNotOrchestrator() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + INTRA_PING_MEDIAN_URI + "fake_attribute")
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterPingMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(INTER_PING_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterPingMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(INTER_PING_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterPingPairMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(post(INTER_PING_PAIR_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudSystemFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterPingPairMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(post(INTER_PING_PAIR_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudSystemFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetInterPingPairMeasurementsCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT)
					.secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudSystemFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetInterPingPairMeasurementsCertificateNotOrchestrator() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudSystemFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterRelayMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(INTER_RELAY_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterRelayMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(INTER_RELAY_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterRelayPairMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(post(INTER_RELAY_PAIR_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudRelayFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterRelayPairMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(post(INTER_RELAY_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudRelayFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterRelayBestMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + INTER_RELAY_BEST_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSBestRelayRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetInterRelayBestMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + INTER_RELAY_BEST_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSBestRelayRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetInterRelayMeasurementsByCloudCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT)
					.secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetInterRelayMeasurementsByCloudCertificateNotOrchestrator() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new CloudRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublicKeyCertificateSameCloud() throws Exception {
		this.mockMvc.perform(get(PUBLIC_KEY_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublicKeyCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(PUBLIC_KEY_URI)
					.secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPJoinRelayTestCertificateGatekeeper() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_JOIN_RELAY_TEST_URI)
					.secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSRelayTestProposalRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPJoinRelayTestCertificateNotGatekeeper() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_JOIN_RELAY_TEST_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSRelayTestProposalRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPInitRelayTestCertificateGatekeeper() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INIT_RELAY_TEST_URI)
					.secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSMonitorSenderConnectionRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPInitRelayTestCertificateNotGatekeeper() throws Exception {
		this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INIT_RELAY_TEST_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSMonitorSenderConnectionRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
}