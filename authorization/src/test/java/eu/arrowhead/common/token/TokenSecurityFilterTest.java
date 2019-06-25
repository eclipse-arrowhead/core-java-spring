package eu.arrowhead.common.token;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.core.authorization.AuthorizationMain;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@AutoConfigureMockMvc
public class TokenSecurityFilterTest {
	
	//=================================================================================================
	// members

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	private MockMvc mockMvc;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() throws Exception {
		assumeTrue(secure);
		
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(getTokenSecurityFilter())
									  .build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTokenSecurityFilterNoToken() throws Exception {
		this.mockMvc.perform(get("/authorization/echo")
					.secure(true)
					.with(x509("certificates/consumer.pem")))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTokenSecurityFilterEmptyToken() throws Exception {
		this.mockMvc.perform(get("/authorization/echo?token=")
					.secure(true)
					.with(x509("certificates/consumer.pem")))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTokenSecurityFilterSomebodyElsesToken() throws Exception {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.CcwXpakkpOwganOeZLQcmeB3WZ-UZxZ7oZ1MbunWKetGty7NoQJRwfMBpD9HeK58-2glvRXMnirwRi7x-HeIj2FA4wKM7ptRvQQirtM4xUsUMyPSVgRWeRuvVN8UUf4L_O_M6fi0HgDKPE5aCq1x_mkCULvSo9SiVyePTddByWscYBRn-kHIZK9va_xt9GR9DTlAw-ooqv04z_rAo83iTuNZLob_LY6kYDvzcfHxE38vnPKDCzks6AHXLk0wU5XEqmiDBQmUtlc-Gzo43P4LR-ZopT5p0WUajP5HxPwrdGw9K88wSqWAYi3zKh2REd8NQCmnVobwg9VFXpwmW9S1MA.uDMJ5zVdMoa-Qa_mE-2vMQ.a56lUBtFO8R5AZS0FNIK_ABDXeX8nZWfSJra3fqlwKc9i8euMC9drd6cMnU9NUiRmMm7YOB_XQ97FrHvz6VkzljbVJIYxNO-xF-6p6h2hj2MYvO11Wnk8r9s8gh1JFbP2iU07ceZXCxDYlaRUNFc69Fn-ZRx8NS4WhDlqQtg5k6uw-qS5J5uOp-5zUtksRlz1Minda5oky-FHBMJD3aiUGrAFhziGV8ftax-89krnYEgqSJcUNUlm1rvLXdaaGBFHQ1KE8iXkS8ujgCexvwzXlBjLcqx1_gMJPojC-xZ10u9_7q4VVwpCiVNSrIb0m764vr4bjSs8wEQGVN5ACxADAh3flY2z-yrvfcHCZE4wbYSZeesRNkJ3EhETAqoPqM2yZx2ztY5u3dHmexQtFeSXbZalGUTbwu9HPUbJQJUR-4ux3dSeQxP6HQg_cvDMTGTb4LMZy08i6Nx8pbBXX9wZojdL38P20SQuFDYEcyED7PQtMdu1Ov4No9iXelhxqBrdldN461_AM-tPbdzQrXi4c3Iv8V-S-nrUTYVXIJB3vw5OL1ZfP0Bu3kD0_3GuJEfGluWJDCP2j-eic67LjGpfu7f6H5ik99t5lRhJovxvqo4dDZLuVCJlxgY20azSxICPcldnLxsFCSzG8CYrazPvMktBE1mmxkpA8NUPGsDcEM5XtSjHvpyafwaVVwETCRFCdQelUcGJmVv_ZCzXo7tqeqmAUYDdYlitb1ZllWjCmE.iE76xWS5HQWamApKUT3WBdq1rqBn6qrmStAz6zj2a8U";

		this.mockMvc.perform(get("/authorization/echo?token=" + token)
					.secure(true)
					.with(x509("certificates/provider.pem")))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTokenSecurityFilterOk() throws Exception {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.CcwXpakkpOwganOeZLQcmeB3WZ-UZxZ7oZ1MbunWKetGty7NoQJRwfMBpD9HeK58-2glvRXMnirwRi7x-HeIj2FA4wKM7ptRvQQirtM4xUsUMyPSVgRWeRuvVN8UUf4L_O_M6fi0HgDKPE5aCq1x_mkCULvSo9SiVyePTddByWscYBRn-kHIZK9va_xt9GR9DTlAw-ooqv04z_rAo83iTuNZLob_LY6kYDvzcfHxE38vnPKDCzks6AHXLk0wU5XEqmiDBQmUtlc-Gzo43P4LR-ZopT5p0WUajP5HxPwrdGw9K88wSqWAYi3zKh2REd8NQCmnVobwg9VFXpwmW9S1MA.uDMJ5zVdMoa-Qa_mE-2vMQ.a56lUBtFO8R5AZS0FNIK_ABDXeX8nZWfSJra3fqlwKc9i8euMC9drd6cMnU9NUiRmMm7YOB_XQ97FrHvz6VkzljbVJIYxNO-xF-6p6h2hj2MYvO11Wnk8r9s8gh1JFbP2iU07ceZXCxDYlaRUNFc69Fn-ZRx8NS4WhDlqQtg5k6uw-qS5J5uOp-5zUtksRlz1Minda5oky-FHBMJD3aiUGrAFhziGV8ftax-89krnYEgqSJcUNUlm1rvLXdaaGBFHQ1KE8iXkS8ujgCexvwzXlBjLcqx1_gMJPojC-xZ10u9_7q4VVwpCiVNSrIb0m764vr4bjSs8wEQGVN5ACxADAh3flY2z-yrvfcHCZE4wbYSZeesRNkJ3EhETAqoPqM2yZx2ztY5u3dHmexQtFeSXbZalGUTbwu9HPUbJQJUR-4ux3dSeQxP6HQg_cvDMTGTb4LMZy08i6Nx8pbBXX9wZojdL38P20SQuFDYEcyED7PQtMdu1Ov4No9iXelhxqBrdldN461_AM-tPbdzQrXi4c3Iv8V-S-nrUTYVXIJB3vw5OL1ZfP0Bu3kD0_3GuJEfGluWJDCP2j-eic67LjGpfu7f6H5ik99t5lRhJovxvqo4dDZLuVCJlxgY20azSxICPcldnLxsFCSzG8CYrazPvMktBE1mmxkpA8NUPGsDcEM5XtSjHvpyafwaVVwETCRFCdQelUcGJmVv_ZCzXo7tqeqmAUYDdYlitb1ZllWjCmE.iE76xWS5HQWamApKUT3WBdq1rqBn6qrmStAz6zj2a8U";

		this.mockMvc.perform(get("/authorization/echo?token=" + token)
					.secure(true)
					.with(x509("certificates/consumer.pem")))
					.andExpect(status().isOk());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public TokenSecurityFilter getTokenSecurityFilter() throws Exception {
		final PublicKey authPublicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/provider.p12"), "123456".toCharArray());
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, "123456");

		return new TestTokenSecurityFilter(providerPrivateKey, authPublicKey);
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	private static class TestTokenSecurityFilter extends TokenSecurityFilter {
		
		//=================================================================================================
		// methods

		//-------------------------------------------------------------------------------------------------
		public TestTokenSecurityFilter(final PrivateKey myPrivateKey, final PublicKey authorizationPublicKey) {
			super(myPrivateKey, authorizationPublicKey);
		}
	}
}