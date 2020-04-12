package eu.arrowhead.core.msvc;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = CommonConstants.BASE_PACKAGE)
@EntityScan(CommonConstants.BASE_PACKAGE)
@EnableJpaRepositories(basePackages = CommonConstants.BASE_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
public class MsvcMain {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static void main(final String[] args) {
        SpringApplication.run(MsvcMain.class, args);
    }
}
