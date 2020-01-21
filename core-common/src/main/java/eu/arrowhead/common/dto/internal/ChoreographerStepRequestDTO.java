package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerStepRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8100852327039160839L;

	private String name;
    private List<String> serviceNames;
    private List<String> nextStepNames;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO(final String name, final List<String> serviceNames, final List<String> nextStepNames) {
        this.name = name;
        this.serviceNames = serviceNames;
        this.nextStepNames = nextStepNames;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public List<String> getServiceNames() { return serviceNames; }
	public List<String> getNextStepNames() { return nextStepNames; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setServiceNames(final List<String> serviceNames) { this.serviceNames = serviceNames; }
    public void setNextStepNames(final List<String> nextStepNames) { this.nextStepNames = nextStepNames; }
}