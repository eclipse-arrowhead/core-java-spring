package eu.arrowhead.core.gams.utility;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class MathHelper {

    private static final int PRECISION = 1000;

    public static <T extends Number> boolean isLowerThan(final T a, final T b) {
        return convert(a) < convert(b);
    }

    public static <T extends Number> long convert(final T number) {
        if(Objects.isNull(number)) throw new IllegalArgumentException("input number is null");
        else if(number instanceof Double) return (long)(((Double)number) * PRECISION);
        else if(number instanceof Float) return (long)(((Float)number) * PRECISION);
        else if(number instanceof Byte) return number.longValue();
        else if(number instanceof Short) return number.longValue();
        else if(number instanceof Integer) return number.longValue();
        else if(number instanceof Long) return number.longValue();
        else if(number instanceof BigInteger) return number.longValue();
        else if(number instanceof BigDecimal) return number.longValue();
        else throw new IllegalArgumentException("Unrecognized Number class: " + number.getClass());
    }
}
