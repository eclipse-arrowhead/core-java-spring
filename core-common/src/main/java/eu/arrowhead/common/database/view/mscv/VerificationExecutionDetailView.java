package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.dto.shared.mscv.DetailSuccessIndicator;
import org.springframework.beans.factory.annotation.Value;

public interface VerificationExecutionDetailView {

    @Value("#{target.execution.target.name}")
    String getTarget();

    @Value("#{target.execution.executionDate}")
    String getExecutionDate();

    @Value("#{target.verificationEntry.mip}")
    MipView getMip();

    @Value("#{target.execution.verificationList.name}")
    String getVerificationList();

    @Value("#{target.verificationEntry.weight}")
    Short getResultWeight();

    @Value("#{target.result}")
    DetailSuccessIndicator getResult();
}
