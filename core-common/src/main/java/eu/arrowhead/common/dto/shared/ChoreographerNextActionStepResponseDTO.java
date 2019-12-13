package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class ChoreographerNextActionStepResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -4318735564703960811L;
	
	private long id;
    private String stepName;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerNextActionStepResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerNextActionStepResponseDTO(final long id, final String stepName) {
        this.id = id;
        this.stepName = stepName;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getStepName() { return stepName; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setStepName(final String stepName) { this.stepName = stepName; }
}