package eu.arrowhead.common.testhelper;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;

public abstract class StandaloneModeInTests {
	
	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(StandaloneModeInTests.class);
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	@Order(5)
	public void onApplicationEvent(final ContextRefreshedEvent event)  {
		logger.info("STANDALONE mode is set...");
		arrowheadContext.put(CoreCommonConstants.SERVER_STANDALONE_MODE, true);
	}
}