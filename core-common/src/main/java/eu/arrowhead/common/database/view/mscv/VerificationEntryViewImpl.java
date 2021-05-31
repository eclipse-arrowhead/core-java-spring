package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
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
