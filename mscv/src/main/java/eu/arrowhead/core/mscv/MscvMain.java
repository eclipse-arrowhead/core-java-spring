package eu.arrowhead.core.mscv;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import eu.arrowhead.common.quartz.uricrawler.UriCrawlerTask;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "eu.arrowhead.common.quartz.uricrawler.*")
})
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
public class MscvMain {

     //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static void main(final String[] args) {
        SpringApplication.run(MscvMain.class, args);
    }

}
