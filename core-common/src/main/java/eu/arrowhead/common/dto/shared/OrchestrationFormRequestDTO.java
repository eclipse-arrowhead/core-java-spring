package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.exception.BadPayloadException;

public class OrchestrationFormRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -3595427649312358521L;

	private SystemRequestDTO requesterSystem;
	private CloudRequestDTO requesterCloud;
	private ServiceQueryFormDTO requestedService;
	private OrchestrationFlags orchestrationFlags = new OrchestrationFlags();
	private List<PreferredProviderDataDTO> preferredProviders = new ArrayList<>();
	private Map<String,String> commands = new HashMap<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public OrchestrationFormRequestDTO() {}

	//-------------------------------------------------------------------------------------------------	
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public OrchestrationFlags getOrchestrationFlags() { return orchestrationFlags; }
	public List<PreferredProviderDataDTO> getPreferredProviders() { return preferredProviders; }
	public Map<String,String> getCommands() { return commands; }
	
	//-------------------------------------------------------------------------------------------------	
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setOrchestrationFlags(final Map<String,Boolean> orchestrationFlags) { this.orchestrationFlags = new OrchestrationFlags(orchestrationFlags); }
	public void setCommands(final Map<String,String> commands) { this.commands = commands; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredProviders(final List<PreferredProviderDataDTO> preferredProviders) {
		if (preferredProviders != null) {
			this.preferredProviders = preferredProviders;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationFormRequestDTO validateCrossParameterConstraints() {
        if (requestedService == null && orchestrationFlags.get(Flag.OVERRIDE_STORE)) {
            throw new BadPayloadException("Requested service can not be null when \"" + Flag.OVERRIDE_STORE + "\" is TRUE");
        }
        
        if (requestedService == null && orchestrationFlags.get(Flag.TRIGGER_INTER_CLOUD)) {
            throw new BadPayloadException("Requested service can not be null when \"" + Flag.TRIGGER_INTER_CLOUD + "\" is TRUE");
        }

        if (orchestrationFlags.get(Flag.ONLY_PREFERRED)) {
        	for (final Iterator<PreferredProviderDataDTO> it = preferredProviders.iterator(); it.hasNext();) {
				final PreferredProviderDataDTO provider = it.next();
				if (!provider.isValid()) {
					it.remove();
				}
			}
        	
            if (preferredProviders.isEmpty()) {
                throw new BadPayloadException("There is no valid preferred provider, but \"" + Flag.ONLY_PREFERRED + "\" is set to true");
            }
        }

        return this;
    }
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationFormRequestDTO(final Builder builder) {
		this.requesterSystem = builder.requesterSystem;
		this.requesterCloud = builder.requesterCloud;
		this.requestedService = builder.requestedService;
		this.orchestrationFlags = builder.orchestrationFlags;
		this.preferredProviders = builder.preferredProviders;
		this.commands = builder.commands;
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	public static class Builder {
		
		//=================================================================================================
		// members
		
		private final SystemRequestDTO requesterSystem;
		private CloudRequestDTO requesterCloud;
		private ServiceQueryFormDTO requestedService;
		private final OrchestrationFlags orchestrationFlags = new OrchestrationFlags();
		private List<PreferredProviderDataDTO> preferredProviders = new ArrayList<>();
		private Map<String,String> commands = new HashMap<>();

		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public Builder(final SystemRequestDTO requesterSystem) {
			Assert.notNull(requesterSystem, "requesterSystem is null.");
			this.requesterSystem = requesterSystem;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder requesterCloud(final CloudRequestDTO requesterCloud) {
			this.requesterCloud = requesterCloud;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder requestedService(final ServiceQueryFormDTO requestedService) {
			this.requestedService = requestedService;
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public Builder flag(final Flag flag, final boolean value) {
			this.orchestrationFlags.put(flag, value);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder flags(final OrchestrationFlags flags) {
			this.orchestrationFlags.putAll(flags);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder preferredProviders(final PreferredProviderDataDTO... preferredProviders) {
			this.preferredProviders = preferredProviders == null || preferredProviders.length == 0 ? List.of() : Arrays.asList(preferredProviders);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder commands(final Map<String,String> commands) {
			this.commands = commands;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder command(final String key, final String value) {
			Assert.isTrue(key != null && !key.isBlank(), "Key is null or blank");
			
			if (this.commands == null) {
				this.commands = new HashMap<>();
			}
			this.commands.put(key, value);
			
			return this;

		}
		
		//-------------------------------------------------------------------------------------------------
		public OrchestrationFormRequestDTO build() {
			return new OrchestrationFormRequestDTO(this).validateCrossParameterConstraints();
		}
	}
}