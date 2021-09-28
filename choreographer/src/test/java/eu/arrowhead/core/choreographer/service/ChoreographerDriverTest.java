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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryResultDTO;
import eu.arrowhead.common.dto.internal.KeyValuesDTO;
import eu.arrowhead.common.dto.shared.ActiveSessionCloseErrorDTO;
import eu.arrowhead.common.dto.shared.ChoreographerAbortStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecuteStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class ChoreographerDriverTest {

    //=================================================================================================
    //  members
	
    private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_BY_PROXY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    private static final String GATEWAY_CLOSE_SESSIONS_URI_KEY = CoreSystemService.GATEWAY_CLOSE_SESSIONS_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    private static final String GATEKEEPER_MULTI_GSD_URI_KEY = CoreSystemService.GATEKEEPER_MULTI_GLOBAL_SERVICE_DISCOVERY.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;

    @InjectMocks
    private ChoreographerDriver testObject;

    @Mock
    private HttpService httpService;

    @Mock
    private Map<String, Object> arrowheadContext;
    
    @Mock
    private SSLProperties sslProperties;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Test(expected = ArrowheadException.class)
    public void testPullServiceRegistryConfigUriNotFound() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI)).thenReturn(false);
    	
    	try {
    		testObject.pullServiceRegistryConfig();
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry pull-config URI.", ex.getMessage());
    		
    		throw ex;
    	}
    }
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = ArrowheadException.class)
    public void testPullServiceRegistryConfigUriWrongType() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI)).thenReturn(true);
    	when(arrowheadContext.get(CoreCommonConstants.SR_PULL_CONFIG_URI)).thenReturn("invalid");
    	
    	try {
    		testObject.pullServiceRegistryConfig();
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI);
    		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_PULL_CONFIG_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry pull-config URI.", ex.getMessage());
    		
    		throw ex;
    	}
    }
    
    //-------------------------------------------------------------------------------------------------
    @Test
    public void testPullServiceRegistryConfigOk() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI)).thenReturn(true);
    	when(arrowheadContext.get(CoreCommonConstants.SR_PULL_CONFIG_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
    	when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(KeyValuesDTO.class))).thenReturn(new ResponseEntity<>(new KeyValuesDTO(Map.of("key", "value")), HttpStatus.OK));
    	
    	final KeyValuesDTO result = testObject.pullServiceRegistryConfig();
    	
   		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI);
   		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_PULL_CONFIG_URI);
   		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(KeyValuesDTO.class));
    	Assert.assertEquals(1, result.getMap().size());
    }
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryBySystemSystemNameNull() {
		try {
			testObject.queryServiceRegistryBySystem(null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("systemName is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryBySystemSystemNameEmpty() {
		try {
			testObject.queryServiceRegistryBySystem("", null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("systemName is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryBySystemAddressNull() {
		try {
			testObject.queryServiceRegistryBySystem("system", null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryBySystemAddressEmpty() {
		try {
			testObject.queryServiceRegistryBySystem("system", " ", 0);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryBySystemUriNotFound() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)).thenReturn(false);
    	
    	try {
    		testObject.queryServiceRegistryBySystem("system", "localhost", 1234);
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry query by system URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryBySystemUriWrongType() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)).thenReturn(true);
    	when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)).thenReturn("invalid");
    	
    	try {
    		testObject.queryServiceRegistryBySystem("system", "localhost", 1234);
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
    		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry query by system URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test
	public void testQueryServiceRegistryBySystemOk() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)).thenReturn(true);
    	when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
       	when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(SystemResponseDTO.class), any(SystemRequestDTO.class))).thenReturn(new ResponseEntity<>(new SystemResponseDTO(), HttpStatus.OK));
        
		final SystemResponseDTO result = testObject.queryServiceRegistryBySystem("system", "localhost", 1234);
		
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(SystemResponseDTO.class), any(SystemRequestDTO.class));
		Assert.assertNotNull(result);
    }
    
    //-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
    public void testRegisterSystemInputNull() {
		try {
			testObject.registerSystem(null);
		} catch (final Exception ex) {
			Assert.assertEquals("SystemRequestDTO is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
    public void testRegisterSystemUriNotFound() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)).thenReturn(false);
    	
    	try {
    		testObject.registerSystem(new SystemRequestDTO());
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry Register System URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
    public void testRegisterSystemUriWrongType() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)).thenReturn("invalid");
    	
    	try {
    		testObject.registerSystem(new SystemRequestDTO());
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
    		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry Register System URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemOk() {
    	when(arrowheadContext.containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)).thenReturn(true);
    	when(arrowheadContext.get(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
       	when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(SystemResponseDTO.class), any(SystemRequestDTO.class))).thenReturn(new ResponseEntity<>(new SystemResponseDTO(), HttpStatus.OK));
        
		final SystemResponseDTO result = testObject.registerSystem(new SystemRequestDTO());
		
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(SystemResponseDTO.class), any(SystemRequestDTO.class));
		Assert.assertNotNull(result);		
	}
	
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testUnregisterSystemSystemNameNull() {
		try {
			testObject.unregisterSystem(null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("systemName is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testUnregisterSystemSystemNameEmpty() {
		try {
			testObject.unregisterSystem("", null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("systemName is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testUnregisterSystemAddressNull() {
		try {
			testObject.unregisterSystem("system", null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
	public void testUnregisterSystemAddressEmpty() {
		try {
			testObject.unregisterSystem("system", " ", 0);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @Test(expected = ArrowheadException.class)
	public void testUnregisterSystemUriNotFound() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI)).thenReturn(false);
    	
    	try {
    		testObject.unregisterSystem("system", "localhost", 1234);
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI);
    		Assert.assertEquals("Choreographer can't find Service Registry Unregister System URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
    
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
 	public void testUnregisterSystemUriWrongType() {
 		when(arrowheadContext.containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI)).thenReturn(true);
 		when(arrowheadContext.get(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI)).thenReturn("invalid");
     	
     	try {
     		testObject.unregisterSystem("system", "localhost", 1234);
     	} catch (final Exception ex) {
     		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI);
     		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI);
     		Assert.assertEquals("Choreographer can't find Service Registry Unregister System URI.", ex.getMessage());
     		
     		throw ex;
     	}
 	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemOk() {
	   	when(arrowheadContext.containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI)).thenReturn(true);
    	when(arrowheadContext.get(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
       	when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.DELETE), eq(Void.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        
		testObject.unregisterSystem("system", "localhost", 1234);
		
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.DELETE), eq(Void.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryExecutorServiceInfoAddressNull() {
		try {
			testObject.queryExecutorServiceInfo(null, 0, null, null, 0, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryExecutorServiceInfoAddressEmpty() {
		try {
			testObject.queryExecutorServiceInfo(" ", 0, null, null, 0, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryExecutorServiceInfoBaseUriNull() {
		try {
			testObject.queryExecutorServiceInfo("localhost", 1234, null, null, 0, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("baseUri is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryExecutorServiceInfoServiceDefinitionNull() {
		try {
			testObject.queryExecutorServiceInfo("localhost", 1234, "", null, 0, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("serviceDefinition is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryExecutorServiceInfoServiceDefinitionEmpty() {
		try {
			testObject.queryExecutorServiceInfo("localhost", 1234, "", " ", 1, 1);
		} catch (final Exception ex) {
			Assert.assertEquals("serviceDefinition is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryExecutorServiceInfoOk() {
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ChoreographerExecutorServiceInfoResponseDTO.class), any(ChoreographerExecutorServiceInfoRequestDTO.class))).thenReturn(new ResponseEntity<>(
																																																new ChoreographerExecutorServiceInfoResponseDTO(), HttpStatus.OK));
		
		final ChoreographerExecutorServiceInfoResponseDTO result = testObject.queryExecutorServiceInfo("localhost", 1234, "", "service", 1, 1);
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ChoreographerExecutorServiceInfoResponseDTO.class), any(ChoreographerExecutorServiceInfoRequestDTO.class));
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testStartExecutorAddressNull() {
		try {
			testObject.startExecutor(null, 0, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testStartExecutorAddressEmpty() {
		try {
			testObject.startExecutor(" ", 0, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testStartExecutorBaseUriNull() {
		try {
			testObject.startExecutor("localhost", 1234, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("baseUri is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testStartExecutorPayloadNull() {
		try {
			testObject.startExecutor("localhost", 1234, "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("payload is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStartExecutorOk() {
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(ChoreographerExecuteStepRequestDTO.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
		
		testObject.startExecutor("localhost", 1234, "", new ChoreographerExecuteStepRequestDTO());
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(ChoreographerExecuteStepRequestDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testAbortExecutorAddressNull() {
		try {
			testObject.abortExecutor(null, 0, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testAbortExecutorAddressEmpty() {
		try {
			testObject.abortExecutor(" ", 0, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("address is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testAbortExecutorBaseUriNull() {
		try {
			testObject.abortExecutor("localhost", 1234, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("baseUri is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testAbortExecutorPayloadNull() {
		try {
			testObject.abortExecutor("localhost", 1234, "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("payload is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAbortExecutorOk() {
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(ChoreographerAbortStepRequestDTO.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
		
		testObject.abortExecutor("localhost", 1234, "", new ChoreographerAbortStepRequestDTO());
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(ChoreographerAbortStepRequestDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryOrchestratorFormNull() {
		try {
			testObject.queryOrchestrator(null);
		} catch (final Exception ex) {
			Assert.assertEquals("form is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryOrchestratorUriNotFound() {
		when(arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)).thenReturn(false);
    	
    	try {
    		testObject.queryOrchestrator(new OrchestrationFormRequestDTO());
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(ORCHESTRATION_PROCESS_URI_KEY);
    		Assert.assertEquals("Choreographer can't find orchestration process URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryOrchestratorUriWrongType() {
		when(arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(ORCHESTRATION_PROCESS_URI_KEY)).thenReturn("invalid");
    	
    	try {
    		testObject.queryOrchestrator(new OrchestrationFormRequestDTO());
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(ORCHESTRATION_PROCESS_URI_KEY);
    		verify(arrowheadContext, times(1)).get(ORCHESTRATION_PROCESS_URI_KEY);
    		Assert.assertEquals("Choreographer can't find orchestration process URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryOrchestratorOk() {
		when(arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(ORCHESTRATION_PROCESS_URI_KEY)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
       	when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(OrchestrationResponseDTO.class), any(OrchestrationFormRequestDTO.class))).thenReturn(new ResponseEntity<>(new OrchestrationResponseDTO(), HttpStatus.OK));
    	
		final OrchestrationResponseDTO result = testObject.queryOrchestrator(new OrchestrationFormRequestDTO());
		
		verify(arrowheadContext, times(1)).containsKey(ORCHESTRATION_PROCESS_URI_KEY);
		verify(arrowheadContext, times(1)).get(ORCHESTRATION_PROCESS_URI_KEY);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(OrchestrationResponseDTO.class), any(OrchestrationFormRequestDTO.class));
		Assert.assertNotNull(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSessionNotificationNotifyUriNull() {
		try {
			testObject.sendSessionNotification(null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Notification URI is not specified.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSessionNotificationNotifyUriEmpty() {
		try {
			testObject.sendSessionNotification(" ", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Notification URI is not specified.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSessionNotificationPayloadEmpty() {
		try {
			testObject.sendSessionNotification("https://localhost/abc", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is not specified.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendSessionNotificationOk() {
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(ChoreographerNotificationDTO.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
		
		testObject.sendSessionNotification("https://localhost/abc", new ChoreographerNotificationDTO());
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(ChoreographerNotificationDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCloseGatewayTunnelsNullInput() {
		try {
			testObject.closeGatewayTunnels(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Port list is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCloseGatewayTunnelsEmptyInput() {
		try {
			testObject.closeGatewayTunnels(List.of());
		} catch (final Exception ex) {
			Assert.assertEquals("Port list is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCloseGatewayTunnelsUriNotFound() {
		when(arrowheadContext.containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(false);
    	
    	try {
    		testObject.closeGatewayTunnels(List.of(5555));
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    		Assert.assertEquals("Choreographer can't find gateway close sessions URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCloseGatewayTunnelsUriWrongType() {
		when(arrowheadContext.containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn("invalid");
    	
    	try {
    		testObject.closeGatewayTunnels(List.of(5555));
    	} catch (final Exception ex) {
    		verify(arrowheadContext, times(1)).containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    		verify(arrowheadContext, times(1)).get(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    		Assert.assertEquals("Choreographer can't find gateway close sessions URI.", ex.getMessage());
    		
    		throw ex;
    	}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	@Test
	public void testCloseGatewayTunnelsOk() {
		when(arrowheadContext.containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final List<ActiveSessionCloseErrorDTO> result = List.of();
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(List.class), anyList())).thenReturn(new ResponseEntity<List>(result, HttpStatus.OK));
    	
   		testObject.closeGatewayTunnels(List.of(5555));

   		verify(arrowheadContext, times(1)).containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    	verify(arrowheadContext, times(1)).get(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    	verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(List.class), anyList());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	@Test
	public void testCloseGatewayTunnelsOnlyLogErrorResponse() {
		when(arrowheadContext.containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final List<ActiveSessionCloseErrorDTO> result = List.of(new ActiveSessionCloseErrorDTO(5555, "Not found"));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(List.class), anyList())).thenReturn(new ResponseEntity<List>(result, HttpStatus.OK));
    	
   		testObject.closeGatewayTunnels(List.of(5555));

   		verify(arrowheadContext, times(1)).containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    	verify(arrowheadContext, times(1)).get(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    	verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(List.class), anyList());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseGatewayTunnelsOnlyLogExceptions() {
		when(arrowheadContext.containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEWAY_CLOSE_SESSIONS_URI_KEY)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(List.class), anyList())).thenThrow(new RuntimeException("connection error"));
    	
   		testObject.closeGatewayTunnels(List.of(5555));

   		verify(arrowheadContext, times(1)).containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    	verify(arrowheadContext, times(1)).get(GATEWAY_CLOSE_SESSIONS_URI_KEY);
    	verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(List.class), anyList());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSearchForServicesFormsNull() {
		try {
			testObject.searchForServices(null, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Forms is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSearchForServicesFormListEmpty() {
		try {
			testObject.searchForServices(new ServiceQueryFormListDTO(), false);
		} catch (final Exception ex) {
			Assert.assertEquals("Form list is empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSearchForServicesMultiQueryServiceRegistryUriNotFound() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(false);
		
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(new ChoreographerServiceQueryFormDTO()));
		try {
			testObject.searchForServices(forms, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Choreographer can't find Service Registry multi-query URI.", ex.getMessage());
			
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSearchForServicesMultiQueryServiceRegistryUriWrongType() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn("invalid");
		
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(new ChoreographerServiceQueryFormDTO()));
		try {
			testObject.searchForServices(forms, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Choreographer can't find Service Registry multi-query URI.", ex.getMessage());
			
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSearchForServicesOkLocalOnly() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of(new ServiceRegistryResponseDTO()));
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(new ChoreographerServiceQueryFormDTO()));
		final Map<Integer,List<String>> resultMap = testObject.searchForServices(forms, false);
		
		Assert.assertEquals(1, resultMap.size());
		Assert.assertTrue(resultMap.containsKey(0));
		Assert.assertEquals("#OWN_CLOUD#", resultMap.get(0).get(0));
			
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSearchForServicesEmptyResultLocalOnly() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of());
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(new ChoreographerServiceQueryFormDTO()));
		final Map<Integer,List<String>> resultMap = testObject.searchForServices(forms, false);
		
		Assert.assertEquals(1, resultMap.size());
		Assert.assertTrue(resultMap.containsKey(0));
		Assert.assertEquals(0, resultMap.get(0).size());
			
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
		verify(httpService, never()).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSearchForServicesAllowInterCloudButLocalCloudOnly() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of());
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		
		final ChoreographerServiceQueryFormDTO form = new ChoreographerServiceQueryFormDTO();
		form.setLocalCloudOnly(true);
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(form));
		final Map<Integer,List<String>> resultMap = testObject.searchForServices(forms, true);
		
		Assert.assertEquals(1, resultMap.size());
		Assert.assertTrue(resultMap.containsKey(0));
		Assert.assertEquals(0, resultMap.get(0).size());
			
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
		verify(httpService, never()).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSearchForServicesMultiGlobalServiceDiscoveryUriNotFound() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of());
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		when(arrowheadContext.containsKey(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn(false);
		
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(new ChoreographerServiceQueryFormDTO()));
		
		try {
			testObject.searchForServices(forms, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Choreographer can't find Gatekeeper Multi Global Service Discovery URI.", ex.getMessage());
			
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
			verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
			verify(arrowheadContext, times(1)).containsKey(GATEKEEPER_MULTI_GSD_URI_KEY);
			verify(httpService, never()).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSearchForServicesMultiGlobalServiceDiscoveryWrongType() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of());
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		when(arrowheadContext.containsKey(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn("invalid");
		
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(new ChoreographerServiceQueryFormDTO()));
		
		try {
			testObject.searchForServices(forms, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Choreographer can't find Gatekeeper Multi Global Service Discovery URI.", ex.getMessage());
			
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
			verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
			verify(arrowheadContext, times(1)).containsKey(GATEKEEPER_MULTI_GSD_URI_KEY);
			verify(arrowheadContext, times(1)).get(GATEKEEPER_MULTI_GSD_URI_KEY);
			verify(httpService, never()).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSearchForServicesOkAllowIntercloud() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of());
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		when(arrowheadContext.containsKey(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/def").build());
		final CloudResponseDTO providerCloud = new CloudResponseDTO();
		providerCloud.setOperator("operator");
		providerCloud.setName("name");
		GSDMultiPollResponseDTO pollResponse = new GSDMultiPollResponseDTO();
		pollResponse.setProvidedServiceDefinitions(List.of("service"));
		pollResponse.setProviderCloud(providerCloud);
		final GSDMultiQueryResultDTO result2 = new GSDMultiQueryResultDTO(List.of(pollResponse), 0);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class))).thenReturn(new ResponseEntity<>(result2, HttpStatus.OK));
		
		ChoreographerServiceQueryFormDTO form = new ChoreographerServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("service");
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(form));
		
		final Map<Integer,List<String>> resultMap = testObject.searchForServices(forms, true);
		
		Assert.assertEquals(1, resultMap.size());
		Assert.assertTrue(resultMap.containsKey(0));
		Assert.assertEquals("operator/name", resultMap.get(0).get(0));

			
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
		verify(arrowheadContext, times(1)).containsKey(GATEKEEPER_MULTI_GSD_URI_KEY);
		verify(arrowheadContext, times(1)).get(GATEKEEPER_MULTI_GSD_URI_KEY);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSearchForServicesEmptyResponseAllowIntercloud() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/abc").build());
		final ServiceQueryResultDTO resultDTO = new ServiceQueryResultDTO();
		resultDTO.setServiceQueryData(List.of());
		final ServiceQueryResultListDTO result = new ServiceQueryResultListDTO(List.of(resultDTO));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));
		when(arrowheadContext.containsKey(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(GATEKEEPER_MULTI_GSD_URI_KEY)).thenReturn(UriComponentsBuilder.fromHttpUrl("http://localhost/def").build());
		final GSDMultiQueryResultDTO result2 = new GSDMultiQueryResultDTO(List.of(), 0);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class))).thenReturn(new ResponseEntity<>(result2, HttpStatus.OK));
		
		ChoreographerServiceQueryFormDTO form = new ChoreographerServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("service");
		final ServiceQueryFormListDTO forms = new ServiceQueryFormListDTO(List.of(form));
		
		final Map<Integer,List<String>> resultMap = testObject.searchForServices(forms, true);
		
		Assert.assertEquals(1, resultMap.size());
		Assert.assertTrue(resultMap.containsKey(0));
		Assert.assertEquals(0, resultMap.get(0).size());

			
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
		verify(arrowheadContext, times(1)).containsKey(GATEKEEPER_MULTI_GSD_URI_KEY);
		verify(arrowheadContext, times(1)).get(GATEKEEPER_MULTI_GSD_URI_KEY);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(GSDMultiQueryResultDTO.class), any(GSDMultiQueryFormDTO.class));
	}
}