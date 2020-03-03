package eu.arrowhead.core.qos;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;

@Component
public class QoSMonitorApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		final ApplicationContext appContext = event.getApplicationContext();
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);

		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;

		final UriComponents queryAll = createQueryAllUri(scheme);
		context.put(CoreCommonConstants.SR_QUERY_ALL, queryAll);
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryAllUri(final String scheme) {
		logger.debug("createQueryAllUri started...");

		final String registyUriStr = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_ALL_URI;

		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registyUriStr);
	}
}