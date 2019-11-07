package eu.arrowhead.common.dto.internal;

public class ChoreographerWorkspaceRequestDTO {

    private String name;

    private double relativeXCoordinate;

    private double relativeYCoordinate;

    private double relativeZCoordinate;

    private double relativeRCoordinate;

    public ChoreographerWorkspaceRequestDTO() {}

    public ChoreographerWorkspaceRequestDTO(String name, double relativeXCoordinate, double relativeYCoordinate, double relativeZCoordinate, double relativeRCoordinate) {
        this.name = name;
        this.relativeXCoordinate = relativeXCoordinate;
        this.relativeYCoordinate = relativeYCoordinate;
        this.relativeZCoordinate = relativeZCoordinate;
        this.relativeRCoordinate = relativeRCoordinate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRelativeXCoordinate() {
        return relativeXCoordinate;
    }

    public void setRelativeXCoordinate(double relativeXCoordinate) {
        this.relativeXCoordinate = relativeXCoordinate;
    }

    public double getRelativeYCoordinate() {
        return relativeYCoordinate;
    }

    public void setRelativeYCoordinate(double relativeYCoordinate) {
        this.relativeYCoordinate = relativeYCoordinate;
    }

    public double getRelativeZCoordinate() {
        return relativeZCoordinate;
    }

    public void setRelativeZCoordinate(double relativeZCoordinate) {
        this.relativeZCoordinate = relativeZCoordinate;
    }

    public double getRelativeRCoordinate() {
        return relativeRCoordinate;
    }

    public void setRelativeRCoordinate(double relativeRCoordinate) {
        this.relativeRCoordinate = relativeRCoordinate;
    }
}
