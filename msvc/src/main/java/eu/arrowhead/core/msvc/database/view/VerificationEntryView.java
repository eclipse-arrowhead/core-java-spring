package eu.arrowhead.core.msvc.database.view;

import org.springframework.beans.factory.annotation.Value;

public interface VerificationEntryView {

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.id}")
    Short getWeight();

    @Value("#{target.id}")
    MipView getMip();
}
