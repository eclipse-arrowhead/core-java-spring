package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.dto.shared.mscv.VerificationRunDetailResult;
import org.springframework.beans.factory.annotation.Value;

public interface VerificationExecutionDetailView {

    @Value("#{target.execution.target.name}")
    String getTarget();

    @Value("#{target.execution.executionDate}")
    String getExecutionDate();

    @Value("#{target.verificationEntry.mip}")
    MipView getMip();

    @Value("#{target.execution.verificationList.name}")
    String getVerificationSet();

    @Value("#{target.verificationEntry.weight}")
    Integer getResultWeight();

    @Value("#{target.result}")
    VerificationRunDetailResult getResult();
}
