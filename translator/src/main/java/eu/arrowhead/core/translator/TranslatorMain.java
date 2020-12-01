package eu.arrowhead.core.translator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan(CommonConstants.BASE_PACKAGE)
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
@EnableScheduling
public class TranslatorMain {

    //=================================================================================================
    // members
    //-------------------------------------------------------------------------------------------------
    public static void main(final String[] args) {
        SpringApplication.run(TranslatorMain.class, args);

    }
}
