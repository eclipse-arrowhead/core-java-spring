package eu.arrowhead.common.token;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

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
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.zRR8VyqFDZZ5KSByd5jnRKQEHSAmOce99H02ABELKAMnlz-9OekZnJvsIxPP28Vv9CVtf1r_AACylBmVkZtuUoQE5PLlkiYq128rYCiYx7Aa5oCU2hRQIMfW_9KhUDMG5KdF9_9ZMqvt1ZfrP4tRc0b0I_DA20aZpH2-TR60JuX8eISKon131Rif3TmNpqXI6FHsAGvh4h4a9vt2nwCCTyCdqI2-9NJ8Gz-bze5cFEccADtnvcEPA8J7qDqq_bQWAFAECY8YUDKDrXmYbsyXqJ2LXhH_6aBWpq6PxgOq2g9HkHYR1M404sseMDY3rFpMll8pQjikcop0uAi-NE_l1g.EHG0BhIMqdEXJwOONU5CQw.NvgiIqYbD0ML_d9hcahUU3jFLWIMar8RdwQoflQolv-vVoXwqENRnKjQxujLbigM4buzPylLFF7dT8y1-x_SB-H1Eyqj_QSp1TFUAh3t0_8XWUfPztFh71GjvTPzhyoBjEqliayfIO_qBmsn9iI_4p0j7dmKOhj6ICjt0TIygwTUwGA2OK06kNE9YJeYkExE5gYJDvor8kFeBPZXe1Bhfw-8BBygMCrJ-Ej7orjCNpV0evoQ9b7AvQeATlwV7pWj6tn34uvId9X369ySzfyEf4WBaP4f48-ldbj3untQhRj8gKXvWlOXmQqr_DTrra8wPkAJXXVfeT5f3IknVCh2xkcthtPf4g2M9DyGRIGivbkwsQLNRzNSPszOnxg0OUvXoN3oOhtOdVBNcNdjF1C4kiht-krvUGvkf1W1aNc0EX35uIBkL-Z__eUeQwipsyz6r5FrKlk-54yFK0wDavFLy9790fF46m5U1RpaI43Wemt77iuUP9Mn_vV7gL7dEOgjej1Vcumwt_gSTSS5a-qttEwchb3p4sQfekGRxS5hz4ecQs5dbme8RoK9sFqVMyu3e2K7S9iXbHhw0vhWf2hjPSegRWwVF04ojcN1BQnIELfFnJ3QGzA82nz6t31b72_0AfKxw1A6j7r5JW0iuqMroEnC6OdD_7PJcHx0rF89qbS2Z2qahO4MVh9ZB6w2N8oAFloWtQV2SGkQQ6Dp-d6BS49on5utFR4PjdIXs0z0ETk.jasfCemk7jsQR5NFAvIVJ47r4XRBP9uTi90I1_yMg90";

		this.mockMvc.perform(get("/authorization/echo?token=" + token)
					.secure(true)
					.with(x509("certificates/provider.pem")))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTokenSecurityFilterOk() throws Exception {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.zRR8VyqFDZZ5KSByd5jnRKQEHSAmOce99H02ABELKAMnlz-9OekZnJvsIxPP28Vv9CVtf1r_AACylBmVkZtuUoQE5PLlkiYq128rYCiYx7Aa5oCU2hRQIMfW_9KhUDMG5KdF9_9ZMqvt1ZfrP4tRc0b0I_DA20aZpH2-TR60JuX8eISKon131Rif3TmNpqXI6FHsAGvh4h4a9vt2nwCCTyCdqI2-9NJ8Gz-bze5cFEccADtnvcEPA8J7qDqq_bQWAFAECY8YUDKDrXmYbsyXqJ2LXhH_6aBWpq6PxgOq2g9HkHYR1M404sseMDY3rFpMll8pQjikcop0uAi-NE_l1g.EHG0BhIMqdEXJwOONU5CQw.NvgiIqYbD0ML_d9hcahUU3jFLWIMar8RdwQoflQolv-vVoXwqENRnKjQxujLbigM4buzPylLFF7dT8y1-x_SB-H1Eyqj_QSp1TFUAh3t0_8XWUfPztFh71GjvTPzhyoBjEqliayfIO_qBmsn9iI_4p0j7dmKOhj6ICjt0TIygwTUwGA2OK06kNE9YJeYkExE5gYJDvor8kFeBPZXe1Bhfw-8BBygMCrJ-Ej7orjCNpV0evoQ9b7AvQeATlwV7pWj6tn34uvId9X369ySzfyEf4WBaP4f48-ldbj3untQhRj8gKXvWlOXmQqr_DTrra8wPkAJXXVfeT5f3IknVCh2xkcthtPf4g2M9DyGRIGivbkwsQLNRzNSPszOnxg0OUvXoN3oOhtOdVBNcNdjF1C4kiht-krvUGvkf1W1aNc0EX35uIBkL-Z__eUeQwipsyz6r5FrKlk-54yFK0wDavFLy9790fF46m5U1RpaI43Wemt77iuUP9Mn_vV7gL7dEOgjej1Vcumwt_gSTSS5a-qttEwchb3p4sQfekGRxS5hz4ecQs5dbme8RoK9sFqVMyu3e2K7S9iXbHhw0vhWf2hjPSegRWwVF04ojcN1BQnIELfFnJ3QGzA82nz6t31b72_0AfKxw1A6j7r5JW0iuqMroEnC6OdD_7PJcHx0rF89qbS2Z2qahO4MVh9ZB6w2N8oAFloWtQV2SGkQQ6Dp-d6BS49on5utFR4PjdIXs0z0ETk.jasfCemk7jsQR5NFAvIVJ47r4XRBP9uTi90I1_yMg90";

		this.mockMvc.perform(get("/authorization/echo?token=" + token)
					.secure(true)
					.with(x509("certificates/consumer.pem")))
					.andExpect(status().isOk());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public TokenSecurityFilter getTokenSecurityFilter() throws Exception {
		final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		final PublicKey authPublicKey = Utilities.getPublicKeyFromPEMFile(is);

		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/provider.p12"), "123456".toCharArray());
		final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, "123456");

		return new TokenSecurityFilter(providerPrivateKey, authPublicKey) {};
	}
}