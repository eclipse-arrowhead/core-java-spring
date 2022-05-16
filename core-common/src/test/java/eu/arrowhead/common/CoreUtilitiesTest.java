/********************************************************************************
 * Copyright (c) 2019 AITIA
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

package eu.arrowhead.common;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.exception.BadPayloadException;

@RunWith(SpringRunner.class)
public class CoreUtilitiesTest {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCalculateDirectionDirectionNull() {
		CoreUtilities.calculateDirection(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCalculateDirectionDirectionEmpty() {
		CoreUtilities.calculateDirection(" ", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCalculateDirectionDirectionInvalid() {
		CoreUtilities.calculateDirection("invalid", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateDirectionDirectionAsc() {
		final Direction direction = CoreUtilities.calculateDirection(" ASC ", null);
		Assert.assertEquals(Direction.ASC, direction);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateDirectionDirectionDesc() {
		final Direction direction = CoreUtilities.calculateDirection("desc", null);
		Assert.assertEquals(Direction.DESC, direction);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidatePageParametersPageAndSizeNull() {
		final ValidatedPageParams vpp = CoreUtilities.validatePageParameters(null, null, "ASC", "origin");
		Assert.assertEquals(0, vpp.getValidatedPage());
		Assert.assertEquals(Integer.MAX_VALUE, vpp.getValidatedSize());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testValidatePageParametersPageNullAndSizeNotNull() {
		CoreUtilities.validatePageParameters(null, 10, "ASC", "origin");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testValidatePageParametersPageNotNullAndSizeNull() {
		CoreUtilities.validatePageParameters(0, null, "ASC", "origin");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidatePageParametersPageAndSizeNotNull() {
		final ValidatedPageParams vpp = CoreUtilities.validatePageParameters(1, 15, "ASC", "origin");
		Assert.assertEquals(1, vpp.getValidatedPage());
		Assert.assertEquals(15, vpp.getValidatedSize());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelNull() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(null, null);
		Assert.assertNull(logLevels);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelEmpty() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("", null);
		Assert.assertNull(logLevels);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testGetLogLevelsMaxLevelInvalid() {
		try {
			CoreUtilities.getLogLevels("unknown", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Defined log level is not exists.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelOff() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("off", null);
		Assert.assertEquals(1, logLevels.size());
		Assert.assertEquals(LogLevel.OFF, logLevels.get(0));
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelFatal() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("fatal", null);
		Assert.assertEquals(2, logLevels.size());
		Assert.assertEquals(LogLevel.FATAL, logLevels.get(0));
		Assert.assertEquals(LogLevel.OFF, logLevels.get(1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelError() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("error", null);
		Assert.assertEquals(3, logLevels.size());
		Assert.assertEquals(LogLevel.ERROR, logLevels.get(0));
		Assert.assertEquals(LogLevel.FATAL, logLevels.get(1));
		Assert.assertEquals(LogLevel.OFF, logLevels.get(2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelWarn() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("warn", null);
		Assert.assertEquals(4, logLevels.size());
		Assert.assertEquals(LogLevel.WARN, logLevels.get(0));
		Assert.assertEquals(LogLevel.ERROR, logLevels.get(1));
		Assert.assertEquals(LogLevel.FATAL, logLevels.get(2));
		Assert.assertEquals(LogLevel.OFF, logLevels.get(3));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelInfo() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("info", null);
		Assert.assertEquals(5, logLevels.size());
		Assert.assertEquals(LogLevel.INFO, logLevels.get(0));
		Assert.assertEquals(LogLevel.WARN, logLevels.get(1));
		Assert.assertEquals(LogLevel.ERROR, logLevels.get(2));
		Assert.assertEquals(LogLevel.FATAL, logLevels.get(3));
		Assert.assertEquals(LogLevel.OFF, logLevels.get(4));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelDebug() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("debug", null);
		Assert.assertEquals(6, logLevels.size());
		Assert.assertEquals(LogLevel.DEBUG, logLevels.get(0));
		Assert.assertEquals(LogLevel.INFO, logLevels.get(1));
		Assert.assertEquals(LogLevel.WARN, logLevels.get(2));
		Assert.assertEquals(LogLevel.ERROR, logLevels.get(3));
		Assert.assertEquals(LogLevel.FATAL, logLevels.get(4));
		Assert.assertEquals(LogLevel.OFF, logLevels.get(5));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogLevelsMaxLevelTrace() {
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels("trace", null);
		Assert.assertEquals(7, logLevels.size());
		Assert.assertEquals(LogLevel.TRACE, logLevels.get(0));
		Assert.assertEquals(LogLevel.DEBUG, logLevels.get(1));
		Assert.assertEquals(LogLevel.INFO, logLevels.get(2));
		Assert.assertEquals(LogLevel.WARN, logLevels.get(3));
		Assert.assertEquals(LogLevel.ERROR, logLevels.get(4));
		Assert.assertEquals(LogLevel.FATAL, logLevels.get(5));
		Assert.assertEquals(LogLevel.OFF, logLevels.get(6));
	}
}