package eu.arrowhead.core.mscv;

import java.util.Base64;
import java.util.Objects;
import java.util.function.Supplier;

import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import org.apache.http.HttpStatus;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.Validation.CREDENTIALS_NULL_ERROR_MESSAGE;


public class MscvUtilities {

    private static final Validation VALIDATION = new Validation();
    private static final String NOT_FOUND_ERROR_MESSAGE = " not found";

    private MscvUtilities() { throw new UnsupportedOperationException(); }

    public static Supplier<DataNotFoundException> notFoundException(final String variable) {
        return notFoundException(variable, null);
    }

    public static Supplier<DataNotFoundException> notFoundException(final String variable, final String origin) {
        return () -> new DataNotFoundException(variable + NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, origin);
    }


    public static Tuple2<String, String> decodeCredentials(final String base64Credentials, final String origin) {
        if(Objects.isNull(base64Credentials)) {
            throw new BadPayloadException(CREDENTIALS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        final String decoded = new String(Base64.getDecoder().decode(base64Credentials));
        final String[] credentials = decoded.split(":", 2);
        VALIDATION.verifyCredentials(credentials, origin);

        return MscvUtilities.Tuple2.of(credentials);
    }

    public static class Tuple2<T1, T2> {
        private final T1 t1;
        private final T2 t2;

        public Tuple2(final T1 t1, final T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public static <T1, T2> Tuple2 of(final T1 t1, final T2 t2) {
            return new Tuple2(t1, t2);
        }

        @SafeVarargs
        public static <X> Tuple2 of(final X... array) {
            Assert.isTrue(array.length == 2, "There must be 2 arguments");
            return new Tuple2(array[0], array[1]);
        }

        public T1 getT1() {
            return t1;
        }

        public T2 getT2() {
            return t2;
        }
    }
}
