package eu.arrowhead.core.msvc.database.view;

import eu.arrowhead.core.msvc.database.OS;
import eu.arrowhead.core.msvc.database.entities.Target;
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
