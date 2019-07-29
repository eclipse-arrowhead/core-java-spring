package eu.arrowhead.core.authorization;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.authorization.token.TokenGenerationService;

@Configuration
public class AuthorizationServiceTestContext {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public TokenGenerationService mockTokenGenerationService() {
		return Mockito.mock(TokenGenerationService.class);
	}
}