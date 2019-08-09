package eu.arrowhead.core.gatekeeper.service.matchmaking;

import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Cloud;

public class GatekeeperMatchmakingParameters {

	//=================================================================================================
	// members
	
	protected Cloud cloud;
	protected long randomSeed = System.currentTimeMillis();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperMatchmakingParameters(final Cloud cloud) {
		Assert.notNull(cloud, "cloud is null.");
		this.cloud = cloud;
	}

	//-------------------------------------------------------------------------------------------------
	public Cloud getCloud() { return cloud; }
	public long getRandomSeed() { return randomSeed; }
	
}
