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

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class CommonDBServiceTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private CommonDBService commonDBService; 

	@Mock
	private CloudRepository cloudRepository;

	//=================================================================================================
	// methods
	
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
	public void testInsertOwnCloudCloudAlreadyExists() {
		when(cloudRepository.findByOperatorAndName("operator", "name")).thenReturn(Optional.of(new Cloud()));
		commonDBService.insertOwnCloud("operator", "name", false, null);
	}
}