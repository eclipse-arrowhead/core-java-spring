package eu.arrowhead.common.database.view.mscv;

import org.springframework.beans.factory.annotation.Value;

public interface VerificationEntryView {

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.weight}")
    Short getWeight();

    @Value("#{target.mip}")
    MipView getMip();
}
