package eu.arrowhead.core.qos;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.RelayTestService;

@Configuration
public class QoSMonitorTestContext {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public QoSDBService mockQoSDBService() {
		return Mockito.mock(QoSDBService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public PingService mockPingService() {
		return Mockito.mock(PingService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public RelayTestService mockRelayTestService() {
		return Mockito.mock(RelayTestService.class);
	}
}
