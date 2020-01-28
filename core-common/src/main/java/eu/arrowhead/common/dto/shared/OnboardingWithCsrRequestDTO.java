package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class OnboardingWithCsrRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 1L;

    private String certificateSigningRequest;

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public String getCertificateSigningRequest() { return certificateSigningRequest; }

    //-------------------------------------------------------------------------------------------------
    public void setCertificateSigningRequest(final String certificateSigningRequest) { this.certificateSigningRequest = certificateSigningRequest; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public int hashCode()
    {
        return Objects.hash(certificateSigningRequest);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final OnboardingWithCsrRequestDTO other = (OnboardingWithCsrRequestDTO) obj;

        return Objects.equals(certificateSigningRequest, other.certificateSigningRequest);
    }
}