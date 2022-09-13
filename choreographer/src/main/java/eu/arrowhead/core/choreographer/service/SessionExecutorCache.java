package eu.arrowhead.core.choreographer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import eu.arrowhead.common.Defaults;
import eu.arrowhead.core.choreographer.executor.ExecutorData;

public class SessionExecutorCache {

	//=================================================================================================
	// members
	
	private static final String SEPARATOR = "/";
	
	private final Map<String,ExecutorData> executorCache = new ConcurrentHashMap<>();
	private final Set<Long> exclusions = new HashSet<>();
	private final Map<Long,List<Integer>> gatewayTunnels = new ConcurrentHashMap<>();
	private final boolean allowInterCloud;
	private final boolean chooseOptimalExecutor;
	private boolean done = false;
	private boolean aborted = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SessionExecutorCache(final boolean allowInterCloud, final boolean chooseOptimalExecutor) {
		this.allowInterCloud = allowInterCloud;
		this.chooseOptimalExecutor = chooseOptimalExecutor;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<String,ExecutorData> getExecutorCache() { return executorCache; }
	public Set<Long> getExclusions() { return exclusions; }
	public Map<Long,List<Integer>> getGatewayTunnels() { return gatewayTunnels; }
	public boolean isAllowInterCloud() { return allowInterCloud; }
	public boolean getChooseOptimalExecutor() { return chooseOptimalExecutor; }	
	public boolean isDone() { return done; }
	public boolean isAborted() { return aborted; }

	//-------------------------------------------------------------------------------------------------
	public ExecutorData get(final String serviceDefinition, final Integer minVersion, final Integer maxVersion) {
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;

		return executorCache.get(getKey(serviceDefinition, _minVersion, _maxVersion));
	}
	
	//-------------------------------------------------------------------------------------------------
	public void put(final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final ExecutorData executorData) {
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;

		executorCache.put(getKey(serviceDefinition, _minVersion, _maxVersion), executorData);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void done() {
		this.done = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void aborted() {
		this.aborted = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void remove(final String serviceDefinition, final Integer minVersion, final Integer maxVersion) {
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;

		executorCache.remove(getKey(serviceDefinition, _minVersion, _maxVersion));
	}

	//-------------------------------------------------------------------------------------------------
	public boolean isCacheRemovable() {
		return (aborted || done) && gatewayTunnels.isEmpty();
	}	
	
	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	private String getKey(final String serviceDefinition, final int minVersion, final int maxVersion) {
		return serviceDefinition + SEPARATOR + minVersion + SEPARATOR + maxVersion;
	}
}