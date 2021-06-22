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
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.repository.CloudRepository;
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
	private CommonNamePartVerifier cnVerifier;
	
	private CommonNamePartVerifier realVerifier = new CommonNamePartVerifier();

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
}