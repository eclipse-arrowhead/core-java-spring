package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ChoreographerExecutorRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 680650068900258014L;

    private String name;
    private String address;
    private Integer port;
    private String baseUri;
    private String serviceDefinitionName;
    private Integer version;

    //=================================================================================================
    // constructors

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorRequestDTO() {
    }

    public ChoreographerExecutorRequestDTO(String name, String address, Integer port, String serviceDefinitionName, Integer version) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
    }

    public ChoreographerExecutorRequestDTO(String name, String address, Integer port, String baseUri, String serviceDefinitionName, Integer version) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.baseUri = baseUri;
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Integer getPort() { return port; }
    public String getBaseUri() { return baseUri; }
    public String getServiceDefinitionName() { return serviceDefinitionName; }
    public Integer getVersion() { return version; }

    //-------------------------------------------------------------------------------------------------
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPort(Integer port) { this.port = port; }
    public void setBaseUri(String baseUri) { this.baseUri = baseUri; }
    public void setServiceDefinitionName(String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
    public void setVersion(Integer version) { this.version = version; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return new StringJoiner(", ", ChoreographerExecutorRequestDTO.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("address=" + address)
                .add("port='" + port + "'")
                .add("baseUri='" + baseUri + "'")
                .add("serviceDefinitionName='" + serviceDefinitionName + "'")
                .add("version=" + version)
                .toString();
    }
}
