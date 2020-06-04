package eu.arrowhead.core.gams.controller;

import eu.arrowhead.core.gams.utility.MathHelper;
import org.springframework.util.Assert;

/**
 * A SetPoint controller has two set points on which it switches the output.
 * It returns a positive number if the input is below the lower set point and a negative if the number exceeds the upper set point. It returns 0 in all other cases.
 * If the lower set point and the upper set point is the same, it acts as a BangBangController.
 */
public class SetPointController<T extends Number> {

    private final boolean inverse;
    private final long lowerSetPoint;
    private final long upperSetPoint;

    public SetPointController(final T setPoint) {
        this(false, setPoint, setPoint);
    }

    public SetPointController(final T lowerSetPoint, final T upperSetPoint) {
        this(false, lowerSetPoint, upperSetPoint);
    }

    public SetPointController(final boolean inverse, final T setPoint) {
        this(inverse, setPoint, setPoint);
    }

    public SetPointController(final boolean inverse, final T lowerSetPoint, final T upperSetPoint) {
        super();
        this.inverse = inverse;
        this.lowerSetPoint = MathHelper.convert(lowerSetPoint);
        this.upperSetPoint = MathHelper.convert(upperSetPoint);

        if (this.lowerSetPoint > this.upperSetPoint) { throw new IllegalArgumentException("lowerSetPoint must not be greater than upperSetPoint"); }
    }

    public long evaluate(final T inputT) {
        Assert.notNull(inputT, "input is null");

        final long input = MathHelper.convert(inputT);
        final long result;

        if (input < lowerSetPoint) { result = Math.subtractExact(lowerSetPoint, input); }
        else if (input > upperSetPoint) { result = Math.subtractExact(upperSetPoint, input); }
        else { result = 0; }

        if (inverse) { return Math.negateExact(result); }
        else { return result; }
    }
}
