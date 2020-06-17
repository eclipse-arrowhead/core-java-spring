package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoreographerStepDetailResponseDTO {

    //=================================================================================================
    // members

    private long id;
    private String serviceDefinition;
    private int version;
    private String type;
    private int minVersion;
    private int maxVersion;
    private String dto;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getServiceDefinition() { return serviceDefinition; }
    public int getVersion() { return version; }
    public String getType() { return type; }
    public int getMinVersion() { return minVersion; }
    public int getMaxVersion() { return maxVersion; }public String getDto() { return dto; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setServiceDefinition(String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setVersion(int version) { this.version = version; }
    public void setType(String type) { this.type = type; }
    public void setMinVersion(int minVersion) { this.minVersion = minVersion; }
    public void setMaxVersion(int maxVersion) { this.maxVersion = maxVersion; }
    public void setDto(String dto) { this.dto = dto; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
