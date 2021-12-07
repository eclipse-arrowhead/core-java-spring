package eu.arrowhead.core.confmgr.arrowhead.model.response;

import java.util.List;

import lombok.Data;

@Data
public class PublicKeyEndpointResponseDTO {
    private List<ServiceRegistryResponseDTO> serviceQueryData;
    private int unfilteredHits;
}