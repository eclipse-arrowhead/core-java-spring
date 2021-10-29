package eu.arrowhead.core.gatekeeper.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

@Component
@DisallowConcurrentExecution
public class RelaySupervisingTask implements Job {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		for (final GatekeeperRelayClient client : RelaySuprvisor.getRegistry()) {
			client.destroyStaleQueuesAndConnections();;
		}
	}
}
