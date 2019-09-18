package eu.arrowhead.common;

import java.util.ServiceConfigurationError;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.core.CoreSystem;

@Component
public class CoreSystemRegistrationProperties {

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(CoreSystemRegistrationProperties.class);

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String coreSystemName;
	
	private CoreSystem coreSystem;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_ADDRESS_WD)
	private String serviceRegistryAddress;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PORT_WD)
	private int serviceRegistryPort;

	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String coreSystemAddress;
	
	@Value(CoreCommonConstants.$SERVER_PORT)
	private int coreSystemPort;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	public void initCoreSystem() {
		logger.debug("initCoreSystem started...");
		
		if (Utilities.isEmpty(coreSystemName)) {
			throw new ServiceConfigurationError("Core system name is not specified in the application.properties file");
		}
		
		try {
			coreSystem = CoreSystem.valueOf(coreSystemName.trim().toUpperCase());
		} catch (final IllegalArgumentException ex) {
			throw new ServiceConfigurationError("Core system name " + coreSystemName + " is not recognized.", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getCoreSystemName() { return coreSystemName; }
	public CoreSystem getCoreSystem() { return coreSystem; }
	public String getServiceRegistryAddress() { return serviceRegistryAddress; }
	public int getServiceRegistryPort() { return serviceRegistryPort; }
	
	//-------------------------------------------------------------------------------------------------
	public String getCoreSystemAddress() { 
		return Utilities.isEmpty(coreSystemAddress) ? CommonConstants.LOCALHOST : coreSystemAddress;
	}
	
	//-------------------------------------------------------------------------------------------------
	public int getCoreSystemPort() { 
		return coreSystemPort <= 0 ? coreSystem.getDefaultPort() : coreSystemPort;
	}
}