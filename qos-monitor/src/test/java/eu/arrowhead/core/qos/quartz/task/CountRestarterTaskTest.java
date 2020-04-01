package eu.arrowhead.core.qos.quartz.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@RunWith(SpringRunner.class)
public class CountRestarterTaskTest {

	//=================================================================================================
	// members
	@InjectMocks
	private final CountRestarterTask counRestarterTask = new CountRestarterTask();

	@Mock
	private QoSDBService qoSDBService;

	@Mock
	private Map<String,Object> arrowheadContext;

	@Mock
	private JobExecutionContext jobExecutionContext;

	private Logger logger;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		logger = mock(Logger.class);
		ReflectionTestUtils.setField(counRestarterTask, "logger", logger);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteServerIsInStandalonMode() {

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(true);

		doNothing().when(qoSDBService).updateCountStartedAt();

		try {

			counRestarterTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(qoSDBService, times(0)).updateCountStartedAt();

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecute() {

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);

		doNothing().when(qoSDBService).updateCountStartedAt();

		try {

			counRestarterTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(qoSDBService, times(1)).updateCountStartedAt();

	}


	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteUpdateCountStartedAtThrowsException() {

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);

		doThrow(ArrowheadException.class).when(qoSDBService).updateCountStartedAt();

		try {

			counRestarterTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {

			fail();

		}catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(qoSDBService, times(1)).updateCountStartedAt();

			throw ex;
		}

	}
}
