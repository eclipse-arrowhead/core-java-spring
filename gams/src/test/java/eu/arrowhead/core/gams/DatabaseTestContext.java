package eu.arrowhead.core.gams;


import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import eu.arrowhead.core.gams.GamsApplicationInitListener;
import eu.arrowhead.core.gams.GamsMain;
import eu.arrowhead.core.gams.mock.GamsTestContext;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootTest
@PropertySource(value = "classpath:application.properties")
@EnableAutoConfiguration
@EntityScan(basePackages = CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class,
        repositoryImplementationPostfix = "Impl")
@EnableTransactionManagement
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GamsMain.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GamsApplicationInitListener.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GamsTestContext.class),
        })
public class DatabaseTestContext {

}

