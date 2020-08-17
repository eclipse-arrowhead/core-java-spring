package eu.arrowhead.common.dto.internal;

public class ChoreographerRunPlanRequestDTO {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -4337560592612039357L;

    private long id;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunPlanRequestDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunPlanRequestDTO(long id) {
        this.id = id;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
}
