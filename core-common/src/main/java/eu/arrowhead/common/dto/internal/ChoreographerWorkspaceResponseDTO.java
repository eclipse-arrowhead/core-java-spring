package eu.arrowhead.common.dto.internal;

public class ChoreographerWorkspaceResponseDTO {

    private long id;

    private String name;

    private double relativeXCoordinate;

    private double relativeYCoordinate;

    private double relativeZCoordinate;

    private double relativeRCoordinate;

    private String createdAt;

    private String updatedAt;

    public ChoreographerWorkspaceResponseDTO() {}

    public ChoreographerWorkspaceResponseDTO(long id, String name, double relativeXCoordinate, double relativeYCoordinate, double relativeZCoordinate, double relativeRCoordinate, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.relativeXCoordinate = relativeXCoordinate;
        this.relativeYCoordinate = relativeYCoordinate;
        this.relativeZCoordinate = relativeZCoordinate;
        this.relativeRCoordinate = relativeRCoordinate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
