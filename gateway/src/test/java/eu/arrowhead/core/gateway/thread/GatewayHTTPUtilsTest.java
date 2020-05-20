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
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsStartOfAHttpRequestNullParam() {
		final Answer answer = GatewayHTTPUtils.isStartOfAHttpRequest(null);
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
}