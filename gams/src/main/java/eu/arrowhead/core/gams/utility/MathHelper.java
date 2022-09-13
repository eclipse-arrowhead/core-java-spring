package eu.arrowhead.core.gams.utility;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class MathHelper {

    public static final double PRECISION = 1000.0;

    public static <T extends Number> boolean isLowerThan(final T a, final T b) {
        return convertToLong(a) < convertToLong(b);
    }

    public static Long convertToLong(final Object number) {
        if (Objects.isNull(number)) {
            throw new IllegalArgumentException("input number is null");
        } else if (number instanceof Double) {
            return ((Double) number).longValue();
        } else if (number instanceof Float) {
            return ((Float) number).longValue();
        } else if (number instanceof Byte) {
            return ((Byte) number).longValue();
        } else if (number instanceof Short) {
            return ((Short) number).longValue();
        } else if (number instanceof Integer) {
            return ((Integer) number).longValue();
        } else if (number instanceof Long) {
            return (Long) number;
        } else if (number instanceof BigInteger) {
            return ((BigInteger) number).longValue();
        } else if (number instanceof BigDecimal) {
            return ((BigDecimal) number).longValue();
        } else if (number instanceof String) {
            return parseStringAsLong((String) number);
        } else {
            throw new IllegalArgumentException("Unrecognized Number class: " + number.getClass());
        }
    }

    private static Long parseStringAsLong(final String number) {
        return convertToLong(parseStringAsDouble(number));
    }

    private static Double parseStringAsDouble(final String number) {
        try {
            return convertToDouble(Double.parseDouble(number));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number: " + number);
        }
    }

    public static Double convertToDouble(final Object number) {

        if (Objects.isNull(number)) {
            throw new IllegalArgumentException("input number is null");
        } else if (number instanceof Double) {
            long value = (long) (((Double) number) * PRECISION);
            return value / PRECISION;
        } else if (number instanceof Float) {
            long value = (long) (((Float) number) * PRECISION);
            return value / PRECISION;
        } else if (number instanceof Byte) {
            return ((Byte) number).doubleValue();
        } else if (number instanceof Short) {
            return ((Short) number).doubleValue();
        } else if (number instanceof Integer) {
            return ((Integer) number).doubleValue();
        } else if (number instanceof Long) {
            return ((Long) number).doubleValue();
        } else if (number instanceof BigInteger) {
            return ((BigInteger) number).doubleValue();
        } else if (number instanceof BigDecimal) {
            return ((BigDecimal) number).doubleValue();
        } else if (number instanceof String) {
            return parseStringAsDouble((String) number);
        } else {
            throw new IllegalArgumentException("Unrecognized Number class: " + number.getClass());
        }
    }

    public static <T> MathHelperDto<T> convertWithPrecision(final T input) {
        if (Objects.isNull(input)) {
            throw new IllegalArgumentException("input number is null");
        }
        final Long output = Math.round(convertToDouble(input) * PRECISION);
        final Class<T> inputClass = (Class<T>) input.getClass();
        return new MathHelperDto<>(input, output, inputClass);
    }

    public static class MathHelperDto<T> {
        public final T input;
        public final Long output;
        private final Class<T> inputClass;

        private MathHelperDto(final T input, final Long output, final Class<T> inputClass) {
            this.input = input;
            this.output = output;
            this.inputClass = inputClass;
        }

        public T convertResult(final Long result) {
            if (inputClass == String.class) {
                return (T) dividePrecision(result).toString();
            } else {
                return (T) dividePrecision(result);
            }
        }

        private Double dividePrecision(final long result) {
            return result / PRECISION;
        }
    }
}
