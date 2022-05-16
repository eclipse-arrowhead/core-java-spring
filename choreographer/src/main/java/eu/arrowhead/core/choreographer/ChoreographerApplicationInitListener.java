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
import java.util.ServiceConfigurationError;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
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
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionErrorHandler;
import eu.arrowhead.core.choreographer.executor.ExecutorMeasurementStrategy;
import eu.arrowhead.core.choreographer.executor.ExecutorPrioritizationStrategy;
import eu.arrowhead.core.choreographer.executor.MinimalDependencyExecutorPrioritizationStrategy;
import eu.arrowhead.core.choreographer.executor.WeightedExecutorMeasurementStrategy;
import eu.arrowhead.core.choreographer.graph.EdgeDestroyerStepGraphNormalizer;
import eu.arrowhead.core.choreographer.graph.KahnAlgorithmStepGraphCircleDetector;
import eu.arrowhead.core.choreographer.graph.StepGraphCircleDetector;
import eu.arrowhead.core.choreographer.graph.StepGraphNormalizer;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;
import eu.arrowhead.core.choreographer.service.ChoreographerExecutorService;

@Component
public class ChoreographerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	private static final String TYPE_ID_PROPERTY_NAME = "_type";
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Autowired
	private ChoreographerExecutorService choreographerExecutorService;
	
	@Autowired
	private ChoreographerDriver driver;
	
	@Value(CoreCommonConstants.$CHOREOGRAPHER_IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(final ConnectionFactory connectionFactory, final DefaultJmsListenerContainerFactoryConfigurer configurer, final ChoreographerSessionErrorHandler errorHandler) {
		final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setErrorHandler(errorHandler);
		configurer.configure(factory, connectionFactory);
		factory.setSessionTransacted(false);
        
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
    	return new MinimalDependencyExecutorPrioritizationStrategy();
    }
    
    //-------------------------------------------------------------------------------------------------
	@Bean
	public ExecutorMeasurementStrategy getExecutorMeasurementStrategy() {
		return new WeightedExecutorMeasurementStrategy();
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
        context.put(CoreCommonConstants.SR_PULL_CONFIG_URI, createPullSRConfigUri(scheme));
        context.put(CoreCommonConstants.SR_MULTI_QUERY_URI, createMultiQueryRegistryUri(scheme));
        context.put(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI, createQueryRegistryBySystemUri(scheme));
        context.put(CoreCommonConstants.SR_REGISTER_SYSTEM_URI, createRegisterSystemUri(scheme));
        context.put(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI, createUnregisterSystemUri(scheme));
        
        configureServiceRegistryDependentVerifiers();
    }
    
    //-------------------------------------------------------------------------------------------------
	@Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		if (gateKeeperIsPresent) {
			return List.of(CoreSystemService.ORCHESTRATION_BY_PROXY_SERVICE, CoreSystemService.GATEKEEPER_MULTI_GLOBAL_SERVICE_DISCOVERY, CoreSystemService.GATEWAY_CLOSE_SESSIONS_SERVICE); 
		}
		
		return List.of(CoreSystemService.ORCHESTRATION_BY_PROXY_SERVICE);
    }

    //=================================================================================================
    // assistant methods
	
	//-------------------------------------------------------------------------------------------------
    private UriComponents createPullSRConfigUri(final String scheme) {
        logger.debug("createPullSRConfigUri started...");

        final String uriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_PULL_CONFIG_URI;
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), uriStr);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents createMultiQueryRegistryUri(final String scheme) {
        logger.debug("createMultiQueryRegistryUri started...");

        final String uriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI;
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), uriStr);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents createQueryRegistryBySystemUri(final String scheme) {
        logger.debug("createQueryRegistryBySystemUri started...");

        final String uriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI;
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
    
    //-------------------------------------------------------------------------------------------------
    private void configureServiceRegistryDependentVerifiers() {
    	logger.debug("configureNetworkAddressVerifier started...");
    	
    	try {
    		final Map<String,String> srConfig = driver.pullServiceRegistryConfig().getMap();
    		networkAddressVerifier.configure(Boolean.valueOf(srConfig.get(CoreCommonConstants.ALLOW_SELF_ADDRESSING)),
    										 Boolean.valueOf(srConfig.get(CoreCommonConstants.ALLOW_NON_ROUTABLE_ADDRESSING)));
    		
    		choreographerExecutorService.configure(Boolean.valueOf(srConfig.get(CoreCommonConstants.USE_STRICT_SERVICE_DEFINITION_VERIFIER)));
    	} catch (final Exception ex) {
    		logger.error(ex.getMessage());
    		logger.debug(ex);
			throw new ServiceConfigurationError("ServiceRegistry dependent verifiers configuration failure");
		}
    }
}