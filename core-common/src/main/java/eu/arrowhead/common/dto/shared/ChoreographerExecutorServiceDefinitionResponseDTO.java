package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.StringJoiner;

public class ChoreographerExecutorServiceDefinitionResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private long id;
    private String serviceDefinitionName;
    private Integer version;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // constructors

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceDefinitionResponseDTO() { }

    //-------------------------------------------------------------------------------------------------

    public ChoreographerExecutorServiceDefinitionResponseDTO(long id, String serviceDefinitionName, Integer version, String createdAt, String updatedAt) {
        this.id = id;
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getServiceDefinitionName() { return serviceDefinitionName; }
    public Integer getVersion() { return version; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setServiceDefinitionName(String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
    public void setVersion(Integer version) { this.version = version; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public String toString() {
        return new StringJoiner(", ", ChoreographerExecutorServiceDefinitionResponseDTO.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("serviceDefinitionName=" + serviceDefinitionName)
                .add("version'" + version + "'")
                .add("createdAt='" + createdAt + "'")
                .add("updatedAt='" + updatedAt + "'")
                .toString();
    }
}
