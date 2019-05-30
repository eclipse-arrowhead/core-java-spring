package eu.arrowhead.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class UtilitiesTest {
	
	@Test
	public void testGetCertCNFromSubjectOk() {
		String result = Utilities.getCertCNFromSubject("cn=abc.def.gh");
		Assert.assertEquals("abc.def.gh", result);
	}
	
	@Test
	public void testGetCertCNFromSubjectNotOk() {
		String result = Utilities.getCertCNFromSubject("abc.def.gh");
		Assert.assertNull(result);
	}
	
	@Test
	public void testGetCertCNFromSubjectNullParameter() {
		String result = Utilities.getCertCNFromSubject(null);
		Assert.assertNull(result);
	}
}