package eu.arrowhead.core.serviceregistry;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.exception.DataNotFoundException;

@Component
public class ServiceRegistryApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private CommonDBService commonDBService; 


	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");
		if (!isOwnCloudRegistered()) {
			registerOwnCloud(event.getApplicationContext());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isOwnCloudRegistered() {
		logger.debug("isOwnCloudRegistered started...");
		try {
			commonDBService.getOwnCloud(sslProperties.isSslEnabled());
			return true;
		} catch (final DataNotFoundException ex) {
			return false;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void registerOwnCloud(final ApplicationContext appContext) {
		logger.debug("registerOwnCloud started...");
		String name = Defaults.DEFAULT_OWN_CLOUD_NAME;
		String operator = Defaults.DEFAULT_OWN_CLOUD_OPERATOR;
		
		if (sslProperties.isSslEnabled()) {
			@SuppressWarnings("unchecked")
			final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
			final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
			final String[] serverFields = serverCN.split("\\.");
			name = serverFields[1];
			operator = serverFields[2];
		}
		
		commonDBService.insertOwnCloudWithoutGatekeeper(operator, name, sslProperties.isSslEnabled());
		logger.info("{}.{} own cloud is registered in {} mode.", name, operator, getModeString());
	}
}