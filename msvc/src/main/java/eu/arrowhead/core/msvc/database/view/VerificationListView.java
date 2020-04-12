package eu.arrowhead.core.msvc.database.view;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

public interface VerificationListView {

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.name}")
    String getName();

    @Value("#{target.description}")
    String getDescription();

    @Value("#{target.entries}")
    Set<VerificationEntryView> getEntries();
}
