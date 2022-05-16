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

package eu.arrowhead.core.serviceregistry.database.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;

@RunWith(SpringRunner.class)
public class RegistryUtilsTest {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeInterfaceNamesNullList() {
		Assert.assertEquals(0, RegistryUtils.normalizeInterfaceNames(null).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeInterfaceNamesEmptyList() {
		Assert.assertEquals(0, RegistryUtils.normalizeInterfaceNames(List.of()).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeInterfaceNamesRemovesNull() {
		Assert.assertEquals(1, RegistryUtils.normalizeInterfaceNames(Arrays.asList("a", null)).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeInterfaceNamesRemovesBlank() {
		Assert.assertEquals(1, RegistryUtils.normalizeInterfaceNames(List.of("a", "", "  ")).size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeInterfaceNamesUppercaseTrim() {
		final List<String> result = RegistryUtils.normalizeInterfaceNames(List.of(" http-secure-xml "));

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("HTTP-SECURE-XML", result.get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeInterfaceNamesAll() {
		final List<String> result = RegistryUtils.normalizeInterfaceNames(getExampleInterfaceNameList());

		Assert.assertEquals(3, result.size());
		Assert.assertEquals("HTTP-SECURE-JSON", result.get(0));
		Assert.assertEquals("HTTP-SECURE-XML", result.get(1));
		Assert.assertEquals("HTTP-INSECURE-JSON", result.get(2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnInterfacesProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnInterfaces(null, List.of("HTTP-SECURE-JSON")); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnInterfaces(List.of(), List.of("HTTP-SECURE-JSON")); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnInterfacesInterfaceNameListNullOrEmpty() {
		final ServiceRegistry sr = new ServiceRegistry();
		final List<ServiceRegistry> providedServices = List.of(sr);
		final List<ServiceRegistry> expectedList = List.of(sr);
		
		RegistryUtils.filterOnInterfaces(providedServices, null); // it just shows there is no exception and no changes if it called with null second parameter
		
		Assert.assertEquals(expectedList, providedServices);
		
		RegistryUtils.filterOnInterfaces(providedServices, List.of()); // it just shows there is no exception and no changes if it called with empty second parameter
		
		Assert.assertEquals(expectedList, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnInterfacesUnknownInterface() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		RegistryUtils.filterOnInterfaces(providedServices, List.of("HTTP-INSECURE-XML"));
		
		Assert.assertEquals(0, providedServices.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnInterfacesGood() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		Assert.assertEquals(2, providedServices.get(2).getInterfaceConnections().size());
		
		RegistryUtils.filterOnInterfaces(providedServices, List.of("HTTP-SECURE-JSON"));
		
		Assert.assertEquals(2, providedServices.size());
		Assert.assertEquals(1, providedServices.get(0).getId());
		Assert.assertEquals(3, providedServices.get(1).getId());
		Assert.assertEquals(1, providedServices.get(1).getInterfaceConnections().size()); // one interface removed 
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnInterfacesGood2() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		final List<ServiceRegistry> expectedList = new ArrayList<ServiceRegistry>(providedServices);
		
		RegistryUtils.filterOnInterfaces(providedServices, List.of("HTTP-SECURE-JSON", "HTTP-INSECURE-JSON"));
		
		Assert.assertEquals(3, providedServices.size());
		Assert.assertEquals(expectedList, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeSecurityTypesNullList() {
		Assert.assertEquals(0, RegistryUtils.normalizeSecurityTypes(null).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeSecurityTypesEmptyList() {
		Assert.assertEquals(0, RegistryUtils.normalizeSecurityTypes(List.of()).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeSecurityTypesRemoveNull() {
		Assert.assertEquals(0, RegistryUtils.normalizeSecurityTypes(Arrays.asList((ServiceSecurityType)null)).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeSecurityTypesDoesNothing() {
		final List<ServiceSecurityType> result = RegistryUtils.normalizeSecurityTypes(List.of(ServiceSecurityType.TOKEN));
		
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(ServiceSecurityType.TOKEN, result.get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnSecurityTypeProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnSecurityType(null, List.of(ServiceSecurityType.TOKEN)); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnSecurityType(List.of(), List.of(ServiceSecurityType.TOKEN)); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnSecurityTypeSecurityTypeListNullOrEmpty() {
		final ServiceRegistry sr = new ServiceRegistry();
		final List<ServiceRegistry> providedServices = List.of(sr);
		final List<ServiceRegistry> expectedList = List.of(sr);
		
		RegistryUtils.filterOnSecurityType(providedServices, null); // it just shows there is no exception and no changes if it called with null second parameter
		
		Assert.assertEquals(expectedList, providedServices);
		
		RegistryUtils.filterOnSecurityType(providedServices, List.of()); // it just shows there is no exception and no changes if it called with empty second parameter
		
		Assert.assertEquals(expectedList, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnSecurityTypeNoMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnSecurityType(providedServices, List.of(ServiceSecurityType.NOT_SECURE));
		
		Assert.assertEquals(0, providedServices.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnSecurityTypeOneMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnSecurityType(providedServices, List.of(ServiceSecurityType.TOKEN));
		
		Assert.assertEquals(1, providedServices.size());
		Assert.assertEquals(1, providedServices.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnSecurityTypeTwoMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnSecurityType(providedServices, List.of(ServiceSecurityType.CERTIFICATE));
		
		Assert.assertEquals(2, providedServices.size());
		Assert.assertEquals(2, providedServices.get(0).getId());
		Assert.assertEquals(3, providedServices.get(1).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnSecurityTypeThreeMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		final List<ServiceRegistry> expected = new ArrayList<>(providedServices);
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnSecurityType(providedServices, List.of(ServiceSecurityType.CERTIFICATE, ServiceSecurityType.TOKEN));
		
		Assert.assertEquals(3, providedServices.size());
		Assert.assertEquals(expected, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnVersionExactProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnVersion(null, 1); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnVersion(List.of(), 1); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnVersionExactNoMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnVersion(providedServices, 1);
		
		Assert.assertEquals(0, providedServices.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnVersionExactOneMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnVersion(providedServices, 2);
		
		Assert.assertEquals(1, providedServices.size());
		Assert.assertEquals(2, providedServices.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnVersionMinMaxProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnVersion(null, 1, 10); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnVersion(List.of(), 1, 10); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnVersionMinMaxNoMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnVersion(providedServices, 3, 5);
		
		Assert.assertEquals(0, providedServices.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnVersionMinMaxOneMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnVersion(providedServices, 5, 12);
		
		Assert.assertEquals(1, providedServices.size());
		Assert.assertEquals(3, providedServices.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnVersionMinMaxTwoMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnVersion(providedServices, 2, 10);
		
		Assert.assertEquals(2, providedServices.size());
		Assert.assertEquals(2, providedServices.get(0).getId());
		Assert.assertEquals(3, providedServices.get(1).getId());
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeMetadataNullMap() {
		Assert.assertEquals(0, RegistryUtils.normalizeMetadata(null).size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalStateException.class)
	public void testNormalizeMetadataDuplicateKeys() {
		final Map<String,String> map = Map.of("A", "B", "A ", "C");
		
		RegistryUtils.normalizeMetadata(map);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeMetadataNullValueRemoved() {
		final Map<String,String> map = new HashMap<>();
		map.put("A", null);
		
		final Map<String,String> normalizedMap = RegistryUtils.normalizeMetadata(map);
		
		Assert.assertEquals(0, normalizedMap.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeMetadataTrimKeyAndValue() {
		final Map<String,String> map = new HashMap<>();
		map.put("A  ", "B  ");
		
		final Map<String,String> normalizedMap = RegistryUtils.normalizeMetadata(map);
		
		Assert.assertEquals(1, normalizedMap.size());
		Assert.assertTrue(normalizedMap.containsKey("A"));
		Assert.assertEquals("B", normalizedMap.get("A"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnMetaProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnMeta(null, Map.of("A", "B")); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnMeta(List.of(), Map.of("A", "B")); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnMetaMetadataRequirementsNullOrEmpty() {
		final ServiceRegistry sr = new ServiceRegistry();
		final List<ServiceRegistry> providedServices = List.of(sr);
		final List<ServiceRegistry> expectedList = List.of(sr);
		
		RegistryUtils.filterOnMeta(providedServices, null); // it just shows there is no exception and no changes if it called with null second parameter
		
		Assert.assertEquals(expectedList, providedServices);
		
		RegistryUtils.filterOnMeta(providedServices, Map.of()); // it just shows there is no exception and no changes if it called with empty second parameter
		
		Assert.assertEquals(expectedList, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnMetaExactMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnMeta(providedServices, Map.of("key", "value2"));
		
		Assert.assertEquals(1, providedServices.size());
		Assert.assertEquals(3, providedServices.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnMetaSubsetMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnMeta(providedServices, Map.of("key", "value"));
		
		Assert.assertEquals(1, providedServices.size());
		Assert.assertEquals(2, providedServices.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnPingProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnPing(null, 1); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnPing(List.of(), 1); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnPingInvalidTimeout() {
		final ServiceRegistry sr = new ServiceRegistry();
		final List<ServiceRegistry> providedServices = List.of(sr);
		final List<ServiceRegistry> expectedList = List.of(sr);
		
		RegistryUtils.filterOnPing(providedServices, -1); // it just shows there is no exception if it called with invalid second parameter
		
		Assert.assertEquals(expectedList, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeAddressTypesNullList() {
		Assert.assertEquals(0, RegistryUtils.normalizeAddressTypes(null).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNormalizeAddressTypesEmptyList() {
		Assert.assertEquals(0, RegistryUtils.normalizeAddressTypes(List.of()).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void  testNormalizeAddressTypesRemoveNull() {
		Assert.assertEquals(0, RegistryUtils.normalizeAddressTypes(Arrays.asList((AddressType)null)).size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testFilterOnProviderAddressTypeProvidedServicesNullOrEmpty() {
		RegistryUtils.filterOnProviderAddressType(null, List.of(AddressType.IPV6)); // it just shows there is no exception if it called with null first parameter
		RegistryUtils.filterOnProviderAddressType(List.of(), List.of(AddressType.IPV6)); // it just shows there is no exception if it called with empty first parameter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnProviderAddressTypeAddressTypeListNullOrEmpty() {
		final ServiceRegistry sr = new ServiceRegistry();
		final List<ServiceRegistry> providedServices = List.of(sr);
		final List<ServiceRegistry> expectedList = List.of(sr);
		
		RegistryUtils.filterOnProviderAddressType(providedServices, null); // it just shows there is no exception and no changes if it called with null second parameter
		
		Assert.assertEquals(expectedList, providedServices);
		
		RegistryUtils.filterOnProviderAddressType(providedServices, List.of()); // it just shows there is no exception and no changes if it called with empty second parameter
		
		Assert.assertEquals(expectedList, providedServices);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnProviderAddressTypeNoMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnProviderAddressType(providedServices, List.of(AddressType.IPV6));
		
		Assert.assertEquals(0, providedServices.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnProviderAddressTypeOneMatch() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnProviderAddressType(providedServices, List.of(AddressType.IPV4));
		
		Assert.assertEquals(1, providedServices.size());
		Assert.assertEquals(2, providedServices.get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterOnProviderAddressTypeTwoMatches() {
		final List<ServiceRegistry> providedServices = getProvidedServices();
		
		Assert.assertEquals(3, providedServices.size());
		
		RegistryUtils.filterOnProviderAddressType(providedServices, List.of(AddressType.IPV4, AddressType.HOSTNAME));
		
		Assert.assertEquals(2, providedServices.size());
		Assert.assertEquals(2, providedServices.get(0).getId());
		Assert.assertEquals(3, providedServices.get(1).getId());
	}
 	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<String> getExampleInterfaceNameList() {
		return Arrays.asList("HTTP-SECURE-JSON", null, "", " ", "http-secure-xml", "   HTTP-INSECURE-JSON   ");
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistry> getProvidedServices() {
		final ServiceInterface intf1 = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceInterface intf2 = new ServiceInterface("HTTP-SECURE-XML");
		final ServiceInterface intf3 = new ServiceInterface("HTTP-INSECURE-JSON");
		
		final ServiceRegistry srEntry1 = new ServiceRegistry();
		srEntry1.setId(1);
		srEntry1.setVersion(null);
		srEntry1.setSecure(ServiceSecurityType.TOKEN);
		srEntry1.setMetadata(null);
		srEntry1.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(srEntry1, intf1));
		srEntry1.setSystem(new System("system", "something", null, 1234, null, null));
		
		final ServiceRegistry srEntry2 = new ServiceRegistry();
		srEntry2.setId(2);
		srEntry2.setVersion(2);
		srEntry2.setSecure(ServiceSecurityType.CERTIFICATE);
		srEntry2.setMetadata("key=value, otherkey=othervalue");
		srEntry2.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(srEntry2, intf3));
		srEntry2.setSystem(new System("system2", "127.0.0.1", AddressType.IPV4, 1234, null, null));
		
		final ServiceRegistry srEntry3 = new ServiceRegistry();
		srEntry3.setId(3);
		srEntry3.setVersion(10);
		srEntry3.setSecure(ServiceSecurityType.CERTIFICATE);
		srEntry3.setMetadata("key=value2");
		srEntry3.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(srEntry3, intf1));
		srEntry3.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(srEntry3, intf2));
		srEntry3.setSystem(new System("system3", "localhost", AddressType.HOSTNAME, 1234, null, null));
		
		return new ArrayList<>(List.of(srEntry1, srEntry2, srEntry3));
	}
}