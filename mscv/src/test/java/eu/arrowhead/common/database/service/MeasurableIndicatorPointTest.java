package eu.arrowhead.common.database.service;

import eu.arrowhead.common.database.repository.mscv.MipCategoryRepository;
import eu.arrowhead.common.database.repository.mscv.MipDomainRepository;
import eu.arrowhead.common.database.repository.mscv.MipRepository;
import eu.arrowhead.common.database.repository.mscv.StandardRepository;
import eu.arrowhead.common.database.view.mscv.MipView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create"
})
@Sql(scripts = "classpath:/sql/mip.sql")
public class MeasurableIndicatorPointTest {

    @Autowired
    private MipCategoryRepository indicatorPointGroupRepository;
    @Autowired
    private MipDomainRepository indicatorPointTypeRepository;
    @Autowired
    private MipRepository measurableIndicatorPointRepository;
    @Autowired
    private StandardRepository standardDescriptionRepository;

    @Test
    public void injectedComponentsAreNotNull() {
        Assert.assertNotNull(indicatorPointGroupRepository);
        Assert.assertNotNull(indicatorPointTypeRepository);
        Assert.assertNotNull(measurableIndicatorPointRepository);
        Assert.assertNotNull(standardDescriptionRepository);
    }

    @Test
    public void mipView() {
        MipView mip = measurableIndicatorPointRepository.findViewById(1L);
        Assert.assertEquals("IAC-1", mip.getIdentifier());
        Assert.assertEquals("SafIAC", mip.getName());
        Assert.assertEquals("Description SafIAC", mip.getDescription());
        Assert.assertEquals("SAFETY", mip.getDomain());
        Assert.assertEquals("IAC", mip.getCategory());
        Assert.assertEquals("IAC", mip.getCategoryAbbreviation());
        Assert.assertEquals("BLA-4671-347", mip.getStandard());
        Assert.assertEquals("uri", mip.getReferenceUri());

        mip = measurableIndicatorPointRepository.findViewById(2L);
        Assert.assertEquals("IAC-2", mip.getIdentifier());
        Assert.assertEquals("SecIAC", mip.getName());
        Assert.assertEquals("Description SecIAC", mip.getDescription());
        Assert.assertEquals("SECURITY", mip.getDomain());
        Assert.assertEquals("IAC", mip.getCategory());
        Assert.assertEquals("IAC", mip.getCategoryAbbreviation());
        Assert.assertEquals("BLA-4671-347", mip.getStandard());
        Assert.assertEquals("uri", mip.getReferenceUri());
    }
}