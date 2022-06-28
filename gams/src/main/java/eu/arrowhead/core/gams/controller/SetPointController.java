package eu.arrowhead.core.gams.controller;

import eu.arrowhead.core.gams.utility.MathHelper;
import org.springframework.util.Assert;

/**
 * A SetPoint controller has two set points on which it switches the output.
 * It returns a positive number if the input exceeds the upper set point and a negative if the number is below the lower set point. It returns 0 in all other
 * cases.
 * If the lower set point and the upper set point is the same, it acts similar to a BangBangController where a positive output equals to the "on" state.
 */
public class SetPointController<T extends Number> {

    private final boolean inverse;
    private final MathHelper.MathHelperDto<T> lowerSetPoint;
    private final MathHelper.MathHelperDto<T> upperSetPoint;

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
        this.lowerSetPoint = MathHelper.convertWithPrecision(lowerSetPoint);
        this.upperSetPoint = MathHelper.convertWithPrecision(upperSetPoint);

        if (this.lowerSetPoint.output > this.upperSetPoint.output) {
            throw new IllegalArgumentException("lowerSetPoint must not be greater than upperSetPoint");
        }
    }

    public static <T extends Number> SetPointController<T> create (final boolean inverse, final T lowerSetPoint, final T upperSetPoint) {
        return new SetPointController<>(inverse, lowerSetPoint, upperSetPoint);
    }

    public double evaluate(final T inputT) {
        Assert.notNull(inputT, "number is null");

        final MathHelper.MathHelperDto<T> number = MathHelper.convertWithPrecision(inputT);
        final long result;

        if (number.output < lowerSetPoint.output) {
            result = Math.subtractExact(number.output, lowerSetPoint.output);
        } else if (number.output > upperSetPoint.output) {
            result = Math.subtractExact(number.output, upperSetPoint.output);
        } else {
            result = 0;
        }

        if (inverse) {
            return number.convertResult(Math.negateExact(result)).doubleValue();
        } else {
            return number.convertResult(result).doubleValue();
        }
    }
}
