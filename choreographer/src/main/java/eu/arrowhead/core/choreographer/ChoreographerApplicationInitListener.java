/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.exception.ArrowheadException;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;
import org.springframework.web.util.UriComponents;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.jms.ConnectionFactory;
import java.util.List;
import java.util.Map;

@Component
public class ChoreographerApplicationInitListener extends ApplicationInitListener {

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {
        logger.debug("customInit started...");

        if (standaloneMode) {
            return;
        }

        final ApplicationContext appContext = event.getApplicationContext();
        @SuppressWarnings("unchecked")
        final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);

        final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        final UriComponents queryByServiceDefinitionList = createQueryByServiceDefinitionList(scheme);

        context.put(CoreCommonConstants.SR_QUERY_BY_SERVICE_DEFINITION_LIST_URI, queryByServiceDefinitionList);
    }

    private UriComponents createQueryByServiceDefinitionList(final String scheme) {
        logger.debug("createQuerySystemByServiceDefinitionList started...");

        final String registryUriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SERVICE_DEFINITION_LIST_URI;

        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registryUriStr);
    }

    @Bean
    public JmsListenerContainerFactory<?> getFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer, ExampleErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Service
    public class ExampleErrorHandler implements ErrorHandler {

        @Override
        public void handleError(Throwable throwable) {
            System.out.println("Error happened during Workflow Choreography.");
        }
    }

    @Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
        return List.of(CoreSystemService.ORCHESTRATION_SERVICE);
    }
}