package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class VerificationResultDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private String executionDate;
    private Double result;
    private String listName;
    private String listDescription;
    private TargetDto target;
    private SuccessIndicator successIndicator;
    private List<MipVerificationResultDto> mipResults;


    public VerificationResultDto() {
        super();
    }

    public VerificationResultDto(final String executionDate, final Double result, final String listName, final String listDescription, final TargetDto target,
                                 final SuccessIndicator successIndicator,
                                 final List<MipVerificationResultDto> mipResults) {
        this.executionDate = executionDate;
        this.result = result;
        this.listName = listName;
        this.listDescription = listDescription;
        this.target = target;
        this.successIndicator = successIndicator;
        this.mipResults = mipResults;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(final String executionDate) {
        this.executionDate = executionDate;
    }

    public Double getResult() {
        return result;
    }

    public void setResult(final Double result) {
        this.result = result;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(final String listName) {
        this.listName = listName;
    }

    public String getListDescription() {
        return listDescription;
    }

    public void setListDescription(final String listDescription) {
        this.listDescription = listDescription;
    }

    public TargetDto getTarget() {
        return target;
    }

    public void setTarget(final TargetDto target) {
        this.target = target;
    }

    public SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    public void setSuccessIndicator(final SuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    public List<MipVerificationResultDto> getMipResults() {
        return mipResults;
    }

    public void setMipResults(final List<MipVerificationResultDto> mipResults) {
        this.mipResults = mipResults;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationResultDto that = (VerificationResultDto) o;
        return Objects.equals(executionDate, that.executionDate) &&
                Objects.equals(result, that.result) &&
                Objects.equals(listName, that.listName) &&
                Objects.equals(listDescription, that.listDescription) &&
                Objects.equals(target, that.target) &&
                successIndicator == that.successIndicator &&
                Objects.equals(mipResults, that.mipResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionDate, result, listName, listDescription, target, successIndicator, mipResults);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VerificationResultDto.class.getSimpleName() + "[", "]")
                .add("executionDate='" + executionDate + "'")
                .add("result=" + result)
                .add("listName='" + listName + "'")
                .add("listDescription='" + listDescription + "'")
                .add("target=" + target)
                .add("successIndicator=" + successIndicator)
                .add("mipResults=" + mipResults)
                .toString();
    }
}
