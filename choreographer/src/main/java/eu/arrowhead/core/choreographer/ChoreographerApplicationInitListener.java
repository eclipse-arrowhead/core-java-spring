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

import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;

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
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionErrorHandler;
import eu.arrowhead.core.choreographer.executor.ExecutorPrioritizationStrategy;
import eu.arrowhead.core.choreographer.executor.RandomExecutorPrioritizationStrategy;
import eu.arrowhead.core.choreographer.graph.EdgeDestroyerStepGraphNormalizer;
import eu.arrowhead.core.choreographer.graph.KahnAlgorithmStepGraphCircleDetector;
import eu.arrowhead.core.choreographer.graph.StepGraphCircleDetector;
import eu.arrowhead.core.choreographer.graph.StepGraphNormalizer;

@Component
public class ChoreographerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	private static final String TYPE_ID_PROPERTY_NAME = "_type";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public JmsListenerContainerFactory<?> getJMSFactory(final ConnectionFactory connectionFactory, final DefaultJmsListenerContainerFactoryConfigurer configurer, final ChoreographerSessionErrorHandler errorHandler) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler);
        
        return factory;
    }

    //-------------------------------------------------------------------------------------------------
	@Bean
    public MessageConverter jacksonJmsMessageConverter() {
        final MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY_NAME);
        return converter;
    }
    
    //-------------------------------------------------------------------------------------------------
	@Bean
    public StepGraphCircleDetector getStepGraphCircleDetector() {
    	return new KahnAlgorithmStepGraphCircleDetector();
    }
    
    //-------------------------------------------------------------------------------------------------
	@Bean
    public StepGraphNormalizer getStepGraphNormalizer() {
    	return new EdgeDestroyerStepGraphNormalizer();
    }
	
    //-------------------------------------------------------------------------------------------------
    @Bean
    public ExecutorPrioritizationStrategy getExecutorPrioritizationStrategy() {
    	return new RandomExecutorPrioritizationStrategy();
    }
	
	//=================================================================================================
	// assistant methods

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
        context.put(CoreCommonConstants.SR_MULTI_QUERY_URI, createMultiQueryRegistryUri(scheme));
        context.put(CoreCommonConstants.SR_REGISTER_SYSTEM_URI, createRegisterSystemUri(scheme));
        context.put(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI, createUnregisterSystemUri(scheme));
    }
    
    //-------------------------------------------------------------------------------------------------
	@Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
        return List.of(CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE);
    }

    //=================================================================================================
    // assistant methods
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents createMultiQueryRegistryUri(final String scheme) {
        logger.debug("createMultiQueryRegistryUri started...");

        final String uriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI;
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), uriStr);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents createRegisterSystemUri(final String scheme) {
        logger.debug("createQuerySystemByServiceDefinitionList started...");

        final String uriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_SYSTEM_URI;
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), uriStr);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents createUnregisterSystemUri(final String scheme) {
        logger.debug("createUnregisterSystemUri started...");

        final String uriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI;
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), uriStr);
    }
}