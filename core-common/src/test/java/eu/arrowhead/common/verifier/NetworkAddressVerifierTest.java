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

package eu.arrowhead.common.verifier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class NetworkAddressVerifierTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Spy
	private CommonNamePartVerifier commonNamePartVerifier;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNullAddress() {
		networkAddressVerifier.verify(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyEmptyAddress() {
		networkAddressVerifier.verify("  ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIPV4_OK() {
		try {
			networkAddressVerifier.verify("192.0.2.18");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIPV4_SelfAddressing_Allowed() {
		ReflectionTestUtils.setField(networkAddressVerifier, "allowSelfAddressing", true);
		try {
			networkAddressVerifier.verify("127.0.0.1");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV4_SelfAddressing_Denied() {
		networkAddressVerifier.verify("127.0.0.1");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIPV4_NonRoutableAddressing_Allowed() {
		ReflectionTestUtils.setField(networkAddressVerifier, "allowNonRoutableAddressing", true);
		try {
			networkAddressVerifier.verify("169.254.2.11");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV4_NonRoutableAddressing_Denied() {
		networkAddressVerifier.verify("169.254.52.118");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV4_PlaceholderAddress() {
		networkAddressVerifier.verify("0.0.0.0");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV4_BroadcastAddress() {
		networkAddressVerifier.verify("255.255.255.255");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV4_MulticatAddress_1() {
		networkAddressVerifier.verify("224.0.0.0");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV4_MulticatAddress_2() {
		networkAddressVerifier.verify("239.255.255.255");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIPV6_OK() {
		try {
			networkAddressVerifier.verify("ee80:0000:dda5:9ef3:935c:0000:0000:f85d");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIPV6_SelfAddressing_Allowed() {
		ReflectionTestUtils.setField(networkAddressVerifier, "allowSelfAddressing", true);
		try {
			networkAddressVerifier.verify("0000:0000:0000:0000:0000:0000:0000:0001");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV6_SelfAddressing_Denied() {
		networkAddressVerifier.verify("0000:0000:0000:0000:0000:0000:0000:0001");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIPV6_NonRoutableAddressing_Allowed() {
		ReflectionTestUtils.setField(networkAddressVerifier, "allowNonRoutableAddressing", true);
		try {
			networkAddressVerifier.verify("fe80:0db8:0000:0000:0000:ff00:0042:8329");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV6_NonRoutableAddressing_Denied() {
		networkAddressVerifier.verify("fe80:0db8:0000:0000:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV6_UnspecifiedAddress() {
		networkAddressVerifier.verify("0000:0000:0000:0000:0000:0000:0000:0000");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV6_MulticastAddress_1() {
		networkAddressVerifier.verify("fff0:0db8:0000:0000:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyIPV6_MulticastAddress_2() {
		networkAddressVerifier.verify("ffe3:0db8:0000:0000:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoType_1() {		
		try {
			networkAddressVerifier.verify("notype");	
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoType_2() {
		ReflectionTestUtils.setField(networkAddressVerifier, "allowSelfAddressing", true);
		try {
			networkAddressVerifier.verify("localhost");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_3() {
		networkAddressVerifier.verify("localhost");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoType_4() {
		ReflectionTestUtils.setField(networkAddressVerifier, "allowSelfAddressing", true);
		try {
			networkAddressVerifier.verify("loopback");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_5() {
		networkAddressVerifier.verify("loopback");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoType_6() {
		try {
			networkAddressVerifier.verify("String.with.Dot.abc");			
		} catch (InvalidParameterException ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_7() {
		networkAddressVerifier.verify("48Invalid.with.Dot.abc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_8() {
		networkAddressVerifier.verify("Invalid-.with.Dot.abc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_9() {
		networkAddressVerifier.verify("::db8::");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_10() {
		networkAddressVerifier.verify("192.0.2");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_11() {
		networkAddressVerifier.verify("::ffff:192.0.2");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_12() {
		networkAddressVerifier.verify("192.0.2.128.6");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_13() {
		networkAddressVerifier.verify("::ffff:192.0.2.128.6");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_14() {
		networkAddressVerifier.verify("192.0.ff00.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_15() {
		networkAddressVerifier.verify("::ffff:192.0.ff00.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_16() {
		networkAddressVerifier.verify("::ffff:192.0.2.128::");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_17() {
		networkAddressVerifier.verify("256.0.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_18() {
		networkAddressVerifier.verify("::ffff:256.0.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_19() {
		networkAddressVerifier.verify("192.256.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_20() {
		networkAddressVerifier.verify("::ffff:192.256.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_21() {
		networkAddressVerifier.verify("192.0.256.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_22() {
		networkAddressVerifier.verify("::ffff:192.0.256.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_23() {
		networkAddressVerifier.verify("192.0.2.256");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_24() {
		networkAddressVerifier.verify("::ffff:192.0.2.256");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_25() {
		networkAddressVerifier.verify("-192.0.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_26() {
		networkAddressVerifier.verify("::ffff:-192.0.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_27() {
		networkAddressVerifier.verify("192.-1.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_28() {
		networkAddressVerifier.verify("::ffff:192.-1.2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_29() {
		networkAddressVerifier.verify("192.1.-2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_30() {
		networkAddressVerifier.verify("::ffff:192.1.-2.128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_31() {
		networkAddressVerifier.verify("192.1.2.-128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_32() {
		networkAddressVerifier.verify("::ffff:192.1.2.-128");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_33() {
		networkAddressVerifier.verify("0uo3:0db8:0000:0000:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_34() {
		networkAddressVerifier.verify("0000:0uo3:0000:0000:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_35() {
		networkAddressVerifier.verify("0000:0db8:0uo3:0000:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_36() {
		networkAddressVerifier.verify("0000:0db8:0000:0uo3:0000:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_37() {
		networkAddressVerifier.verify("0000:0db8:0000:0000:0uo3:ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_38() {
		networkAddressVerifier.verify("0000:0db8:0000:0000:0000:0uo3:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_39() {
		networkAddressVerifier.verify("0000:0db8:0000:0000:0000:ff00:0uo3:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_40() {
		networkAddressVerifier.verify("0000:0db8:0000:0000:0000:ff00:0042:0uo3");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_41() {
		networkAddressVerifier.verify("::ff00:0042:8329");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_42() {
		networkAddressVerifier.verify("fa00:0042:8329::");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_43() {
		networkAddressVerifier.verify("0:8:0:0:0:f:2:9");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_44() {
		networkAddressVerifier.verify("0:8:0::0:f:2:9");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyNoType_45() {
		networkAddressVerifier.verify("0000:0db8:0000:0000:0000:ff00:192.0.2.118");
	}
}
