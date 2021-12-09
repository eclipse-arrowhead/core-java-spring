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

package eu.arrowhead.common.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.LogsRepository;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@RunWith(SpringRunner.class)
public class CommonDBServiceTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private CommonDBService commonDBService; 

	@Mock
	private CloudRepository cloudRepository;
	
	@Mock
	private LogsRepository logsRepository;
	
	@Mock
	private CommonNamePartVerifier cnVerifier;
	
	private final CommonNamePartVerifier realVerifier = new CommonNamePartVerifier();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		when(cnVerifier.isValid(any(String.class))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				return realVerifier.isValid(invocation.getArgument(0));
			}
		});
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetOwnCloudNoResult() {
		when(cloudRepository.findByOwnCloudAndSecure(true, true)).thenReturn(List.of());
		commonDBService.getOwnCloud(true);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetOwnCloudTooMuchResult() {
		when(cloudRepository.findByOwnCloudAndSecure(true, true)).thenReturn(List.of(new Cloud(), new Cloud()));
		commonDBService.getOwnCloud(true);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInsertOwnCloudOperatorWrongFormat() {
		try {
			commonDBService.insertOwnCloud("operator_wrong", "name", false, null);
		} catch (final InvalidParameterException ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Operator has invalid format. Operator must match with the following regular expression: "));			
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInsertOwnCloudNameWrongFormat() {
		try {
			commonDBService.insertOwnCloud("valid-operator", "name_wrong", false, null);
		} catch (final InvalidParameterException ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Name has invalid format. Name must match with the following regular expression: "));			
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInsertOwnCloudCloudAlreadyExists() {
		when(cloudRepository.findByOperatorAndName("operator", "name")).thenReturn(Optional.of(new Cloud()));

		try {
			commonDBService.insertOwnCloud("operator", "name", false, null);
		} catch (final InvalidParameterException ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Cloud with operator"));			
			Assert.assertTrue(ex.getMessage().endsWith(" is already exists."));			
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetLogEntriesSystemNull() {
		try {
			commonDBService.getLogEntries(0, 10, Direction.ASC, "entryDate", null, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("System is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetLogEntriesInvalidSortField() {
		try {
			commonDBService.getLogEntries(0, 10, Direction.ASC, "invalid", CoreSystem.SERVICEREGISTRY, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Sortable field with reference 'invalid' is not available", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetLogEntriesInvalidTimeInterval() {
		final ZonedDateTime from = ZonedDateTime.now();
		final ZonedDateTime to = from.minusHours(1);
		
		try {
			commonDBService.getLogEntries(0, 10, Direction.ASC, "entryDate", CoreSystem.SERVICEREGISTRY, null, from, to, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid time interval", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test(expected = ArrowheadException.class)
	public void testGetLogEntriesDatabaseError() {
		when(logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetween(eq(CoreSystem.SERVICEREGISTRY), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class), any(Pageable.class))).thenThrow(new RuntimeException("test"));
		try {
			commonDBService.getLogEntries(0, 10, Direction.ASC, "entryDate", CoreSystem.SERVICEREGISTRY, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			final ArgumentCaptor<List<LogLevel>> levelListCaptor = ArgumentCaptor.forClass(List.class);
			final ArgumentCaptor<ZonedDateTime> fromCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
			final ArgumentCaptor<ZonedDateTime> toCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
			
			verify(logsRepository, times(1)).findAllBySystemAndLogLevelInAndEntryDateBetween(eq(CoreSystem.SERVICEREGISTRY), levelListCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), any(Pageable.class));
			
			Assert.assertEquals(7, levelListCaptor.getValue().size());
			final ZonedDateTime from = fromCaptor.getValue();
			Assert.assertEquals(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 1, ZoneId.systemDefault()), from);
			final ZonedDateTime to = toCaptor.getValue();
			Assert.assertNotNull(to);
			Assert.assertTrue(to.isAfter(from));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogEntriesOk() {
		final Logs entry = new Logs("log", ZonedDateTime.now(), "logger", CoreSystem.SERVICEREGISTRY, LogLevel.INFO, "test", null);
		final ZonedDateTime to = ZonedDateTime.now();
		final ZonedDateTime from = to.minusHours(1);
		
		when(logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetween(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entry)));
		
		final Page<Logs> result = commonDBService.getLogEntries(0, 10, Direction.ASC, "entryDate", CoreSystem.SERVICEREGISTRY, null, from, to, null);
		
		verify(logsRepository, times(1)).findAllBySystemAndLogLevelInAndEntryDateBetween(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), any(Pageable.class));
		
		Assert.assertEquals(1, result.getContent().size());
		Assert.assertEquals("log", result.getContent().get(0).getLogId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogEntriesOkWithLogger() {
		final Logs entry = new Logs("log", ZonedDateTime.now(), "logger", CoreSystem.SERVICEREGISTRY, LogLevel.INFO, "test", null);
		final ZonedDateTime to = ZonedDateTime.now();
		final ZonedDateTime from = to.minusHours(1);
		
		when(logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetweenAndLoggerContainsIgnoreCase(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entry)));
		
		final Page<Logs> result = commonDBService.getLogEntries(0, 10, Direction.ASC, "entryDate", CoreSystem.SERVICEREGISTRY, null, from, to, "log");
		
		verify(logsRepository, never()).findAllBySystemAndLogLevelInAndEntryDateBetween(any(CoreSystem.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class), any(Pageable.class));
		verify(logsRepository, times(1)).findAllBySystemAndLogLevelInAndEntryDateBetweenAndLoggerContainsIgnoreCase(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), eq("log"), any(Pageable.class));
		
		Assert.assertEquals(1, result.getContent().size());
		Assert.assertEquals("log", result.getContent().get(0).getLogId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogEntriesResponseOk() {
		final Logs entry = new Logs("log", ZonedDateTime.now(), "logger", CoreSystem.SERVICEREGISTRY, LogLevel.INFO, "test", null);
		final ZonedDateTime to = ZonedDateTime.now();
		final ZonedDateTime from = to.minusHours(1);
		
		when(logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetween(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entry)));
		
		final LogEntryListResponseDTO result = commonDBService.getLogEntriesResponse(0, 10, Direction.ASC, "entryDate", CoreSystem.SERVICEREGISTRY, null, from, to, null);
		
		verify(logsRepository, times(1)).findAllBySystemAndLogLevelInAndEntryDateBetween(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), any(Pageable.class));
		
		Assert.assertEquals(1, result.getCount());
		Assert.assertEquals("log", result.getData().get(0).getLogId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetLogEntriesResponseOkWithLogger() {
		final Logs entry = new Logs("log", ZonedDateTime.now(), "logger", CoreSystem.SERVICEREGISTRY, LogLevel.INFO, "test", null);
		final ZonedDateTime to = ZonedDateTime.now();
		final ZonedDateTime from = to.minusHours(1);
		
		when(logsRepository.findAllBySystemAndLogLevelInAndEntryDateBetweenAndLoggerContainsIgnoreCase(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entry)));
		
		final LogEntryListResponseDTO result = commonDBService.getLogEntriesResponse(0, 10, Direction.ASC, "entryDate", CoreSystem.SERVICEREGISTRY, null, from, to, "log");
		
		verify(logsRepository, never()).findAllBySystemAndLogLevelInAndEntryDateBetween(any(CoreSystem.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class), any(Pageable.class));
		verify(logsRepository, times(1)).findAllBySystemAndLogLevelInAndEntryDateBetweenAndLoggerContainsIgnoreCase(eq(CoreSystem.SERVICEREGISTRY), anyList(), eq(from), eq(to), eq("log"), any(Pageable.class));
		
		Assert.assertEquals(1, result.getCount());
		Assert.assertEquals("log", result.getData().get(0).getLogId());
	}
}