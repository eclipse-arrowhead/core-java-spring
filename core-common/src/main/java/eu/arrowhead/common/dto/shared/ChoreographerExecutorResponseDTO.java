package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.StringJoiner;

public class ChoreographerExecutorResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 2497926459449130611L;

    private long id;
    private String name;
    private String address;
    private Integer port;
    private String baseUri;
    private String serviceDefinitionName;
    private Integer version;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // constructors

    //-------------------------------------------------------------------------------------------------

    public ChoreographerExecutorResponseDTO() {
    }

    public ChoreographerExecutorResponseDTO(long id, String name, String address, Integer port, String serviceDefinitionName, Integer version, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ChoreographerExecutorResponseDTO(long id, String name, String address, Integer port, String baseUri, String serviceDefinitionName, Integer version, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.baseUri = baseUri;
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Integer getPort() { return port; }
    public String getBaseUri() { return baseUri; }
    public String getServiceDefinitionName() { return serviceDefinitionName; }
    public Integer getVersion() { return version; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPort(Integer port) { this.port = port; }
    public void setBaseUri(String baseUri) { this.baseUri = baseUri; }
    public void setServiceDefinitionName(String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
    public void setVersion(Integer version) { this.version = version; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return new StringJoiner(", ", ChoreographerExecutorResponseDTO.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name=" + name)
                .add("address=" + address)
                .add("port='" + port + "'")
                .add("baseUri='" + baseUri + "'")
                .add("serviceDefinitionName=" + serviceDefinitionName)
                .add("version=" + version)
                .add("createdAt='" + createdAt + "'")
                .add("updatedAt='" + updatedAt + "'")
                .toString();
    }
}
