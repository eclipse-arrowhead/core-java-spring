package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerStepRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8100852327039160839L;

	private String name;
    private String serviceName;
    private String metadata;
    private String parameters;
    private List<String> nextStepNames;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO(final String name, final String serviceName, final List<String> nextStepNames) {
        this.name = name;
        this.serviceName = serviceName;
        this.nextStepNames = nextStepNames;
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepRequestDTO(String name, String serviceName, String metadata, String parameters, List<String> nextStepNames) {
        this.name = name;
        this.serviceName = serviceName;
        this.metadata = metadata;
        this.parameters = parameters;
        this.nextStepNames = nextStepNames;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public String getServiceName() { return serviceName; }
	public List<String> getNextStepNames() { return nextStepNames; }
    public String getMetadata() { return metadata; }
    public String getParameters() { return parameters; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setServiceName(final String serviceName) { this.serviceName = serviceName; }
    public void setNextStepNames(final List<String> nextStepNames) { this.nextStepNames = nextStepNames; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setParameters(String parameters) { this.parameters = parameters; }
}