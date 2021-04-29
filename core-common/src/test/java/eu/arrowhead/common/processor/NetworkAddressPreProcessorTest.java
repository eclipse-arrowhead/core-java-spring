package eu.arrowhead.common.processor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class NetworkAddressPreProcessorTest {

	//=================================================================================================
	// members

	private NetworkAddressPreProcessor processor = new NetworkAddressPreProcessor();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_0() {
		final String result = processor.normalize("2001:0db8:0000:0000:0000:ff00:0042:8329");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_1() {
		final String result = processor.normalize("2001:db8:0:0:0:ff00:42:8329");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_2() {
		final String result = processor.normalize("2001:db8:0::0:ff00:42:8329");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_3() {
		final String result = processor.normalize("2001:db8:0:::ff00::8329");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:0000:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_4() {
		final String result = processor.normalize("::db8:0::0:ff00:42:8329");
		Assert.assertEquals("0000:0db8:0000:0000:0000:ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_5() {
		final String result = processor.normalize("::1");
		Assert.assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_6() {
		final String result = processor.normalize("db8:0::0:ff00:42:8329::");
		Assert.assertEquals("0db8:0000:0000:0000:ff00:0042:8329:0000", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_7() {
		final String result = processor.normalize("db8::");
		Assert.assertEquals("0db8:0000:0000:0000:0000:0000:0000:0000", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_8() { //Unprocessable abbreviation .NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::db8::");
		System.out.println(result);
		Assert.assertEquals("::db8::", result);
	}
}
