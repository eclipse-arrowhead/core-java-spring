package eu.arrowhead.core.msvc;

import eu.arrowhead.core.msvc.database.entities.Mip;
import eu.arrowhead.core.msvc.database.entities.VerificationEntry;
import eu.arrowhead.core.msvc.database.entities.VerificationEntryList;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import eu.arrowhead.core.msvc.database.view.MipView;
import eu.arrowhead.core.msvc.database.view.MipViewImpl;
import eu.arrowhead.core.msvc.database.view.VerificationEntryViewImpl;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionView;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionViewImpl;
import eu.arrowhead.core.msvc.database.view.VerificationListView;
import eu.arrowhead.core.msvc.database.view.VerificationListViewImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MsvcDtoConverter {
    private MsvcDtoConverter() { super(); }

    public static Set<VerificationEntryViewImpl> convert(final Collection<VerificationEntry> entries) {
        final var resultSet = new HashSet<VerificationEntryViewImpl>();
        for (VerificationEntry entry : entries) {
            resultSet.add(convert(entry));
        }
        return resultSet;
    }

    public static VerificationEntryViewImpl convert(final VerificationEntry entry) {
        if (Objects.isNull(entry)) { return null; }
        return new VerificationEntryViewImpl(entry);
    }

    public static MipView convert(final Mip mip) {
        if (Objects.isNull(mip)) { return null; }
        return new MipViewImpl(mip);
    }

    public static VerificationListView convert(final VerificationEntryList entryList) {
        return new VerificationListViewImpl(entryList);
    }

    public static VerificationExecutionView convert(final VerificationExecution execution) {
        return new VerificationExecutionViewImpl(execution);
    }
}
