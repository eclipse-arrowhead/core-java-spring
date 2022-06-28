package eu.arrowhead.common.database.view.mscv;

import org.springframework.beans.factory.annotation.Value;

public interface MipView {

    @Value("#{target.category.abbreviation + '-' + target.extId}")
    String getIdentifier();

    @Value("#{target.name}")
    String getName();

    @Value("#{target.description}")
    String getDescription();

    @Value("#{target.domain.name}")
    String getDomain();

    @Value("#{target.category.name}")
    String getCategory();

    @Value("#{target.category.abbreviation}")
    String getCategoryAbbreviation();

    @Value("#{target.standard.identification}")
    String getStandard();

    @Value("#{target.standard.name}")
    String getStandardName();

    @Value("#{target.standard.referenceUri}")
    String getReferenceUri();
}
