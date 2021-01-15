package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

public class  ChoreographerExecutorResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 2497926459449130611L;

    private long id;
    private String name;
    private String address;
    private Integer port;
    private String baseUri;
    private List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // constructors

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorResponseDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorResponseDTO(long id, String name, String address, Integer port, String baseUri, List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.baseUri = baseUri;
        this.serviceDefinitions = serviceDefinitions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorResponseDTO(long id, String name, String address, Integer port, List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.serviceDefinitions = serviceDefinitions;
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
    public List<ChoreographerExecutorServiceDefinitionResponseDTO> getServiceDefinitions() { return serviceDefinitions; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPort(Integer port) { this.port = port; }
    public void setBaseUri(String baseUri) { this.baseUri = baseUri; }
    public void setServiceDefinitions(List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions) { this.serviceDefinitions = serviceDefinitions; }
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
                .add("serviceDefinition=" + serviceDefinitions.toString())
                .add("createdAt='" + createdAt + "'")
                .add("updatedAt='" + updatedAt + "'")
                .toString();
    }
}
