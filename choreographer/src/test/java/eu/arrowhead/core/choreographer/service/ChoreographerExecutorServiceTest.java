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

package eu.arrowhead.core.choreographer.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.internal.ChoreographerExecutorListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressDetector;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;

@RunWith(SpringRunner.class)
public class ChoreographerExecutorServiceTest {

	//=================================================================================================
    // members
	
	@InjectMocks
	private ChoreographerExecutorService executorService;
	
	@Mock
	private ChoreographerExecutorDBService executorDBService;
	
	@Mock
	private CommonNamePartVerifier cnVerifier;
	
	@Mock
	private NetworkAddressDetector networkAddressDetector;
	
	@Mock
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Mock
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Mock
	private ChoreographerDriver driver;
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddExecutorSystem() {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(new SystemRequestDTO("test-system", "test-address", 5000, "authenticationInfo", null));
		request.setBaseUri("test-uri");
		request.setServiceDefinitionName("test-service");
		request.setMinVersion(1);
		request.setMaxVersion(10);
		final String origin = "origin";
		
		final SystemResponseDTO response = new SystemResponseDTO();
		response.setId(5);
		response.setSystemName(request.getSystem().getSystemName());
		response.setAddress(request.getSystem().getAddress());
		response.setPort(request.getSystem().getPort());
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(64);
		
		ReflectionTestUtils.setField(executorService, "useStrictServiceDefinitionVerifier", true);
		when(networkAddressPreProcessor.normalize(anyString())).thenReturn(request.getSystem().getAddress());
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		when(driver.registerSystem(any())).thenReturn(response);
		when(executorDBService.createExecutorResponse(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(returnValue);
		
		final ChoreographerExecutorResponseDTO result = executorService.addExecutorSystem(request, origin);
		
		assertEquals(returnValue.getId(), result.getId());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(request.getSystem().getAddress()));
		verify(networkAddressVerifier, times(1)).verify(eq(request.getSystem().getAddress()));
		verify(cnVerifier, times(1)).isValid(eq(request.getServiceDefinitionName()));
		verify(driver, times(1)).registerSystem(eq(request.getSystem()));
		verify(executorDBService, times(1)).createExecutorResponse(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()),
																   eq(request.getBaseUri()), eq(request.getServiceDefinitionName()), eq(request.getMinVersion()), eq(request.getMaxVersion()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterExecutorSystem() {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(new SystemRequestDTO("test-system", "test-address", 5000, "authenticationInfo", null));
		request.setBaseUri("test-uri");
		request.setServiceDefinitionName("test-service");
		request.setMinVersion(1);
		request.setMaxVersion(10);
		final String origin = "origin";
		final HttpServletRequest mockedHttpServletReq = Mockito.mock(HttpServletRequest.class);
		
		final SystemResponseDTO response = new SystemResponseDTO();
		response.setId(5);
		response.setSystemName(request.getSystem().getSystemName());
		response.setAddress(request.getSystem().getAddress());
		response.setPort(request.getSystem().getPort());
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(64);
		
		ReflectionTestUtils.setField(executorService, "useStrictServiceDefinitionVerifier", true);
		when(networkAddressPreProcessor.normalize(anyString())).thenReturn(request.getSystem().getAddress());
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		when(driver.queryServiceRegistryBySystem(anyString(), anyString(), anyInt())).thenReturn(response);
		when(executorDBService.createExecutorResponse(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(returnValue);
		
		final ChoreographerExecutorResponseDTO result = executorService.registerExecutorSystem(request, origin, mockedHttpServletReq);
		
		assertEquals(returnValue.getId(), result.getId());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(request.getSystem().getAddress()));
		verify(networkAddressVerifier, times(1)).verify(eq(request.getSystem().getAddress()));
		verify(networkAddressDetector, never()).detect(any());
		verify(cnVerifier, times(1)).isValid(eq(request.getServiceDefinitionName()));
		verify(driver, times(1)).queryServiceRegistryBySystem(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()));
		verify(driver, never()).registerSystem(any());
		verify(executorDBService, times(1)).createExecutorResponse(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()),
																   eq(request.getBaseUri()), eq(request.getServiceDefinitionName()), eq(request.getMinVersion()), eq(request.getMaxVersion()));
		verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterExecutorSystem_WithNetworkAddressDetection() {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(new SystemRequestDTO("test-system", "", 5000, "authenticationInfo", null));
		request.setBaseUri("test-uri");
		request.setServiceDefinitionName("test-service");
		request.setMinVersion(1);
		request.setMaxVersion(10);
		final String origin = "origin";
		final HttpServletRequest mockedHttpServletReq = Mockito.mock(HttpServletRequest.class);
		
		final AddressDetectionResult detectionResult = new AddressDetectionResult();
		detectionResult.setDetectionSuccess(true);
		detectionResult.setDetectedAddress("detected-address");
		final SystemResponseDTO response = new SystemResponseDTO();
		response.setId(5);
		response.setSystemName(request.getSystem().getSystemName());
		response.setAddress(detectionResult.getDetectedAddress());
		response.setPort(request.getSystem().getPort());
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(64);
		
		ReflectionTestUtils.setField(executorService, "useStrictServiceDefinitionVerifier", true);
		when(networkAddressDetector.detect(any())).thenReturn(detectionResult);
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		when(driver.queryServiceRegistryBySystem(anyString(), anyString(), anyInt())).thenReturn(response);
		when(executorDBService.createExecutorResponse(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(returnValue);
		
		final ChoreographerExecutorResponseDTO result = executorService.registerExecutorSystem(request, origin, mockedHttpServletReq);
		
		assertEquals(returnValue.getId(), result.getId());
		
		verify(networkAddressPreProcessor, never()).normalize(anyString());
		verify(networkAddressVerifier, never()).verify(anyString());
		verify(networkAddressDetector, times(1)).detect(eq(mockedHttpServletReq));
		verify(cnVerifier, times(1)).isValid(eq(request.getServiceDefinitionName()));
		verify(driver, times(1)).queryServiceRegistryBySystem(eq(request.getSystem().getSystemName()), eq(detectionResult.getDetectedAddress()), eq(request.getSystem().getPort()));
		verify(driver, never()).registerSystem(any());
		verify(executorDBService, times(1)).createExecutorResponse(eq(request.getSystem().getSystemName()), eq(detectionResult.getDetectedAddress()), eq(request.getSystem().getPort()),
																   eq(request.getBaseUri()), eq(request.getServiceDefinitionName()), eq(request.getMinVersion()), eq(request.getMaxVersion()));
		verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterExecutorSystem_WithSystemRegistration() {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(new SystemRequestDTO("test-system", "test-address", 5000, "authenticationInfo", null));
		request.setBaseUri("test-uri");
		request.setServiceDefinitionName("test-service");
		request.setMinVersion(1);
		request.setMaxVersion(10);
		final String origin = "origin";
		final HttpServletRequest mockedHttpServletReq = Mockito.mock(HttpServletRequest.class);
		
		final SystemResponseDTO response = new SystemResponseDTO();
		response.setId(5);
		response.setSystemName(request.getSystem().getSystemName());
		response.setAddress(request.getSystem().getAddress());
		response.setPort(request.getSystem().getPort());
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(64);
		
		ReflectionTestUtils.setField(executorService, "useStrictServiceDefinitionVerifier", true);
		when(networkAddressPreProcessor.normalize(anyString())).thenReturn(request.getSystem().getAddress());
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		when(driver.queryServiceRegistryBySystem(anyString(), anyString(), anyInt())).thenThrow(new InvalidParameterException("test"));
		when(driver.registerSystem(any())).thenReturn(response);
		when(executorDBService.createExecutorResponse(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(returnValue);
		
		final ChoreographerExecutorResponseDTO result = executorService.registerExecutorSystem(request, origin, mockedHttpServletReq);
		
		assertEquals(returnValue.getId(), result.getId());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(request.getSystem().getAddress()));
		verify(networkAddressVerifier, times(1)).verify(eq(request.getSystem().getAddress()));
		verify(networkAddressDetector, never()).detect(any());
		verify(cnVerifier, times(1)).isValid(eq(request.getServiceDefinitionName()));
		verify(driver, times(1)).queryServiceRegistryBySystem(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()));
		verify(driver, times(1)).registerSystem(eq(request.getSystem()));
		verify(executorDBService, times(1)).createExecutorResponse(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()),
																   eq(request.getBaseUri()), eq(request.getServiceDefinitionName()), eq(request.getMinVersion()), eq(request.getMaxVersion()));
		verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testRegisterExecutorSystem_DatabaseLayerException() {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(new SystemRequestDTO("test-system", "test-address", 5000, "authenticationInfo", null));
		request.setBaseUri("test-uri");
		request.setServiceDefinitionName("test-service");
		request.setMinVersion(1);
		request.setMaxVersion(10);
		final String origin = "origin";
		final HttpServletRequest mockedHttpServletReq = Mockito.mock(HttpServletRequest.class);
		
		final SystemResponseDTO response = new SystemResponseDTO();
		response.setId(5);
		response.setSystemName(request.getSystem().getSystemName());
		response.setAddress(request.getSystem().getAddress());
		response.setPort(request.getSystem().getPort());
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(64);
		
		ReflectionTestUtils.setField(executorService, "useStrictServiceDefinitionVerifier", true);
		when(networkAddressPreProcessor.normalize(anyString())).thenReturn(request.getSystem().getAddress());
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		when(driver.queryServiceRegistryBySystem(anyString(), anyString(), anyInt())).thenThrow(new InvalidParameterException("test"));
		when(driver.registerSystem(any())).thenReturn(response);
		when(executorDBService.createExecutorResponse(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyInt())).thenThrow(new ArrowheadException("test"));
		
		try {
			executorService.registerExecutorSystem(request, origin, mockedHttpServletReq);
			
		} catch (final ArrowheadException ex) {
			verify(networkAddressPreProcessor, times(1)).normalize(eq(request.getSystem().getAddress()));
			verify(networkAddressVerifier, times(1)).verify(eq(request.getSystem().getAddress()));
			verify(networkAddressDetector, never()).detect(any());
			verify(cnVerifier, times(1)).isValid(eq(request.getServiceDefinitionName()));
			verify(driver, times(1)).queryServiceRegistryBySystem(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()));
			verify(driver, times(1)).registerSystem(eq(request.getSystem()));
			verify(executorDBService, times(1)).createExecutorResponse(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()),
					eq(request.getBaseUri()), eq(request.getServiceDefinitionName()), eq(request.getMinVersion()), eq(request.getMaxVersion()));
			verify(driver, times(1)).unregisterSystem(eq(request.getSystem().getSystemName()), eq(request.getSystem().getAddress()), eq(request.getSystem().getPort()));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutors() {
		final Integer page = 5;
		final Integer size = 20;
		final String direction = "ASC";
		final String sortField = "id";
		final String origin = "origin";
		
		final ChoreographerExecutorResponseDTO returnItem = new ChoreographerExecutorResponseDTO();
		returnItem.setId(23);
		final ChoreographerExecutorListResponseDTO returnValue = new ChoreographerExecutorListResponseDTO(List.of(returnItem), 1);
		
		when(executorDBService.getExecutorsResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(returnValue);
		
		final ChoreographerExecutorListResponseDTO result = executorService.getExecutors(page, size, direction, sortField, origin);
		
		assertEquals(returnValue.getData().size(), result.getData().size());
		assertEquals(returnItem.getId(), result.getData().get(0).getId());
		
		verify(executorDBService, times(1)).getExecutorsResponse(eq(page), eq(size), eq(Direction.valueOf(direction)), eq(sortField));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutors_WithoutParameters() {
		final Integer page = null;
		final Integer size = null;
		final String direction = "ASC";
		final String sortField = null;
		final String origin = "origin";
		
		final ChoreographerExecutorResponseDTO returnItem = new ChoreographerExecutorResponseDTO();
		returnItem.setId(23);
		final ChoreographerExecutorListResponseDTO returnValue = new ChoreographerExecutorListResponseDTO(List.of(returnItem), 1);
		
		when(executorDBService.getExecutorsResponse(anyInt(), anyInt(), any(), any())).thenReturn(returnValue);
		
		final ChoreographerExecutorListResponseDTO result = executorService.getExecutors(page, size, direction, sortField, origin);
		
		assertEquals(returnValue.getData().size(), result.getData().size());
		assertEquals(returnItem.getId(), result.getData().get(0).getId());
		
		verify(executorDBService, times(1)).getExecutorsResponse(eq(0), eq(Integer.MAX_VALUE), eq(Direction.valueOf(direction)), nullable(String.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutorById() {
		final long id = 5;
		final String origin = "origin";
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(id);
		
		when(executorDBService.getExecutorOptionalByIdResponse(anyLong())).thenReturn(Optional.of(returnValue));
		
		final ChoreographerExecutorResponseDTO result = executorService.getExecutorById(id, origin);
		
		assertEquals(returnValue.getId(), result.getId());
		verify(executorDBService, times(1)).getExecutorOptionalByIdResponse(eq(id));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testGetExecutorById_NotExists() {
		final long id = 5;
		final String origin = "origin";
		
		when(executorDBService.getExecutorOptionalByIdResponse(anyLong())).thenReturn(Optional.empty());
		
		try {
			executorService.getExecutorById(id, origin);
			
		} catch (final BadPayloadException ex) {
			verify(executorDBService, times(1)).getExecutorOptionalByIdResponse(eq(id));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveExecutorSystem() {
		final long id = 5;
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(id);
		executor.setName("name");
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalById(anyLong())).thenReturn(Optional.of(executor));
		when(executorDBService.isExecutorActiveById(anyLong())).thenReturn(false);
		when(executorDBService.lockExecutorById(anyLong())).thenReturn(true);
		
		executorService.removeExecutorSystem(id, origin);
		
		verify(executorDBService, times(1)).getExecutorOptionalById(eq(id));
		verify(executorDBService, times(1)).isExecutorActiveById(eq(id));
		verify(executorDBService, times(1)).lockExecutorById(eq(id));
		verify(driver, times(1)).unregisterSystem(eq(executor.getName()), eq(executor.getAddress()), eq(executor.getPort()));
		verify(executorDBService, times(1)).deleteExecutorById(eq(id));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveExecutorSystem_NotExists() {
		final long id = 5;
		final String origin = "origin";
		
		when(executorDBService.getExecutorOptionalById(anyLong())).thenReturn(Optional.empty());
		
		executorService.removeExecutorSystem(id, origin);
		
		verify(executorDBService, times(1)).getExecutorOptionalById(eq(id));
		verify(executorDBService, never()).isExecutorActiveById(anyLong());
		verify(executorDBService, never()).lockExecutorById(anyLong());
		verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
		verify(executorDBService, never()).deleteExecutorById(anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testRemoveExecutorSystem_ExecutorIsActive() {
		final long id = 5;
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(id);
		executor.setName("name");
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalById(anyLong())).thenReturn(Optional.of(executor));
		when(executorDBService.isExecutorActiveById(anyLong())).thenReturn(true);
		
		try {
			executorService.removeExecutorSystem(id, origin);
			
		} catch (final BadPayloadException ex) {
			verify(executorDBService, times(1)).getExecutorOptionalById(eq(id));
			verify(executorDBService, times(1)).isExecutorActiveById(eq(id));
			verify(executorDBService, never()).lockExecutorById(anyLong());
			verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
			verify(executorDBService, never()).deleteExecutorById(anyLong());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testRemoveExecutorSystem_LockingFailure() {
		final long id = 5;
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(id);
		executor.setName("name");
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalById(anyLong())).thenReturn(Optional.of(executor));
		when(executorDBService.isExecutorActiveById(anyLong())).thenReturn(false);
		when(executorDBService.lockExecutorById(anyLong())).thenReturn(false);
		
		try {
			executorService.removeExecutorSystem(id, origin);
			
		} catch (final ArrowheadException ex) {
			verify(executorDBService, times(1)).getExecutorOptionalById(eq(id));
			verify(executorDBService, times(1)).isExecutorActiveById(eq(id));
			verify(executorDBService, times(1)).lockExecutorById(eq(id));
			verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
			verify(executorDBService, never()).deleteExecutorById(anyLong());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterExecutorSystem() {
		final String name = "name";
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(5);
		executor.setName(name);
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalByName(anyString())).thenReturn(Optional.of(executor));
		when(executorDBService.isExecutorActiveById(anyLong())).thenReturn(false);
		when(executorDBService.lockExecutorById(anyLong())).thenReturn(true);
		
		executorService.unregisterExecutorSystem(name, origin);
		
		verify(executorDBService, times(1)).getExecutorOptionalByName(eq(name));
		verify(executorDBService, times(1)).isExecutorActiveById(eq(executor.getId()));
		verify(executorDBService, times(1)).lockExecutorById(eq(executor.getId()));
		verify(driver, times(1)).unregisterSystem(eq(executor.getName()), eq(executor.getAddress()), eq(executor.getPort()));
		verify(executorDBService, times(1)).deleteExecutorById(eq(executor.getId()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterExecutorSystem_NotExists() {
		final String name = "name";
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(5);
		executor.setName(name);
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalByName(anyString())).thenReturn(Optional.empty());
		
		executorService.unregisterExecutorSystem(name, origin);
		
		verify(executorDBService, times(1)).getExecutorOptionalByName(eq(name));
		verify(executorDBService, never()).isExecutorActiveById(anyLong());
		verify(executorDBService, never()).lockExecutorById(anyLong());
		verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
		verify(executorDBService, never()).deleteExecutorById(anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testUnsregisterExecutorSystem_ExecutorIsActive() {
		final String name = "name";
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(5);
		executor.setName(name);
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalByName(anyString())).thenReturn(Optional.of(executor));
		when(executorDBService.isExecutorActiveById(anyLong())).thenReturn(true);
		
		try {
			executorService.unregisterExecutorSystem(name, origin);
			
		} catch (final BadPayloadException ex) {
			verify(executorDBService, times(1)).getExecutorOptionalByName(eq(name));
			verify(executorDBService, times(1)).isExecutorActiveById(eq(executor.getId()));
			verify(executorDBService, never()).lockExecutorById(anyLong());
			verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
			verify(executorDBService, never()).deleteExecutorById(anyLong());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testUnsregisterExecutorSystem_LockingFailure() {
		final String name = "name";
		final String origin = "origin";
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(5);
		executor.setName(name);
		executor.setAddress("address");
		executor.setPort(5000);
		
		when(executorDBService.getExecutorOptionalByName(anyString())).thenReturn(Optional.of(executor));
		when(executorDBService.isExecutorActiveById(anyLong())).thenReturn(false);
		when(executorDBService.lockExecutorById(anyLong())).thenReturn(false);
		
		try {
			executorService.unregisterExecutorSystem(name, origin);
			
		} catch (final ArrowheadException ex) {
			verify(executorDBService, times(1)).getExecutorOptionalByName(eq(name));
			verify(executorDBService, times(1)).isExecutorActiveById(eq(executor.getId()));
			verify(executorDBService, times(1)).lockExecutorById(eq(executor.getId()));
			verify(driver, never()).unregisterSystem(anyString(), anyString(), anyInt());
			verify(executorDBService, never()).deleteExecutorById(anyLong());
			throw ex;
		}		
	}
}
