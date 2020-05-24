package eu.arrowhead.core.certificate_authority;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CertificateAuthorityServiceTestContext {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public CertificateAuthorityService mockCertificateAuthorityService() {
		return Mockito.mock(CertificateAuthorityService.class);
	}
}
