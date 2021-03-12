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

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

@RunWith(SpringRunner.class)
public class DefaultCloudMatchmakerTest {

	//=================================================================================================
	// members
	
	private final CloudMatchmakingAlgorithm algorithm = new DefaultCloudMatchmaker();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingReturnFirst() {		
		final boolean onlyPreferred = false;
		final List<CloudRequestDTO> preferredClouds = List.of();
		
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null,	null);
		final int numOfProviders10 = 10;		
		final GSDPollResponseDTO gsdPollResponseDTO1 = new GSDPollResponseDTO(providerCloud1, "requiredServiceDefinition", null, numOfProviders10, null, null, false);
		
		final CloudResponseDTO providerCloud2 = new CloudResponseDTO(1L, "operator2", "cloudname2", false, true, false, null, null,	null);
		final int numOfProviders1 = 1;		
		final GSDPollResponseDTO gsdPollResponseDTO2 = new GSDPollResponseDTO(providerCloud2, "requiredServiceDefinition", null, numOfProviders1, null, null, false);
		
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of(gsdPollResponseDTO1, gsdPollResponseDTO2);
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO( gsdPollResponseDTOList, unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO,	preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);

		Assert.assertTrue(providerCloud1.getName().equalsIgnoreCase(cloudResponseDTO.getName()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingReturnFirstOnlyIfItIsPreferred() {		
		final boolean onlyPreferred = false;
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator2");
		cloudRequestDTO.setName("cloudname2");
		
		final List<CloudRequestDTO> preferredClouds = List.of(cloudRequestDTO);

		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null,	null);
		final int numOfProviders10 = 10;		
		final GSDPollResponseDTO gsdPollResponseDTO1 = new GSDPollResponseDTO(providerCloud1, "requiredServiceDefinition", null, numOfProviders10, null, null, false);
		
		final CloudResponseDTO providerCloud2 = new CloudResponseDTO(1L, "operator2", "cloudname2", false, true, false, null, null,	null);
		final int numOfProviders1 = 1;		
		final GSDPollResponseDTO gsdPollResponseDTO2 = new GSDPollResponseDTO(providerCloud2, "requiredServiceDefinition", null, numOfProviders1, null, null, false);
		
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of(gsdPollResponseDTO1, gsdPollResponseDTO2);
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO(gsdPollResponseDTOList, unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO,	preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);

		Assert.assertFalse(providerCloud1.getName().equalsIgnoreCase(cloudResponseDTO.getName()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingOnlyPreferred() {		
		final boolean onlyPreferred = true;
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator100");
		cloudRequestDTO.setName("cloudname100");
		
		final List<CloudRequestDTO> preferredClouds = List.of(cloudRequestDTO);

		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final int numOfProviders10 = 10;		
		final GSDPollResponseDTO gsdPollResponseDTO1 = new GSDPollResponseDTO(providerCloud1, "requiredServiceDefinition", null, numOfProviders10, null, null, false);
		
		final CloudResponseDTO providerCloud2 = new CloudResponseDTO(1L, "operator2", "cloudname2", false, true, false,	null, null,	null);
		final int numOfProviders1 = 1;		
		final GSDPollResponseDTO gsdPollResponseDTO2 = new GSDPollResponseDTO(providerCloud2, "requiredServiceDefinition", null, numOfProviders1, null, null, false);
		
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of(gsdPollResponseDTO1, gsdPollResponseDTO2);
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO(gsdPollResponseDTOList, unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO,	preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);
		
		Assert.assertNull(cloudResponseDTO.getName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingNotOnlyPreferred() {		
		final boolean onlyPreferred = false;
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator100");
		cloudRequestDTO.setName("cloudname100");
		
		final List<CloudRequestDTO> preferredClouds = List.of(cloudRequestDTO);

		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null,	null);
		final int numOfProviders10 = 10;		
		final GSDPollResponseDTO gsdPollResponseDTO1 = new GSDPollResponseDTO(providerCloud1, "requiredServiceDefinition", null, numOfProviders10, null, null, false);
		
		final CloudResponseDTO providerCloud2 = new CloudResponseDTO(1L, "operator2", "cloudname2", false, true, false, null, null, null);
		final int numOfProviders1 = 1;		
		final GSDPollResponseDTO gsdPollResponseDTO2 = new GSDPollResponseDTO(providerCloud2, "requiredServiceDefinition", null, numOfProviders1, null, null, false);
		
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of(gsdPollResponseDTO1, gsdPollResponseDTO2);
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO(gsdPollResponseDTOList, unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO,	preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);
		
		Assert.assertNotNull(cloudResponseDTO.getName());
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testDoMatchmakingNullParameter() {		
		algorithm.doMatchmaking(null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakigNullGSDQueryResultDTO() {		
		final boolean onlyPreferred = false;
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator100");
		cloudRequestDTO.setName("cloudname100");
		
		final List<CloudRequestDTO> preferredClouds = List.of(cloudRequestDTO);
		
		final GSDQueryResultDTO gsdQueryResultDTO = null;
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO,	preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);
		
		Assert.assertNull(cloudResponseDTO.getName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakigEmptyGSDQueryResultDTO() {		
		final boolean onlyPreferred = false;
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator100");
		cloudRequestDTO.setName("cloudname100");
		
		final List<CloudRequestDTO> preferredClouds = List.of(cloudRequestDTO);
		
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of();
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO(gsdPollResponseDTOList, unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO, preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);
		
		Assert.assertNull(cloudResponseDTO.getName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingProvidersWithZeroWeightWillNBeIncludedInResponse() {		
		final boolean onlyPreferred = false;
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator100");
		cloudRequestDTO.setName("cloudname100");
		
		final List<CloudRequestDTO> preferredClouds = List.of(cloudRequestDTO);

		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1",	false, true, false, null, null,	null);
		final int numOfProviders10 = 0;		
		final GSDPollResponseDTO gsdPollResponseDTO1 = new GSDPollResponseDTO(providerCloud1, "requiredServiceDefinition", null, numOfProviders10, null, null, false);
		
		final CloudResponseDTO providerCloud2 = new CloudResponseDTO(1L, "operator2", "cloudname2", false, true, false,	null, null,	null);
		final int numOfProviders1 = 0;		
		final GSDPollResponseDTO gsdPollResponseDTO2 = new GSDPollResponseDTO(providerCloud2, "requiredServiceDefinition", null, numOfProviders1, null, null, false);
		
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of(gsdPollResponseDTO1, gsdPollResponseDTO2);
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO(gsdPollResponseDTOList, unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(gsdQueryResultDTO, preferredClouds, onlyPreferred);	
		final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);
		
		Assert.assertNotNull(cloudResponseDTO.getName());
	}
}