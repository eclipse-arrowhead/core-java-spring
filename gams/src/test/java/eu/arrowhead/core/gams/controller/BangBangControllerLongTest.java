package eu.arrowhead.core.gams.controller;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BangBangControllerLongTest {

    private static final double MAX_DELTA = 0.0;
    private Long input;
    private Double output;

    public BangBangControllerLongTest(final Long input, final Double output) {
        this.input = input;
        this.output = output;
    }

    @Parameterized.Parameters(name = "input: {0}, output: {1}")
    public static Collection testNumbers() {
        return Arrays.asList(new Object[][] {
                { 1L, -4.0 },
                { 5L, -0.0 },
                { 8L, 3.0 },
                { 12L, 7.0 }
        });
    }

    @Test
    public void bangBang_5() {
        final SetPointController<Long> setPointController = new SetPointController<>(false, 5L);
        Assert.assertEquals(output, setPointController.evaluate(input), MAX_DELTA);

    }

    @Test
    public void inverse_bangBang_5() {
        final SetPointController<Long> setPointController = new SetPointController<>(true, 5L);
        Assert.assertEquals(output *-1, setPointController.evaluate(input), MAX_DELTA);
    }
}