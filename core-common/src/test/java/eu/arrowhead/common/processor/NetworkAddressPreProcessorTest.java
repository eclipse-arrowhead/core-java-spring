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
	public void testNullString() {
		final String result = processor.normalize(null);
		Assert.assertEquals("", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEmptyString() {
		final String result = processor.normalize("  ");
		Assert.assertEquals("", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSimpleString_0() {
		final String result = processor.normalize("simPleString064withoutDotOrColon");
		Assert.assertEquals("simplestring064withoutdotorcolon", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSimpleString_1() {
		final String result = processor.normalize("  simPleString064withoutDotOrColon   ");
		Assert.assertEquals("simplestring064withoutdotorcolon", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStringWithDot() {
		final String result = processor.normalize("String.with.Dot.abc");
		Assert.assertEquals("string.with.dot.abc", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStringWithColon() {
		final String result = processor.normalize("String:with:Colon:abc");
		Assert.assertEquals("string:with:colon:abc", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStringWithDotAndColon() {
		final String result = processor.normalize("String::with.Dot:Colon.");
		Assert.assertEquals("string::with.dot:colon.", result);
	}
	
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
		final String result = processor.normalize("2001:db8:0:::ff00::8329"); //Unprocessable abbreviation. NetworkAddressVerifier will filter it out
		Assert.assertEquals("2001:db8:0:::ff00::8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_4() {
		final String result = processor.normalize("::db8:0::0:ff00:42:8329"); //Unprocessable abbreviation. NetworkAddressVerifier will filter it out
		Assert.assertEquals("::db8:0::0:ff00:42:8329", result);
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
		final String result = processor.normalize("db8:0::0:ff00:42:8329::"); //Unprocessable abbreviation. NetworkAddressVerifier will filter it out
		Assert.assertEquals("db8:0::0:ff00:42:8329::", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_7() {
		final String result = processor.normalize("db8::");
		Assert.assertEquals("0db8:0000:0000:0000:0000:0000:0000:0000", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_8() { //Unprocessable abbreviation. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::db8::");		
		Assert.assertEquals("::db8::", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_9() {
		final String result = processor.normalize("2001:0db8::ff00:0042:8329");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_10() {
		final String result = processor.normalize("2001:0db8:9e52:8a63::ff00:0042:8329"); 
		Assert.assertEquals("2001:0db8:9e52:8a63:0000:ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_11() {
		final String result = processor.normalize("a345:2001:0db8:9e52:8a63::ff00:0042:8329"); //Invalid. NetworkAddressVerifier will filter it out
		Assert.assertEquals("a345:2001:0db8:9e52:8a63::ff00:0042:8329", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_0() {
		final String result = processor.normalize("2001:0db8::ff00:192.0.2.128");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:c000:0280", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_1() {
		final String result = processor.normalize("2001:0db8:0000:0000:0000:ff00:192.0.2.128");
		Assert.assertEquals("2001:0db8:0000:0000:0000:ff00:c000:0280", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_2() {
		final String result = processor.normalize("::ffff:192.0.2.128");
		Assert.assertEquals("0000:0000:0000:0000:0000:ffff:c000:0280", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_3() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.2");
		Assert.assertEquals("::ffff:192.0.2", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_4() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.2.128.6");
		Assert.assertEquals("::ffff:192.0.2.128.6", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_5() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.ff00.128");
		Assert.assertEquals("::ffff:192.0.ff00.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_6() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.2.128::");
		Assert.assertEquals("::ffff:c000:0280::", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_7() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:256.0.2.128");
		Assert.assertEquals("::ffff:256.0.2.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_8() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.256.2.128");
		Assert.assertEquals("::ffff:192.256.2.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_9() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.256.128");
		Assert.assertEquals("::ffff:192.0.256.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_10() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.2.256");
		Assert.assertEquals("::ffff:192.0.2.256", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_11() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:-192.0.2.128");
		Assert.assertEquals("::ffff:-192.0.2.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_12() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.-1.2.128");
		Assert.assertEquals("::ffff:192.-1.2.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_13() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.-2.128");
		Assert.assertEquals("::ffff:192.0.-2.128", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIPv6_IPv4_Hybrid_14() { //Unprocessable IPv6-IPv4 hybrid. NetworkAddressVerifier will filter it out
		final String result = processor.normalize("::ffff:192.0.2.-128");
		Assert.assertEquals("::ffff:192.0.2.-128", result);
	}
}
