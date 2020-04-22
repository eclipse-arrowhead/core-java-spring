package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.dto.shared.mscv.VerificationRunResult;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;

public interface VerificationExecutionView {

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.target}")
    Target getTarget();

    @Value("#{target.verificationList}")
    VerificationListView getVerificationList();

    @Value("#{target.executionDate}")
    ZonedDateTime getExecutionDate();

    @Value("#{target.result}")
    VerificationRunResult getResult();
}
