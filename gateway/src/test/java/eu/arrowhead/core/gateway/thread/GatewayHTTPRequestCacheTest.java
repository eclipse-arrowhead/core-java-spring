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

package eu.arrowhead.core.gateway.thread;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GatewayHTTPRequestCacheTest {
	
	//=================================================================================================
	// members
	
	private static final String validFirstPart = "POST /car?token=eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.L4Iwcfrne6BD-DZcnyjRi4-8GT8XekzXLd3nNSLVXliIbB-lE76AgZnjs9ieQPRMqXDenrVY9SkuWqszEF2uqJP0rzxon-Xwr02yTAUlC1iPzoVomUnaQkpl3OsKEAkbfKgii9lZG2nqMtoAkCzxe8cgY6hu8Mpa6KihhTje9EpqNQhvbc1_vswHRlvd7dGNChY17JEhTMhlhbLvreEH7JUy24MeA7PLh2yWyPL0ouXoocdLzDh1weEgY5GEh_Ag4kpQ8Y9GnE4RULdrqvYF5zv28M_d4SmWvB2ISB5Z1qZyKcAhdT4hlF5stj5FwxMWr19m6-5neJ-QA5VviXIjxQ.kK5rWeM9dM8VZ7K3G_dGVw.gLCdSjagrxvnXgbVodHxYP7fRQogCFTtZVUqhKHOcRxJoHrbHx9tu1U2RAE0TMNo7ytcNez3DJa-Ahn8AX1MMqwlUOdx3wrSAPsn7spvTOWsSWDMpzZ2ASnpX3IQPc-9j1D1YWD3qCpBH-PeRzVXLUD1M1panI6WLZORrw-a52hsHrnBmoA87VBcL5pQ_jqfsMkJbpvsIKlNFZP9ZsgHmoqTnFNc5OGiHcm-gmVly64T_lPRh2S3Wjzjb0g47xs0irCIjTApKT4U2rG0M4Drg-5ns4C7l7lNSMd5NdLkwiZ_4ly7wu3RbWEonQQRR08-uFv-uXczWB_-FquhhN_LUF81hh4HP-FJDAEpUuMCNX0qTL0uwvTujnLfjZWopIjpw5Z3RG5bKrsBveN3FpI-0NlF48LhGXwx2Kbj98c-mm5_ZO5ck6GTmlIVQpU3H9WJ9NXNW6LnQYlUPtUtub8xFy9SNQZ47gwOQ1V18Ui1xAcO4FLF7DlmQysy0xyWbQH3ryVguL5uEsvVI6UaDCO-zRr5L7vIghlRLJvH2c1bpG-SfxgBZbdKvK"; 
	private static final String validSecondPart = "rApdWo-9_e-eC8B-Yi9u2WfdrShUq5s2OTUFNc2Up0YSG7g-v7I65RkqSIcSgYWBbtQc-cmOxgztPP1D83XzpWl-89-Wng0bI5brSxFhER4E2wjHANJOJ1bXYVVocyoGFrJ3Yf1nLElg0uJPEOMWDU0QsCimdtunfVt-deF9R4Iz2CoKatWzCS_3y8lDSglVFS2HBxkY8hOPhLzSh42ER8kQWZSAYWJc-Zc39fp_9ByuiAaVbNzfvz2dQI1GMVjBlcL_FWkdg2.MMy3Ajz0IJVqcOMt9GBtDAkQHGOBBmOfcmBd_rjC-3c HTTP/1.1\r\n" + 
												  "Accept: text/plain\r\n" + 
												  "Accept: application/json\r\n" + 
												  "Content-Type: application/json\r\n" + 
												  "Content-Length: 35\r\n" + 
												  "Host: 127.0.0.1:8000\r\n" + 
												  "Connection: Keep-Alive\r\n" + 
												  "User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" + 
												  "Accept-Encoding: gzip,deflate\r\n" + 
												  "\r\n" + 
												  "{\"brand\":\"opel - 0\",\"color\":\"blue\"}";

	private static final String unfinishedRequest = "POST / HTTP/1.1\r\n" + 
			 										"Accept: text/plain\r\n" + 
			 										"Accept: application/json\r\n" + 
			 										"Content-Type: application/json\r\n" + 
			 										"Transfer-Encoding:";
	
	private static final String noBodyRequest = "DELETE / HTTP/1.1\r\n" + 
			  									"Accept: text/plain\r\n" + 
			  									"User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" + 
		  										"\r\n"; 
	
	private static final String validSecondPartNoBody = "rApdWo-9_e-eC8B-Yi9u2WfdrShUq5s2OTUFNc2Up0YSG7g-v7I65RkqSIcSgYWBbtQc-cmOxgztPP1D83XzpWl-89-Wng0bI5brSxFhER4E2wjHANJOJ1bXYVVocyoGFrJ3Yf1nLElg0uJPEOMWDU0QsCimdtunfVt-deF9R4Iz2CoKatWzCS_3y8lDSglVFS2HBxkY8hOPhLzSh42ER8kQWZSAYWJc-Zc39fp_9ByuiAaVbNzfvz2dQI1GMVjBlcL_FWkdg2.MMy3Ajz0IJVqcOMt9GBtDAkQHGOBBmOfcmBd_rjC-3c HTTP/1.1\r\n" + 
			  "Accept: text/plain\r\n" + 
			  "Accept: application/json\r\n" + 
			  "Content-Type: application/json\r\n" + 
			  "Content-Length: 37\r\n" + 
			  "Host: 127.0.0.1:8000\r\n" + 
			  "Connection: Keep-Alive\r\n" + 
			  "User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" + 
			  "Accept-Encoding: gzip,deflate\r\n" + 
			  "\r\n";
	
	private static final String utf8Body = "{\"brand\":\"opel - 0\",\"color\":\"ütős\"}";
	
	private GatewayHTTPRequestCache cache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		cache = new GatewayHTTPRequestCache(1024);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddBytesNull() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(null);
		Assert.assertEquals(0, cache.getCacheLength());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddBytesZeroLengthArray() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(new byte[0]);
		Assert.assertEquals(0, cache.getCacheLength());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddBytesTwice() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(new byte[] { 1, 2, 3});
		Assert.assertEquals(3, cache.getCacheLength());
		cache.addBytes(new byte[] { 1, 2, 3, 4});
		Assert.assertEquals(7, cache.getCacheLength());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void resetEmptyCache() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.resetCache();
		Assert.assertEquals(0, cache.getCacheLength());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void resetNotEmptyCache() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(new byte[] { 1, 2, 3});
		Assert.assertEquals(3, cache.getCacheLength());
		cache.resetCache();
		Assert.assertEquals(0, cache.getCacheLength());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestEmptyCache() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] request = cache.getHTTPRequestBytes();
		Assert.assertNull(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestGibberish() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(string2bytes("abcde"));
		final byte[] request = cache.getHTTPRequestBytes();
		Assert.assertNull(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestPartialRequestLine() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(string2bytes(validFirstPart));
		final byte[] request = cache.getHTTPRequestBytes();
		Assert.assertNull(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestFullRequestLineOnly() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(string2bytes("GET / HTTP/1.1\r\n"));
		final byte[] request = cache.getHTTPRequestBytes();
		Assert.assertNull(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestHeaderSectionNotEnds() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(string2bytes(unfinishedRequest));
		final byte[] request = cache.getHTTPRequestBytes();
		Assert.assertNull(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestFullRequestNoHeaderNoBody() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] request = string2bytes("GET / HTTP/1.1\r\n\r\n");
		cache.addBytes(request);
		final int cacheLength = cache.getCacheLength();
		final byte[] result = cache.getHTTPRequestBytes();
		Assert.assertArrayEquals(request, result);
		Assert.assertTrue(cache.getCacheLength() >= 0 && cache.getCacheLength() < cacheLength);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestFullRequestNoBody() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] request = string2bytes(noBodyRequest);
		cache.addBytes(request);
		final int cacheLength = cache.getCacheLength();
		final byte[] result = cache.getHTTPRequestBytes();
		Assert.assertArrayEquals(request, result);
		Assert.assertTrue(cache.getCacheLength() >= 0 && cache.getCacheLength() < cacheLength);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestFullRequestWithBody() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] requestPart1 = string2bytes(validFirstPart);
		cache.addBytes(requestPart1);
		final byte[] requestPart2 = string2bytes(validSecondPart);
		cache.addBytes(requestPart2);
		final int cacheLength = cache.getCacheLength();
		final byte[] result = cache.getHTTPRequestBytes();
		final byte[] expected = new byte[requestPart1.length + requestPart2.length];
		System.arraycopy(requestPart1, 0, expected, 0, requestPart1.length);
		System.arraycopy(requestPart2, 0, expected, requestPart1.length, requestPart2.length);
		Assert.assertArrayEquals(expected, result);
		Assert.assertTrue(cache.getCacheLength() >= 0 && cache.getCacheLength() < cacheLength);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestFullRequestWithBodyAndSomeGiberish() {
		Assert.assertEquals(0, cache.getCacheLength());
		cache.addBytes(string2bytes("abcde"));
		final byte[] requestPart1 = string2bytes(validFirstPart);
		cache.addBytes(requestPart1);
		final byte[] requestPart2 = string2bytes(validSecondPart);
		cache.addBytes(requestPart2);
		final int cacheLength = cache.getCacheLength();
		final byte[] result = cache.getHTTPRequestBytes();
		final byte[] expected = new byte[requestPart1.length + requestPart2.length];
		System.arraycopy(requestPart1, 0, expected, 0, requestPart1.length);
		System.arraycopy(requestPart2, 0, expected, requestPart1.length, requestPart2.length);
		Assert.assertArrayEquals(expected, result);
		Assert.assertTrue(cache.getCacheLength() >= 0 && cache.getCacheLength() < cacheLength);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestTwoFullRequestsWithBody() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] requestPart1 = string2bytes(validFirstPart);
		cache.addBytes(requestPart1);
		final byte[] requestPart2 = string2bytes(validSecondPart);
		cache.addBytes(requestPart2);
		final int cacheLength = cache.getCacheLength();
		byte[] result = cache.getHTTPRequestBytes();
		final byte[] expected = new byte[requestPart1.length + requestPart2.length];
		System.arraycopy(requestPart1, 0, expected, 0, requestPart1.length);
		System.arraycopy(requestPart2, 0, expected, requestPart1.length, requestPart2.length);
		Assert.assertArrayEquals(expected, result);
		Assert.assertTrue(cache.getCacheLength() >= 0 && cache.getCacheLength() < cacheLength);
		
		// second request
		cache.addBytes(requestPart1);
		cache.addBytes(requestPart2);
		result = cache.getHTTPRequestBytes();
		Assert.assertArrayEquals(expected, result);
		Assert.assertTrue(cache.getCacheLength() >= 0 && cache.getCacheLength() < cacheLength);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestUnfinishedRequestAndGoodRequestAfter() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] requestPart1 = string2bytes(validFirstPart);
		final byte[] requestPart2 = string2bytes(validSecondPart);

		cache.addBytes(requestPart1);
		cache.addBytes(Arrays.copyOf(requestPart2, requestPart2.length - 5)); // 5 missing bytes from request
		cache.addBytes(requestPart1);
		cache.addBytes(requestPart2);

		byte[] result = cache.getHTTPRequestBytes();
		final byte[] expected = new byte[requestPart1.length + requestPart2.length];
		System.arraycopy(requestPart1, 0, expected, 0, requestPart1.length);
		System.arraycopy(requestPart2, 0, expected, requestPart1.length, requestPart2.length);
		System.arraycopy(string2bytes("POST "), 0, expected, expected.length - 5, 5); // the last 5 bytes comes from the start of the second request
		Assert.assertArrayEquals(expected, result);

		// second request
		result = cache.getHTTPRequestBytes();
		final byte[] expected2 = new byte[requestPart1.length + requestPart2.length];
		System.arraycopy(requestPart1, 0, expected2, 0, requestPart1.length);
		System.arraycopy(requestPart2, 0, expected2, requestPart1.length, requestPart2.length);
		Assert.assertArrayEquals(expected2, result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getHTTPRequestFullRequestWithUTF8Body() {
		Assert.assertEquals(0, cache.getCacheLength());
		final byte[] requestPart1 = string2bytes(validFirstPart);
		cache.addBytes(requestPart1);
		final byte[] requestPart2 = string2bytes(validSecondPartNoBody);
		cache.addBytes(requestPart2);
		final byte[] utf8BodyBytes = utf8Body.getBytes(StandardCharsets.UTF_8);
		cache.addBytes(utf8BodyBytes);
		final byte[] result = cache.getHTTPRequestBytes();
		final byte[] expected = new byte[requestPart1.length + requestPart2.length + utf8BodyBytes.length];
		System.arraycopy(requestPart1, 0, expected, 0, requestPart1.length);
		System.arraycopy(requestPart2, 0, expected, requestPart1.length, requestPart2.length);
		System.arraycopy(utf8BodyBytes, 0, expected, requestPart1.length + requestPart2.length, utf8BodyBytes.length);
		Assert.assertArrayEquals(expected, result);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private byte[] string2bytes(final String str) {
		return str.getBytes(StandardCharsets.ISO_8859_1);
	}
}