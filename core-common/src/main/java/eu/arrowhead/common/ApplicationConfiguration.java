package eu.arrowhead.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ApplicationConfiguration {

    //-------------------------------------------------------------------------------------------------
    @Bean(CommonConstants.ARROWHEAD_CONTEXT)
    public Map<String, Object> getArrowheadContext() {
        return new ConcurrentHashMap<>();
    }

}
