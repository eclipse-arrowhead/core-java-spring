package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class MipVerificationResultDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String standard;
    private String domain;
    private String category;
    private String mipName;
    private String mipIdentifier;
    private Layer layer;
    private DetailSuccessIndicator successIndicator;

    public MipVerificationResultDto() {
        super();
    }

    public MipVerificationResultDto(final String standard, final String domain, final String category, final String mipName, final String mipIdentifier,
                                    final Layer layer, final DetailSuccessIndicator successIndicator) {
        this.standard = standard;
        this.domain = domain;
        this.category = category;
        this.mipName = mipName;
        this.mipIdentifier = mipIdentifier;
        this.layer = layer;
        this.successIndicator = successIndicator;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(final String standard) {
        this.standard = standard;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getMipIdentifier() {
        return mipIdentifier;
    }

    public void setMipIdentifier(final String mipIdentifier) {
        this.mipIdentifier = mipIdentifier;
    }

    public String getMipName() {
        return mipName;
    }

    public void setMipName(final String mipName) {
        this.mipName = mipName;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(final Layer layer) {
        this.layer = layer;
    }

    public DetailSuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    public void setSuccessIndicator(final DetailSuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final MipVerificationResultDto that = (MipVerificationResultDto) o;
        return Objects.equals(standard, that.standard) &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(category, that.category) &&
                Objects.equals(mipName, that.mipName) &&
                Objects.equals(mipIdentifier, that.mipIdentifier) &&
                layer == that.layer &&
                successIndicator == that.successIndicator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(standard, domain, category, mipName, mipIdentifier, layer, successIndicator);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MipVerificationResultDto.class.getSimpleName() + "[", "]")
                .add("standard='" + standard + "'")
                .add("domain='" + domain + "'")
                .add("category='" + category + "'")
                .add("mipName='" + mipName + "'")
                .add("mipIdentifier='" + mipIdentifier + "'")
                .add("layer=" + layer)
                .add("successIndicator=" + successIndicator)
                .toString();
    }
}
