package eu.arrowhead.core.qos.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;

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

	private static final String QOS_MONITOR_ECHO_URI = CommonConstants.QOS_MONITOR_URI + CommonConstants.ECHO_URI;

	private static final String PING_MEASUREMENTS = "/ping/measurements";
	private static final String QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI = CommonConstants.QOS_MONITOR_URI + CoreCommonConstants.MGMT_URI + PING_MEASUREMENTS;

	@Autowired
	private ApplicationContext appContext;

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;

	@Autowired
	private WebApplicationContext wac;

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
		this.mockMvc.perform(get(QOS_MONITOR_ECHO_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(QOS_MONITOR_ECHO_URI)
					.secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetPingMeasurementsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtGetPingMeasurementsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetPingMeasurementsCertificateSysop() throws Exception {

		this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT)
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetPingMeasurementsCertificateNotSysop() throws Exception {

		this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT)
				.secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOPGetPingMeasurementsCertificateOrchestrator() throws Exception {

		this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/1")
				.secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
}
