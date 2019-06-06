package eu.arrowhead.core.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import eu.arrowhead.common.CommonConstants;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan (CommonConstants.BASE_PACKAGE)
@EntityScan (CommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories (CommonConstants.DATABASE_REPOSITORY_PACKAGE)
@EnableSwagger2
public class ServiceRegistryMain {

	public static void main(final String[] args) {
		SpringApplication.run(ServiceRegistryMain.class, args);
	}
}