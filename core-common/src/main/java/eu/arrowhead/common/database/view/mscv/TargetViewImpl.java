package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.database.entity.mscv.Target;
import org.springframework.util.Assert;

public class TargetViewImpl implements TargetView {

    private final Target target;

    public TargetViewImpl(final Target target) {
        Assert.notNull(target, "Backing target is null");
        this.target = target;
    }

    @Override
    public String getName() {
        return target.getName();
    }

    @Override
    public OS getOs() {
        return target.getOs();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
