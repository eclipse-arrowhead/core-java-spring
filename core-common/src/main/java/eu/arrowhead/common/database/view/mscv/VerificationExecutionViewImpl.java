package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.dto.shared.mscv.SuccessIndicator;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;

public class VerificationExecutionViewImpl implements VerificationExecutionView {

    private final VerificationResult target;

    public VerificationExecutionViewImpl(final VerificationResult target) {
        Assert.notNull(target, "Backing execution is null");
        Assert.notNull(target.getVerificationList(), "Backing verificationList is null");
        this.target = target;
    }

    @Override
    public Long getId() {
        return target.getId();
    }

    @Override
    public Target getTarget() {
        return target.getTarget();
    }

    @Override
    public VerificationListView getVerificationList() {
        return new VerificationListViewImpl(target.getVerificationList());
    }

    @Override
    public ZonedDateTime getExecutionDate() {
        return target.getExecutionDate();
    }

    @Override
    public SuccessIndicator getResult() {
        return target.getResult();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
