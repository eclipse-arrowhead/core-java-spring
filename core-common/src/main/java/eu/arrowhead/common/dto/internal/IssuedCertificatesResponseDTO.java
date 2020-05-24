package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IssuedCertificatesResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long count;
    private List<IssuedCertificateDTO> issuedCertificates;

    public IssuedCertificatesResponseDTO() {
        setIssuedCertificates(new ArrayList<>());
    }

    public IssuedCertificatesResponseDTO(List<IssuedCertificateDTO> certificates, long count) {
        setIssuedCertificates(certificates);
        this.count = count;
    }

    public List<IssuedCertificateDTO> getIssuedCertificates() {
        return issuedCertificates;
    }

    public void setIssuedCertificates(List<IssuedCertificateDTO> issuedCertificates) {
        this.issuedCertificates = issuedCertificates;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

}
