package eu.arrowhead.core.mscv.service;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.dto.shared.mscv.TargetDto;
import org.junit.Assert;
import org.junit.Test;

public class TargetServiceTest {

    private final TargetService service = new TargetService(null, null);
    @Test
    public void test_is_supported()
    {
        Assert.assertTrue(service.isSupported(SshTargetDto.class));
        Assert.assertTrue(service.isSupported(SshTarget.class));
        Assert.assertFalse(service.isSupported(TargetDto.class));
        Assert.assertFalse(service.isSupported(Target.class));
        Assert.assertFalse(service.isSupported(TargetService.class));
    }
}