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

package eu.arrowhead.core.gatekeeper.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

@RunWith(SpringRunner.class)
public class GatekeeperDriverGSDTest {
	
	//=================================================================================================
	// members
		
	private GatekeeperDriver testingObject = new GatekeeperDriver();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullCloudList() throws InterruptedException {
		testingObject.sendGSDPollRequest(null, getGSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithEmptyCloudList() throws InterruptedException {
		testingObject.sendGSDPollRequest(new ArrayList<>(), getGSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullGSDPollRequestDTO() throws InterruptedException {
		testingObject.sendGSDPollRequest(List.of(new Cloud()), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequestedService() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.setRequestedService(null);
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequestedServiceDefinition() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.getRequestedService().setServiceDefinitionRequirement(null);
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithBlankRequestedServiceDefinition() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.getRequestedService().setServiceDefinitionRequirement("   ");
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO); 
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequesterCloud() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.setRequesterCloud(null);;
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendServiceReistryQueryNullQueryForm() {
		testingObject.sendServiceRegistryQuery(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullQueryDataList() {
		testingObject.sendInterCloudAuthorizationCheckQuery(null, new CloudRequestDTO(), "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullCloud() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), null, "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullServiceDefinition() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), new CloudRequestDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithBlankServiceDefinition() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), new CloudRequestDTO(), "  ");
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private GSDPollRequestDTO getGSDPollRequestDTO() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		
		return new GSDPollRequestDTO(serviceQueryFormDTO, cloudRequestDTO, false, false);
	}
}