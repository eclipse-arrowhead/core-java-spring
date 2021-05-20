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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.Answer;

@RunWith(SpringRunner.class)
public class GatewayHTTPUtilsTest {
	
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
	
	private static final String chunkedRequest = "POST / HTTP/1.1\r\n" + 
												 "Accept: text/plain\r\n" + 
												 "Accept: application/json\r\n" + 
												 "Content-Type: application/json\r\n" + 
												 "Transfer-Encoding: chunked\r\n" +
												 "User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" +
												 "\r\n" + 
												 "10" +
												 "abcdefghij";
	
	private static final String notChunkedRequest = "POST / HTTP/1.1\r\n" + 
			 										"Accept: text/plain\r\n" + 
			 										"Accept: application/json\r\n" + 
			 										"Content-Type: application/json\r\n" + 
			 										"Transfer-Encoding: identity\r\n" +
			 										"Content-Length: 10\r\n" +
			 										"User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" +
			 										"\r\n" + 
			 										"abcdefghij";

	private static final String unfinishedRequest = "POST / HTTP/1.1\r\n" + 
			 										"Accept: text/plain\r\n" + 
			 										"Accept: application/json\r\n" + 
			 										"Content-Type: application/json\r\n" + 
			 										"Transfer-Encoding:";
	
	private static final String trickyRequest = "POST / HTTP/1.1\r\n" + 
												"Accept: text/plain\r\n" + 
												"Accept: application/json\r\n" + 
												"Content-Type: application/json\r\n" + 
												"Content-Length: 28\r\n" +
												"User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" +
												"\r\n" + 
												"Transfer-Encoding: chunked\r\n";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestNullParam() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest((String)null);
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestNullGibberish() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest("abcdedfg");
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestValidSimple() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest("GET / HTTP/1.1\r\n");
		Assert.assertEquals(Answer.YES, answer);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestInvalidCaseSensitive() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest("get / HTTP/1.1\r\n");
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestInvalidVersion() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest("OPTIONS \"*\" HTTP/121\r\n");
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestCanBe() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest(validFirstPart);
		Assert.assertEquals(Answer.CAN_BE, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestValidDifficult() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest(validFirstPart + validSecondPart);
		Assert.assertEquals(Answer.YES, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedAHttpRequestNullParam() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest((String) null);
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedAHttpRequestGibberish() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest("abcdedfg");
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestCanBe() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest(validFirstPart);
		Assert.assertEquals(Answer.CAN_BE, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestNoEnd1() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest("GET / HTTP/1.1\r\n");
		Assert.assertEquals(Answer.CAN_BE, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestNoEnd2() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest(unfinishedRequest);
		Assert.assertEquals(Answer.CAN_BE, answer);
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestNoHeaders() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest("GET / HTTP/1.1\r\n\r\n");
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestNoTransferEncodingHeader() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest(validFirstPart + validSecondPart);
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestNotChunkedTransferEncodingHeader() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest(notChunkedRequest);
		Assert.assertEquals(Answer.NO, answer);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestChunkedTransferEncodingHeader() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest(chunkedRequest);
		Assert.assertEquals(Answer.YES, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsChunkedHttpRequestNoSearchInBody() {
		final Answer answer = GatewayHTTPUtils.isChunkedHttpRequest(trickyRequest);
		Assert.assertEquals(Answer.NO, answer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIndicesOfHttpRequestLineBoundariesNullParam() {
		final int[] boundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries(null);
		Assert.assertNull(boundaries);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIndicesOfHttpRequestLineBoundariesNotARequest() {
		final int[] boundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries("not a request");
		Assert.assertNull(boundaries);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIndicesOfHttpRequestLineBoundariesPartialRequestLine() {
		final int[] boundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries(validFirstPart);
		Assert.assertNull(boundaries);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIndicesOfHttpRequestLineBoundariesFullRequestLine() {
		final int[] boundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries("GET / HTTP/1.1\r\n");
		Assert.assertNotNull(boundaries);
		Assert.assertEquals(0, boundaries[0]);
		Assert.assertEquals(15, boundaries[1]);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIndicesOfHttpRequestLineBoundariesFullRequestLineAndSomeore() {
		final int[] boundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries("abcd GET / HTTP/1.1\r\nAccept: application/json\r\n\r\n");
		Assert.assertNotNull(boundaries);
		Assert.assertEquals(5, boundaries[0]);
		Assert.assertEquals(20, boundaries[1]);
	}
}