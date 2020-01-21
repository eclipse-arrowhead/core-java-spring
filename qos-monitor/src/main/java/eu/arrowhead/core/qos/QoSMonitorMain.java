package eu.arrowhead.core.qos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;

@SpringBootApplication
@ComponentScan (CommonConstants.BASE_PACKAGE)
@EntityScan (CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories (basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
public class QoSMonitorMain {
	
	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		SpringApplication.run(QoSMonitorMain.class, args);
	}
}