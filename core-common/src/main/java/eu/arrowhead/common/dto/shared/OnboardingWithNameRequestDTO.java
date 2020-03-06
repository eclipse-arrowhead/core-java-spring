package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;

@JsonInclude(Include.NON_NULL)
public class OnboardingWithNameRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 1L;

    private CertificateCreationRequestDTO creationRequestDTO;

    //=================================================================================================
    // constructors

    public OnboardingWithNameRequestDTO() {
    }

    public OnboardingWithNameRequestDTO(CertificateCreationRequestDTO creationRequestDTO) {
        this.creationRequestDTO = creationRequestDTO;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------

    public CertificateCreationRequestDTO getCreationRequestDTO() {
        return creationRequestDTO;
    }

    public void setCreationRequestDTO(CertificateCreationRequestDTO creationRequestDTO) {
        this.creationRequestDTO = creationRequestDTO;
    }
}