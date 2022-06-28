package eu.arrowhead.common.database.service;

import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EntityScan(basePackages = {"eu.arrowhead.common.database.entity"})
@EnableJpaRepositories(
        basePackages = {"eu.arrowhead.common.database.repository"},
        repositoryBaseClass = RefreshableRepositoryImpl.class)
public class DatabaseTestContext {
    public DatabaseTestContext() { super(); }
}
