package eu.arrowhead.core.gams;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = CommonConstants.BASE_PACKAGE)
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class,
        repositoryImplementationPostfix = "Impl")
@EnableSwagger2
public class GamsMain
{

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static void main(final String[] args) {
        SpringApplication.run(GamsMain.class, args);
    }
}
