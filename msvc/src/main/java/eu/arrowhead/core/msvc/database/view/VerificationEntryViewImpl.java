package eu.arrowhead.core.msvc.database.view;

import eu.arrowhead.core.msvc.database.entities.VerificationEntry;
import org.springframework.util.Assert;

public class VerificationEntryViewImpl implements VerificationEntryView {

    private final VerificationEntry target;

    public VerificationEntryViewImpl(final VerificationEntry target) {
        Assert.notNull(target, "Backing entry is null");
        Assert.notNull(target.getMip(), "Backing entry mip is null");
        this.target = target;
    }

    @Override
    public Long getId() {
        return target.getId();
    }

    @Override
    public Short getWeight() {
        return target.getWeight();
    }

    @Override
    public MipView getMip() {
        return new MipViewImpl(target.getMip());
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
