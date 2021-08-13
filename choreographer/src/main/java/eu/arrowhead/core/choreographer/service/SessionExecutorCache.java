package eu.arrowhead.core.choreographer.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;

public class SessionExecutorCache {

	//=================================================================================================
	// members
	
	private static final String SEPARATOR = "/";
	
	private final Map<String,ChoreographerExecutor> executorCache = new ConcurrentHashMap<>();
	private final Set<Long> exclusions = new HashSet<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Map<String,ChoreographerExecutor> getExecutorCache() { return executorCache; }
	public Set<Long> getExclusions() { return exclusions; }
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutor get(final String serviceDefinition, final Integer minVersion, final Integer maxVersion) {
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;

		return executorCache.get(getKey(serviceDefinition, _minVersion, _maxVersion));
	}
	
	//-------------------------------------------------------------------------------------------------
	public void put(final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final ChoreographerExecutor executor) {
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;

		executorCache.put(getKey(serviceDefinition, _minVersion, _maxVersion), executor);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void remove(final String serviceDefinition, final Integer minVersion, final Integer maxVersion) {
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;

		executorCache.remove(getKey(serviceDefinition, _minVersion, _maxVersion));
	}

	
	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	private String getKey(final String serviceDefinition, final int minVersion, final int maxVersion) {
		return serviceDefinition + SEPARATOR + minVersion + SEPARATOR + maxVersion;
	}
}