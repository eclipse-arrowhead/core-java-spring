package eu.arrowhead.common.database.view.mscv;

import eu.arrowhead.common.database.entity.mscv.Mip;
import org.springframework.util.Assert;

public class MipViewImpl implements MipView {

    private final Mip target;

    public MipViewImpl(final Mip target) {
        Assert.notNull(target, "Backing mip is null");
        Assert.notNull(target.getCategory(), "Backing mip category is null");
        Assert.notNull(target.getDomain(), "Backing mip domain is null");
        Assert.notNull(target.getStandard(), "Backing mip standard is null");
        this.target = target;
    }

    @Override
    public String getIdentifier() {
        return target.getCategory().getAbbreviation() + '-' + target.getExtId();
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
    public String getDomain() {
        return target.getDomain().getName();
    }

    @Override
    public String getCategory() {
        return target.getCategory().getName();
    }

    @Override
    public String getCategoryAbbreviation() {
        return target.getCategory().getAbbreviation();
    }

    @Override
    public String getStandard() {
        return target.getStandard().getIdentification();
    }

    @Override
    public String getStandardName() {
        return target.getStandard().getName();
    }

    @Override
    public String getReferenceUri() {
        return target.getStandard().getReferenceUri();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
