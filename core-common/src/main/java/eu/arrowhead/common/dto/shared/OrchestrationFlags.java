package eu.arrowhead.common.dto.shared;

import java.util.HashMap;
import java.util.Map;

import eu.arrowhead.common.CommonConstants;

public class OrchestrationFlags extends HashMap<String,Boolean> {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 5901365646769727914L;
	
	private static final Map<String,Flag> flagMap = initFlagMap();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestrationFlags() {
		super(Flag.values().length);
		for (final Flag flag : Flag.values()) {
			put(flag, false);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationFlags(final Map<String,Boolean> map) {
		this();
		if (map != null) {
			for (final Entry<String,Boolean> entry : map.entrySet()) {
				if (flagMap.containsKey(entry.getKey())) {
					put(flagMap.get(entry.getKey()), entry.getValue());
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Boolean put(final Flag flag, final Boolean value) {
		final boolean validValue = (value == null ? false : value); 
		return super.put(flag.getFlag(), validValue);
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean get(final Flag flag) {
		return super.get(flag.getFlag());
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean getOrDefault(final Flag flag, final boolean defaultValue) {
		return super.getOrDefault(flag.getFlag(), defaultValue);
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean containsKey(final Flag flag) {
		return super.containsKey(flag.getFlag());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private static Map<String,Flag> initFlagMap() {
		final Map<String,Flag> result = new HashMap<>(Flag.values().length);
		result.put(CommonConstants.ORCHESTRATION_FLAG_MATCHMAKING, Flag.MATCHMAKING);
		result.put(CommonConstants.ORCHESTRATION_FLAG_METADATA_SEARCH, Flag.METADATA_SEARCH);
		result.put(CommonConstants.ORCHESTRATION_FLAG_ONLY_PREFERRED, Flag.ONLY_PREFERRED);
		result.put(CommonConstants.ORCHESTRATION_FLAG_PING_PROVIDERS, Flag.PING_PROVIDERS);
		result.put(CommonConstants.ORCHESTRATION_FLAG_OVERRIDE_STORE, Flag.OVERRIDE_STORE);
		result.put(CommonConstants.ORCHESTRATION_FLAG_TRIGGER_INTER_CLOUD, Flag.TRIGGER_INTER_CLOUD);
		result.put(CommonConstants.ORCHESTRATION_FLAG_EXTERNAL_SERVICE_REQUEST, Flag.EXTERNAL_SERVICE_REQUEST);
		result.put(CommonConstants.ORCHESTRATION_FLAG_ENABLE_INTER_CLOUD, Flag.ENABLE_INTER_CLOUD);
		result.put(CommonConstants.ORCHESTRATION_FLAG_ENABLE_QOS, Flag.ENABLE_QOS);
		
		return result;
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	public enum Flag {
		
		//=================================================================================================
		// elements
		
		MATCHMAKING(CommonConstants.ORCHESTRATION_FLAG_MATCHMAKING),
		METADATA_SEARCH(CommonConstants.ORCHESTRATION_FLAG_METADATA_SEARCH),
		ONLY_PREFERRED(CommonConstants.ORCHESTRATION_FLAG_ONLY_PREFERRED),
		PING_PROVIDERS(CommonConstants.ORCHESTRATION_FLAG_PING_PROVIDERS),
		OVERRIDE_STORE(CommonConstants.ORCHESTRATION_FLAG_OVERRIDE_STORE),
		TRIGGER_INTER_CLOUD(CommonConstants.ORCHESTRATION_FLAG_TRIGGER_INTER_CLOUD),
		EXTERNAL_SERVICE_REQUEST(CommonConstants.ORCHESTRATION_FLAG_EXTERNAL_SERVICE_REQUEST),
		ENABLE_INTER_CLOUD(CommonConstants.ORCHESTRATION_FLAG_ENABLE_INTER_CLOUD),
		ENABLE_QOS(CommonConstants.ORCHESTRATION_FLAG_ENABLE_QOS);
		
		//=================================================================================================
		// members
		
		private final String flag;
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public String getFlag() { return flag; }
		
		//-------------------------------------------------------------------------------------------------
		@Override
		public String toString() {
			return getFlag();
		}
		
		//=================================================================================================
		// assistant methods
		
		//-------------------------------------------------------------------------------------------------
		private Flag(final String flag) {
			this.flag = flag;
		}
	}
}