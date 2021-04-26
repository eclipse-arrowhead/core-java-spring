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

package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@RunWith(SpringRunner.class)
public class DefaultIntraCloudProviderMatchmakerTest {

	//=================================================================================================
	// members
	
	private IntraCloudProviderMatchmakingAlgorithm algorithm = new DefaultIntraCloudProviderMatchmaker();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void doMatchmakingSRListNull() {
		algorithm.doMatchmaking(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void doMatchmakingSRListEmpty() {
		algorithm.doMatchmaking(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void doMatchmakingParamsNull() {
		algorithm.doMatchmaking(List.of(new OrchestrationResultDTO()), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void doMatchmakingNoPreferred() {
		final OrchestrationResultDTO dto1 = new OrchestrationResultDTO();
		dto1.setVersion(1);
		final OrchestrationResultDTO dto2 = new OrchestrationResultDTO();
		dto2.setVersion(2);
		
		final OrchestrationResultDTO selected = algorithm.doMatchmaking(List.of(dto1, dto2), new IntraCloudProviderMatchmakingParameters(List.of()));

		Assert.assertEquals(1, selected.getVersion().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void doMatchmakingNoPreferredMatch() {
		final SystemResponseDTO system1 = new SystemResponseDTO(1, "system1", "localhost", 1234, null, null, null, null);
		final SystemResponseDTO system2 = new SystemResponseDTO(1, "system2", "localhost", 4567, null, null, null, null);
		final OrchestrationResultDTO dto1 = new OrchestrationResultDTO();
		dto1.setVersion(1);
		dto1.setProvider(system1);
		final OrchestrationResultDTO dto2 = new OrchestrationResultDTO();
		dto2.setVersion(2);
		dto2.setProvider(system2);
		
		final SystemRequestDTO reqSystem = new SystemRequestDTO();
		reqSystem.setSystemName("other");
		reqSystem.setAddress("127.0.0.1");
		reqSystem.setPort(1111);
		final PreferredProviderDataDTO ppDTO = new PreferredProviderDataDTO();
		ppDTO.setProviderSystem(reqSystem);
		
		final OrchestrationResultDTO selected = algorithm.doMatchmaking(List.of(dto1, dto2), new IntraCloudProviderMatchmakingParameters(List.of(ppDTO)));
		
		Assert.assertEquals(1, selected.getVersion().intValue());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void doMatchmakingPreferredMatch() {
		final SystemResponseDTO system1 = new SystemResponseDTO(1, "system1", "localhost", 1234, null, null, null, null);
		final SystemResponseDTO system2 = new SystemResponseDTO(1, "system2", "localhost", 4567, null, null, null, null);
		final OrchestrationResultDTO dto1 = new OrchestrationResultDTO();
		dto1.setVersion(1);
		dto1.setProvider(system1);
		final OrchestrationResultDTO dto2 = new OrchestrationResultDTO();
		dto2.setVersion(2);
		dto2.setProvider(system2);
		
		final SystemRequestDTO reqSystem = new SystemRequestDTO();
		reqSystem.setSystemName("System2");
		reqSystem.setAddress("localhost   ");
		reqSystem.setPort(4567);
		final PreferredProviderDataDTO ppDTO = new PreferredProviderDataDTO();
		ppDTO.setProviderSystem(reqSystem);
		
		final OrchestrationResultDTO selected = algorithm.doMatchmaking(List.of(dto1, dto2), new IntraCloudProviderMatchmakingParameters(List.of(ppDTO)));
		
		Assert.assertEquals(2, selected.getVersion().intValue());
	}
}