package eu.arrowhead.core.gams.utility;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MathHelperTest {

    private Object input;
    private Long outputL;
    private Double outputD;

    public MathHelperTest(final Object input, final Long outputL, final Double outputD) {
        this.input = input;
        this.outputL = outputL;
        this.outputD = outputD;
    }

    @Parameterized.Parameters(name = "{0} -> {1}L -> {2}D")
    public static Collection testNumbers() {
        return Arrays.asList(new Object[][] {
                { 1L, 1L, 1.0 },
                { 1.0, 1L, 1.0 },
                { "1.0", 1L, 1.0 },
                { 2.5, 2L, 2.5 },
                { 3.9, 3L, 3.9 },
                { "3.9", 3L, 3.9 },
                { "3", 3L, 3.0 },
        });
    }

    @Test
    public void convertToLongTC() {
        Assert.assertEquals(outputL, (Long)MathHelper.convertToLong(input));
    }
    @Test
    public void convertToDoubleTC() {
        Assert.assertEquals(outputD, (Double) MathHelper.convertToDouble(input));
    }
}