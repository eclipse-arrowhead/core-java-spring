package eu.arrowhead.core.qos.quartz.task;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

@RunWith(SpringRunner.class)
public class PingTaskTest {

	//=================================================================================================
	// members
	@InjectMocks
	private PingTask pingTask = new PingTask();

	@Mock
	private QoSDBService qoSDBService;

	@Mock
	private HttpService httpService;

	@Mock
	private PingMeasurementProperties pingMeasurementProperties;

	@Mock
	private Map<String,Object> arrowheadContext;

	private Logger logger;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	//@BeforeClass
	//public void initializeArrowheadContext() {
	//	arrowheadContext.
	//}

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		logger = mock(Logger.class);		
		ReflectionTestUtils.setField(pingTask, "logger", logger);
	}

	//=================================================================================================
	// Tests of execute
	@Test
	public void testExecute() {

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		//TODO Implement testing logic here
	}
}
