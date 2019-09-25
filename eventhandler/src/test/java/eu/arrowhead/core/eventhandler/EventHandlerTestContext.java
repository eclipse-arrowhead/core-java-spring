package eu.arrowhead.core.eventhandler;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.eventhandler.service.EventHandlerService;

@Configuration
public class EventHandlerTestContext {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public EventHandlerService mockEventHandlerService() {
		return Mockito.mock(EventHandlerService.class);
	}
	
}