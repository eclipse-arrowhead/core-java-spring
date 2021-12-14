package eu.arrowhead.common.processor;

import org.junit.Assert;
import org.junit.Test;

import eu.arrowhead.common.dto.shared.AddressType;

public class SpecialNetworkAddressTypeDetectorTest {
	
	//=================================================================================================
	// members
	
	private SpecialNetworkAddressTypeDetector testingObject = new SpecialNetworkAddressTypeDetector();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeNull() {
		final AddressType result = testingObject.detectAddressType(null);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeEmpty() {
		final AddressType result = testingObject.detectAddressType("");
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeSimpleHostname() {
		final AddressType result = testingObject.detectAddressType("localhost");
		Assert.assertEquals(AddressType.HOSTNAME, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeComplexHostname() {
		final AddressType result = testingObject.detectAddressType("www.google.com");
		Assert.assertEquals(AddressType.HOSTNAME, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeIPv4() {
		final AddressType result = testingObject.detectAddressType("192.168.0.1");
		Assert.assertEquals(AddressType.IPV4, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeIPv6() {
		final AddressType result = testingObject.detectAddressType("0000:ffff:1234:5678:9ABC:DEF0:1234:5678");
		Assert.assertEquals(AddressType.IPV6, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetectAddressTypeUnknown() {
		final AddressType result = testingObject.detectAddressType("ghij:ffff:1234:5678:9ABC:DEF0:1234:5678");
		Assert.assertNull(result);
	}
}