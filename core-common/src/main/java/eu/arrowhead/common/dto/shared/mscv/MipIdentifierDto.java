package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class MipIdentifierDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String categoryAbbreviation;
    private Integer extId;

    public MipIdentifierDto() {
        super();
    }

    public MipIdentifierDto(final String categoryAbbreviation, final Integer extId) {
        this.categoryAbbreviation = categoryAbbreviation;
        this.extId = extId;
    }

    public String getCategoryAbbreviation() {
        return categoryAbbreviation;
    }

    public void setCategoryAbbreviation(final String categoryAbbreviation) {
        this.categoryAbbreviation = categoryAbbreviation;
    }

    public Integer getExtId() {
        return extId;
    }

    public void setExtId(final Integer extId) {
        this.extId = extId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MipIdentifierDto.class.getSimpleName() + "[", "]")
                .add("categoryAbbreviation='" + categoryAbbreviation + "'")
                .add("extId=" + extId)
                .toString();
    }
}
