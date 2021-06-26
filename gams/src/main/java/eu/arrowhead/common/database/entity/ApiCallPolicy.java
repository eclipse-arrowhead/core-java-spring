package eu.arrowhead.common.database.entity;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gams_api_policy")
public class ApiCallPolicy extends AbstractPolicy {

    @OneToOne(optional = false)
    private ProcessableAction apiCall;

    public ApiCallPolicy() { super(); }

    public ProcessableAction getApiCall() {
        return apiCall;
    }

    public void setApiCall(final ProcessableAction apiCall) {
        this.apiCall = apiCall;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ApiCallPolicy)) { return false; }
        if (!super.equals(o)) { return false; }
        final ApiCallPolicy that = (ApiCallPolicy) o;
        return Objects.equals(apiCall, that.apiCall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), apiCall);
    }
}
