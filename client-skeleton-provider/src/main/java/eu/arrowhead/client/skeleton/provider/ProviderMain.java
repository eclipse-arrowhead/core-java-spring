package eu.arrowhead.client.skeleton.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.client.skeleton.common.ArrowheadService;

@SpringBootApplication
@ComponentScan(basePackages = "eu.arrowhead")
public class ProviderMain {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ProviderMain.class, args);
		context.getBean(ArrowheadService.class).echoServiceRegistry();
	}
	
	
}
