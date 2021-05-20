package eu.arrowhead.core.gams.controller;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SetPointControllerDoubleTest {

    private static final double MAX_DELTA = 0.0;
    private Double input;
    private Double output;

    public SetPointControllerDoubleTest(final Double input, final Double output) {
        this.input = input;
        this.output = output;
    }

    @Parameterized.Parameters(name = "input: {0}, output: {1}")
    public static Collection testNumbers() {
        return Arrays.asList(new Object[][] {
                { 1.0, -4.0 },
                { 4.9, -0.1 },
                { 5.0, -0.0 },
                { 8.0, 0.0 },
                { 10.01, 0.01 },
                { 12.0, 2.0 }
        });
    }

    @Test
    public void setPoint_5_to_10() {
        final SetPointController<Double> setPointController = new SetPointController<>(false, 5.0, 10.0);
        Assert.assertEquals(output, setPointController.evaluate(input), MAX_DELTA);
    }

    @Test
    public void inverse_setPoint_5_to_10() {
        final SetPointController<Double> setPointController = new SetPointController<>(true, 5.0, 10.0);
        Assert.assertEquals(output * -1, setPointController.evaluate(input), MAX_DELTA);
    }
}