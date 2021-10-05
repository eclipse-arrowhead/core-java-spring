/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.choreographer.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerExecutorServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionStepRepository;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@RunWith(SpringRunner.class)
public class ChoreographerExecutorDBServiceTest {

	//=================================================================================================
    // members
	
	@InjectMocks
	private ChoreographerExecutorDBService dbService;
	
	@Mock
    private ChoreographerExecutorRepository executorRepository;
	
	@Mock
	private ChoreographerExecutorServiceDefinitionRepository executorServiceDefinitionRepository;
	
	@Mock
	private ChoreographerSessionStepRepository sessionStepRepository;
	
	@Spy
	private CommonNamePartVerifier cnVerifier;
	
	//=================================================================================================
    // methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateExecutoreResponseTest() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "serviceDefinitionName";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor(systemName, address, port, baseUri);
		final ChoreographerExecutorServiceDefinition executorServiceDefinition = new ChoreographerExecutorServiceDefinition(executor, serviceDefinitionName, minVersion, maxVersion);
		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenReturn(Optional.of(executor));
		when(executorServiceDefinitionRepository.findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName))).thenReturn(Optional.empty());
		when(executorServiceDefinitionRepository.saveAndFlush(any(ChoreographerExecutorServiceDefinition.class))).thenReturn(executorServiceDefinition);
		when(executorServiceDefinitionRepository.findAllByExecutor(eq(executor))).thenReturn(List.of(executorServiceDefinition));
		
		final ChoreographerExecutorResponseDTO result = dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);
		
		assertEquals(systemName, result.getName());
		assertEquals(address, result.getAddress());
		assertEquals(port, result.getPort());
		assertEquals(baseUri, result.getBaseUri());
		assertEquals(serviceDefinitionName, result.getServiceDefinitions().get(0).getServiceDefinitionName());
		assertEquals(minVersion, result.getServiceDefinitions().get(0).getMinVersion());
		assertEquals(maxVersion, result.getServiceDefinitions().get(0).getMaxVersion());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_ExecutorNotExists() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service-definition-name";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor(systemName, address, port, baseUri);
		final ChoreographerExecutorServiceDefinition executorServiceDefinition = new ChoreographerExecutorServiceDefinition(executor, serviceDefinitionName, minVersion, maxVersion);
		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerExecutor> executorCaptor = ArgumentCaptor.forClass(ChoreographerExecutor.class);
		when(executorRepository.saveAndFlush(executorCaptor.capture())).thenReturn(executor);
		when(executorServiceDefinitionRepository.findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName))).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerExecutorServiceDefinition> executorServDefCaptor = ArgumentCaptor.forClass(ChoreographerExecutorServiceDefinition.class);
		when(executorServiceDefinitionRepository.saveAndFlush(executorServDefCaptor.capture())).thenReturn(executorServiceDefinition);
		
		final ChoreographerExecutorResponseDTO result = dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);
		
		final ChoreographerExecutor capturedExecutor = executorCaptor.getValue();
		assertEquals(systemName, capturedExecutor.getName());
		assertEquals(address, capturedExecutor.getAddress());
		assertEquals(port, capturedExecutor.getPort());
		assertEquals(baseUri, capturedExecutor.getBaseUri());
		
		final ChoreographerExecutorServiceDefinition captureExecutorServDef = executorServDefCaptor.getValue();
		assertEquals(systemName, captureExecutorServDef.getExecutor().getName());
		assertEquals(serviceDefinitionName, captureExecutorServDef.getServiceDefinition());
		assertEquals(minVersion, captureExecutorServDef.getMinVersion().intValue());
		assertEquals(maxVersion, captureExecutorServDef.getMaxVersion().intValue());
		
		assertEquals(systemName, result.getName());
		assertEquals(address, result.getAddress());
		assertEquals(port, result.getPort());
		assertEquals(baseUri, result.getBaseUri());
		
		verify(executorRepository, times(1)).findByName(eq(systemName));
		verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
		verify(executorRepository, times(1)).saveAndFlush(any(ChoreographerExecutor.class));
		verify(cnVerifier, times(1)).isValid(eq(serviceDefinitionName));
		verify(executorServiceDefinitionRepository, times(1)).findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName));
		verify(executorServiceDefinitionRepository, times(1)).saveAndFlush(any(ChoreographerExecutorServiceDefinition.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_ExecutorExistsButServiceDefNot() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service-definition-name";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor(systemName, address, port, baseUri);
		final ChoreographerExecutorServiceDefinition executorServiceDefinition = new ChoreographerExecutorServiceDefinition(executor, serviceDefinitionName, minVersion, maxVersion);
		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenReturn(Optional.of(executor));		
		when(executorServiceDefinitionRepository.findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName))).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerExecutorServiceDefinition> executorServDefCaptor = ArgumentCaptor.forClass(ChoreographerExecutorServiceDefinition.class);
		when(executorServiceDefinitionRepository.saveAndFlush(executorServDefCaptor.capture())).thenReturn(executorServiceDefinition);
		
		final ChoreographerExecutorResponseDTO result = dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);
		
		final ChoreographerExecutorServiceDefinition captureExecutorServDef = executorServDefCaptor.getValue();
		assertEquals(systemName, captureExecutorServDef.getExecutor().getName());
		assertEquals(serviceDefinitionName, captureExecutorServDef.getServiceDefinition());
		assertEquals(minVersion, captureExecutorServDef.getMinVersion().intValue());
		assertEquals(maxVersion, captureExecutorServDef.getMaxVersion().intValue());
		
		assertEquals(systemName, result.getName());
		assertEquals(address, result.getAddress());
		assertEquals(port, result.getPort());
		assertEquals(baseUri, result.getBaseUri());
		
		verify(executorRepository, times(1)).findByName(eq(systemName));
		verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
		verify(executorRepository, never()).saveAndFlush(any());
		verify(cnVerifier, times(1)).isValid(eq(serviceDefinitionName));
		verify(executorServiceDefinitionRepository, times(1)).findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName));
		verify(executorServiceDefinitionRepository, times(1)).saveAndFlush(any(ChoreographerExecutorServiceDefinition.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_ExecutorExistsAndServiceDefToo() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service-definition-name";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor(systemName, address, port, baseUri);
		final ChoreographerExecutorServiceDefinition executorServiceDefinition = new ChoreographerExecutorServiceDefinition(executor, serviceDefinitionName, minVersion, maxVersion);
		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenReturn(Optional.of(executor));		
		when(executorServiceDefinitionRepository.findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName))).thenReturn(Optional.of(executorServiceDefinition));
		
		final ChoreographerExecutorResponseDTO result = dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);
		
		assertEquals(systemName, result.getName());
		assertEquals(address, result.getAddress());
		assertEquals(port, result.getPort());
		assertEquals(baseUri, result.getBaseUri());
		
		verify(executorRepository, times(1)).findByName(eq(systemName));
		verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
		verify(executorRepository, never()).saveAndFlush(any());
		verify(cnVerifier, times(1)).isValid(eq(serviceDefinitionName));
		verify(executorServiceDefinitionRepository, times(1)).findByExecutorAndServiceDefinition(eq(executor), eq(serviceDefinitionName));
		verify(executorServiceDefinitionRepository, never()).saveAndFlush(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_ExecutorNameAlreadyExists() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service-definition-name";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		when(executorRepository.findByName(eq(systemName))).thenReturn(Optional.of(new ChoreographerExecutor()));
		
		try {
			dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);			
		} catch (final InvalidParameterException ex) {
			verify(executorRepository, times(1)).findByName(eq(systemName));
			verify(executorRepository, never()).findByAddressAndPortAndBaseUri(anyString(), anyInt(), anyString());
			verify(executorRepository, never()).saveAndFlush(any());
			verify(cnVerifier, never()).isValid(any());
			verify(executorServiceDefinitionRepository, never()).findByExecutorAndServiceDefinition(any(), anyString());
			verify(executorServiceDefinitionRepository, never()).saveAndFlush(any());
			
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_ServiceNameInvalid() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service_definition_name";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor(systemName, address, port, baseUri);		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenReturn(Optional.of(executor));		
		
		try {
			dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);			
		} catch (final InvalidParameterException ex) {
			verify(executorRepository, times(1)).findByName(eq(systemName));
			verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
			verify(executorRepository, never()).saveAndFlush(any());
			verify(cnVerifier, times(1)).isValid(eq(serviceDefinitionName));
			verify(executorServiceDefinitionRepository, never()).findByExecutorAndServiceDefinition(any(), anyString());
			verify(executorServiceDefinitionRepository, never()).saveAndFlush(any());
			
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_MinVersionIsHigherThanMaxVersion() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service-definition-name";
		final int minVersion = 11;
		final int maxVersion = 10;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor(systemName, address, port, baseUri);		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenReturn(Optional.of(executor));		
		
		try {
			dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);			
		} catch (final InvalidParameterException ex) {
			verify(executorRepository, times(1)).findByName(eq(systemName));
			verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
			verify(executorRepository, never()).saveAndFlush(any());
			verify(cnVerifier, times(1)).isValid(eq(serviceDefinitionName));
			verify(executorServiceDefinitionRepository, never()).findByExecutorAndServiceDefinition(any(), anyString());
			verify(executorServiceDefinitionRepository, never()).saveAndFlush(any());
			
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateExecutorWithoutSystemNameAndAddressValidation_DatabaseException() {
		final String systemName = "systemName";
		final String address = "address";
		final int port = 1000;
		final String baseUri = "baseUri";
		final String serviceDefinitionName = "service-definition-name";
		final int minVersion = 1;
		final int maxVersion = 10;
		
		when(executorRepository.findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri))).thenThrow(new HibernateException("test"));		
		
		try {
			dbService.createExecutorResponse(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);			
		} catch (final InvalidParameterException ex) {
			verify(executorRepository, times(1)).findByName(eq(systemName));
			verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
			verify(executorRepository, never()).saveAndFlush(any());
			verify(cnVerifier, never()).isValid(anyString());
			verify(executorServiceDefinitionRepository, never()).findByExecutorAndServiceDefinition(any(), anyString());
			verify(executorServiceDefinitionRepository, never()).saveAndFlush(any());
			
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutorsResponse() {
		final int page = 0;
		final int size = 100;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		
		final ArgumentCaptor<PageRequest> captorPage = ArgumentCaptor.forClass(PageRequest.class);
		when(executorRepository.findAll(captorPage.capture())).thenReturn(new PageImpl<ChoreographerExecutor>(List.of(executor)));
		final ArgumentCaptor<ChoreographerExecutor> captorExecutor = ArgumentCaptor.forClass(ChoreographerExecutor.class);
		when(executorServiceDefinitionRepository.findAllByExecutor(captorExecutor.capture())).thenReturn(List.of());
		
		dbService.getExecutorsResponse(page, size, direction, sortField);
		
		final PageRequest capturedPage = captorPage.getValue();
		assertEquals(page, capturedPage.getPageNumber());
		assertEquals(size, capturedPage.getPageSize());
		assertNotNull(capturedPage.getSort().getOrderFor(sortField));
		assertEquals(direction, capturedPage.getSort().getOrderFor(sortField).getDirection());
		
		final ChoreographerExecutor capturedExecutor = captorExecutor.getValue();
		assertEquals(executor.getId(), capturedExecutor.getId());
		verify(executorServiceDefinitionRepository, times(1)).findAllByExecutor(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutors() {
		final int page = 0;
		final int size = 100;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		
		final ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
		when(executorRepository.findAll(captor.capture())).thenReturn(new PageImpl<ChoreographerExecutor>(List.of(executor)));
		
		final Page<ChoreographerExecutor> result = dbService.getExecutors(page, size, direction, sortField);
		
		final PageRequest captured = captor.getValue();
		assertEquals(page, captured.getPageNumber());
		assertEquals(size, captured.getPageSize());
		assertNotNull(captured.getSort().getOrderFor(sortField));
		assertEquals(direction, captured.getSort().getOrderFor(sortField).getDirection());
		
		assertEquals(executor.getId(), result.getContent().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutors_WithDefaults() {
		final int page = -1;
		final int size = -1;
		final Direction direction = null;
		final String sortField = null;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		
		final ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
		when(executorRepository.findAll(captor.capture())).thenReturn(new PageImpl<ChoreographerExecutor>(List.of(executor)));
		
		final Page<ChoreographerExecutor> result = dbService.getExecutors(page, size, direction, sortField);
		
		final PageRequest captured = captor.getValue();
		assertEquals(0, captured.getPageNumber());
		assertEquals(Integer.MAX_VALUE, captured.getPageSize());
		assertNotNull(captured.getSort().getOrderFor("id"));
		assertEquals(Direction.ASC, captured.getSort().getOrderFor("id").getDirection());
		
		assertEquals(executor.getId(), result.getContent().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetExecutors_InvalidSortField() {
		final int page = 0;
		final int size = 100;
		final Direction direction = Direction.ASC;
		final String sortField = "invalid";
		
		dbService.getExecutors(page, size, direction, sortField);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetExecutors_DatabaseException() {
		final int page = -1;
		final int size = -1;
		final Direction direction = null;
		final String sortField = null;
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		
		final ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
		when(executorRepository.findAll(captor.capture())).thenThrow(new HibernateException("test"));
		
		dbService.getExecutors(page, size, direction, sortField);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getExecutorOptionalByIdResponse_Present() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		
		when(executorRepository.findById(eq(executor.getId()))).thenReturn(Optional.of(executor));
		when(executorServiceDefinitionRepository.findAllByExecutor(eq(executor))).thenReturn(List.of());
		
		final Optional<ChoreographerExecutorResponseDTO> result = dbService.getExecutorOptionalByIdResponse(executor.getId());
		
		assertTrue(result.isPresent());
		assertEquals(executor.getId(), result.get().getId());
		verify(executorServiceDefinitionRepository, times(1)).findAllByExecutor(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getExecutorOptionalByIdResponse_Empty() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		
		when(executorRepository.findById(eq(executor.getId()))).thenReturn(Optional.empty());
		
		final Optional<ChoreographerExecutorResponseDTO> result = dbService.getExecutorOptionalByIdResponse(executor.getId());
		
		assertTrue(result.isEmpty());
		verify(executorServiceDefinitionRepository, never()).findAllByExecutor(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void getExecutorOptionalByIdResponse_DatabaseException() {
		when(executorRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getExecutorOptionalByIdResponse(1L);			
		} catch (final ArrowheadException ex) {
			verify(executorServiceDefinitionRepository, never()).findAllByExecutor(any());
			throw ex;
		}	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutorOptionalById() {
		dbService.getExecutorOptionalById(1L);
		verify(executorRepository, times(1)).findById(eq(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetExecutorOptionalById_DatabaseException() {
		when(executorRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		dbService.getExecutorOptionalById(1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getExecutorOptionalByAddressAndPortAndBaseUri() {
		final String address = "address";
		final int port = 10000;
		final String baseUri = "baseUri";
		
		dbService.getExecutorOptionalByAddressAndPortAndBaseUri(address, port, baseUri);
		verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(address), eq(port), eq(baseUri));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void getExecutorOptionalByAddressAndPortAndBaseUri_DatabaseException() {
		final String address = "address";
		final int port = 10000;
		final String baseUri = "baseUri";
		when(executorRepository.findByAddressAndPortAndBaseUri(anyString(), anyInt(), anyString())).thenThrow(new HibernateException("test"));
		
		dbService.getExecutorOptionalByAddressAndPortAndBaseUri(address, port, baseUri);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getExecutorOptionalByName() {
		dbService.getExecutorOptionalByName("name");
		verify(executorRepository, times(1)).findByName(eq("name"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void getExecutorOptionalByName_DatabaseException() {
		when(executorRepository.findByName(anyString())).thenThrow(new HibernateException("test"));
		dbService.getExecutorOptionalByName("name");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutorsByServiceDefinitionAndVersion() {		
		final ChoreographerExecutor executor1 = new ChoreographerExecutor();
		executor1.setId(1L);		
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(2L);
		final ChoreographerExecutor executor3 = new ChoreographerExecutor();
		executor3.setId(3L);
		
		final String service = "service";
		final ChoreographerExecutorServiceDefinition executorServDef1 = new ChoreographerExecutorServiceDefinition(executor1, service, 5, 11);
		final ChoreographerExecutorServiceDefinition executorServDef2 = new ChoreographerExecutorServiceDefinition(executor2, service, 4, 10);
		final ChoreographerExecutorServiceDefinition executorServDef3 = new ChoreographerExecutorServiceDefinition(executor2, service, 3, 8);
		
		when(executorServiceDefinitionRepository.findAllByServiceDefinition(eq(service))).thenReturn(List.of(executorServDef1, executorServDef2, executorServDef3));
		final List<ChoreographerExecutor> result = dbService.getExecutorsByServiceDefinitionAndVersion(service, 4, 10);
		
		assertTrue(result.size() == 1);
		assertEquals(executor2.getId(), result.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetExecutorsByServiceDefinitionAndVersion_DatabaseException() {
		when(executorServiceDefinitionRepository.findAllByServiceDefinition(anyString())).thenThrow(new HibernateException("test"));
		dbService.getExecutorsByServiceDefinitionAndVersion("service", 4, 10);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteExecutorById() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(eq(executor.getId()))).thenReturn(Optional.of(executor));
		when(sessionStepRepository.existsByExecutorAndStatusIn(any(), any())).thenReturn(false);
		
		dbService.deleteExecutorById(executor.getId());
		
		verify(executorRepository, times(1)).findById(executor.getId());
		verify(sessionStepRepository, times(1)).existsByExecutorAndStatusIn(eq(executor), any());
		verify(executorRepository, times(1)).deleteById(executor.getId());
		verify(executorRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDeleteExecutorById_WorkingExecutor() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(eq(executor.getId()))).thenReturn(Optional.of(executor));
		when(sessionStepRepository.existsByExecutorAndStatusIn(any(), any())).thenReturn(true);
		
		try {
			dbService.deleteExecutorById(executor.getId());			
		} catch (final InvalidParameterException ex) {
			verify(executorRepository, times(1)).findById(executor.getId());
			verify(sessionStepRepository, times(1)).existsByExecutorAndStatusIn(eq(executor), any());
			verify(executorRepository, never()).deleteById(anyLong());
			verify(executorRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDeleteExecutorById_DatabaseException() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(eq(executor.getId()))).thenThrow(new HibernateException("test"));
		
		try {
			dbService.deleteExecutorById(executor.getId());			
		} catch (final ArrowheadException ex) {
			verify(executorRepository, times(1)).findById(executor.getId());
			verify(sessionStepRepository, never()).existsByExecutorAndStatusIn(any(), any());
			verify(executorRepository, never()).deleteById(anyLong());
			verify(executorRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteExecutorByAddressAndPortAndBaseUri() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		executor.setAddress("address");
		executor.setPort(1000);
		executor.setBaseUri("baseUri");
		when(executorRepository.findByAddressAndPortAndBaseUri(anyString(), anyInt(), anyString())).thenReturn(Optional.of(executor));
		when(executorRepository.findById(any())).thenReturn(Optional.of(executor));
		when(sessionStepRepository.existsByExecutorAndStatusIn(any(), any())).thenReturn(false);
		
		dbService.deleteExecutorByAddressAndPortAndBaseUri(executor.getAddress(), executor.getPort(), executor.getBaseUri());
		
		verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(executor.getAddress()), eq(executor.getPort()), eq(executor.getBaseUri()));
		verify(executorRepository, times(1)).findById(executor.getId());
		verify(sessionStepRepository, times(1)).existsByExecutorAndStatusIn(eq(executor), any());
		verify(executorRepository, times(1)).deleteById(executor.getId());
		verify(executorRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDeleteExecutorByAddressAndPortAndBaseUri_WorkingExecutor() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		executor.setAddress("address");
		executor.setPort(1000);
		executor.setBaseUri("baseUri");
		when(executorRepository.findByAddressAndPortAndBaseUri(anyString(), anyInt(), anyString())).thenReturn(Optional.of(executor));
		when(executorRepository.findById(any())).thenReturn(Optional.of(executor));
		when(sessionStepRepository.existsByExecutorAndStatusIn(any(), any())).thenReturn(true);
		
		try {
			dbService.deleteExecutorByAddressAndPortAndBaseUri(executor.getAddress(), executor.getPort(), executor.getBaseUri());	
			
		} catch (final InvalidParameterException ex) {
			verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(executor.getAddress()), eq(executor.getPort()), eq(executor.getBaseUri()));
			verify(executorRepository, times(1)).findById(eq(executor.getId()));
			verify(sessionStepRepository, times(1)).existsByExecutorAndStatusIn(eq(executor), any());
			verify(executorRepository, never()).deleteById(anyLong());
			verify(executorRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDeleteExecutorByAddressAndPortAndBaseUri_DatabaseException() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		executor.setAddress("address");
		executor.setPort(1000);
		executor.setBaseUri("baseUri");
		when(executorRepository.findByAddressAndPortAndBaseUri(anyString(), anyInt(), anyString())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.deleteExecutorByAddressAndPortAndBaseUri(executor.getAddress(), executor.getPort(), executor.getBaseUri());	
			
		} catch (final ArrowheadException ex) {
			verify(executorRepository, times(1)).findByAddressAndPortAndBaseUri(eq(executor.getAddress()), eq(executor.getPort()), eq(executor.getBaseUri()));
			verify(executorRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).existsByExecutorAndStatusIn(any(), any());
			verify(executorRepository, never()).deleteById(anyLong());
			verify(executorRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testLockExecutorById() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenReturn(Optional.of(executor));
		final ArgumentCaptor<ChoreographerExecutor> captor = ArgumentCaptor.forClass(ChoreographerExecutor.class);
		when(executorRepository.saveAndFlush(captor.capture())).thenReturn(executor);
		
		final boolean result = dbService.lockExecutorById(executor.getId());
		
		verify(executorRepository, times(1)).findById(eq(executor.getId()));
		verify(executorRepository, times(1)).saveAndFlush(eq(executor));
		
		assertTrue(captor.getValue().isLocked());
		assertTrue(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testLockExecutorById_ExecutorNotFound() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenReturn(Optional.empty());
		
		final boolean result = dbService.lockExecutorById(executor.getId());
		
		verify(executorRepository, times(1)).findById(eq(executor.getId()));
		verify(executorRepository, never()).saveAndFlush(any());
		
		assertFalse(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testLockExecutorById_DatabaseException() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.lockExecutorById(executor.getId());		
			
		} catch (final ArrowheadException ex) {
			verify(executorRepository, times(1)).findById(eq(executor.getId()));
			verify(executorRepository, never()).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsExecutorActiveById_True() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenReturn(Optional.of(executor));
		when(sessionStepRepository.existsByExecutorAndStatusIn(any(), any())).thenReturn(true);
		
		final boolean result = dbService.isExecutorActiveById(executor.getId());
		
		verify(executorRepository, times(1)).findById(eq(executor.getId()));
		verify(sessionStepRepository, times(1)).existsByExecutorAndStatusIn(eq(executor), any());
		assertTrue(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsExecutorActiveById_False1() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenReturn(Optional.of(executor));
		when(sessionStepRepository.existsByExecutorAndStatusIn(any(), any())).thenReturn(false);
		
		final boolean result = dbService.isExecutorActiveById(executor.getId());
		
		verify(executorRepository, times(1)).findById(eq(executor.getId()));
		verify(sessionStepRepository, times(1)).existsByExecutorAndStatusIn(eq(executor), any());
		assertFalse(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsExecutorActiveById_False2() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenReturn(Optional.empty());
		
		final boolean result = dbService.isExecutorActiveById(executor.getId());
		
		verify(executorRepository, times(1)).findById(eq(executor.getId()));
		verify(sessionStepRepository, never()).existsByExecutorAndStatusIn(any(), any());
		assertFalse(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testIsExecutorActiveById_DatabaseException() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1L);
		when(executorRepository.findById(any())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.isExecutorActiveById(executor.getId());
			
		} catch (final ArrowheadException ex) {
			verify(executorRepository, times(1)).findById(eq(executor.getId()));
			verify(sessionStepRepository, never()).existsByExecutorAndStatusIn(any(), any());
			throw ex;
		}
	}
}
