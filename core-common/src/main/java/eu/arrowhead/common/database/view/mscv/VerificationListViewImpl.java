package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VerificationListViewImpl implements VerificationListView {

    private final VerificationEntryList target;

    public VerificationListViewImpl(final VerificationEntryList target) {
        Assert.notNull(target, "Backing list is null");
        Assert.notNull(target.getEntries(), "Backing list is null");
        this.target = target;
    }

    @Override
    public Long getId() {
        return target.getId();
    }

    @Override
    public String getName() {
        return target.getName();
    }

    @Override
    public String getDescription() {
        return target.getDescription();
    }

    @Override
    public Long getVerificationInterval() {
        return target.getVerificationInterval();
    }

    @Override
    public Set<VerificationEntryView> getEntries() {
        final var set = new HashSet<VerificationEntryView>();
        for (VerificationEntry entry : target.getEntries()) {
            set.add(new VerificationEntryViewImpl(entry));
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
