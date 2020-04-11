package eu.arrowhead.core.msvc.database.view;

import org.springframework.beans.factory.annotation.Value;

public interface VerificationRunView {

    @Value("#{target.execution.target.name}")
    String getTarget();

    @Value("#{target.execution.executionDate}")
    String getExecutionDate();

    @Value("#{target.mip}")
    MipView geMeasurableIndicatorPoint();

    @Value("#{target.execution.verificationList.name}")
    String getVerificationSet();

    @Value("#{target.result}")
    Integer getResult();
}
