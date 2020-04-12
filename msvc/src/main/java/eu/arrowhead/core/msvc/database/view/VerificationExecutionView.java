package eu.arrowhead.core.msvc.database.view;

import eu.arrowhead.core.msvc.database.VerificationRunResult;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;

public interface VerificationExecutionView {

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.target}")
    TargetView getTarget();

    @Value("#{target.verificationList}")
    VerificationListView getVerificationList();

    @Value("#{target.executionDate}")
    ZonedDateTime getExecutionDate();

    @Value("#{target.result}")
    VerificationRunResult getResult();
}
