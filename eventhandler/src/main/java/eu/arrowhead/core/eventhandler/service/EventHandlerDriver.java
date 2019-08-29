package eu.arrowhead.core.eventhandler.service;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.http.HttpService;

@Component
public class EventHandlerDriver {	

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(EventHandlerDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	

}
