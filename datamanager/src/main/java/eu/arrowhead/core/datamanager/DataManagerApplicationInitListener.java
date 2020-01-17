package eu.arrowhead.core.datamanager;

import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;

import eu.arrowhead.core.datamanager.service.HistorianService;

@Component
public class DataManagerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	//-------------------------------------------------------------------------------------------------
	
	@Autowired
	private ApplicationContext applicationContext;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		
		return List.of(CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {				
		Properties prop = null;
		logger.debug("customInit started...");
		try (InputStream input = new FileInputStream("application.properties")) {
			prop = new Properties();
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		HistorianService.Init(prop);

		System.out.println("Init here");


	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {				
		logger.debug("customDestroy started...");


	}

}

