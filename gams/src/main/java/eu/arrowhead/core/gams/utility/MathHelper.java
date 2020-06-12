package eu.arrowhead.core.gams.utility;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class MathHelper {

    public static final double PRECISION = 1000.0;

    public static <T extends Number> boolean isLowerThan(final T a, final T b) {
        return convert(a) < convert(b);
    }

    public static long convert(final Object number) {
        if(Objects.isNull(number)) throw new IllegalArgumentException("input number is null");
        else if(number instanceof Double) return (long)(((Double)number) * PRECISION);
        else if(number instanceof Float) return (long)(((Float)number) * PRECISION);
        else if(number instanceof Byte) return ((Byte) number).longValue();
        else if(number instanceof Short) return ((Short) number).longValue();
        else if(number instanceof Integer) return ((Integer) number).longValue();
        else if(number instanceof Long) return (Long) number;
        else if(number instanceof BigInteger) return ((BigInteger) number).longValue();
        else if(number instanceof BigDecimal) return ((BigDecimal) number).longValue();
        else throw new IllegalArgumentException("Unrecognized Number class: " + number.getClass());
    }
}
