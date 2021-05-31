package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BooleanParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldParseBooleans() throws ParseError {

        String key1 = "smart";
        String key2 = "tired";
        String key3 = "strong";
        String key4 = "fat";

        final BooleanParameter smartParam = new BooleanParameter.Builder()
            .name(key1)
            .build();
        final BooleanParameter tiredParam = new BooleanParameter.Builder()
            .name(key2)
            .build();

        final List<QueryParameter> requiredParameters = List.of(smartParam, tiredParam);

        final BooleanParameter strongParam = new BooleanParameter.Builder().name(key3).build();
        final BooleanParameter fatParam = new BooleanParameter.Builder().name(key4).build();
        final List<QueryParameter> acceptedParameters = List.of(strongParam, fatParam);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key1, true)
            .queryParam(key2, false)
            .queryParam(key3, true)
            .queryParam(key4, false)
            .build();

        final QueryParamParser parser = new QueryParamParser(requiredParameters, acceptedParameters, request);

        assertTrue(parser.getValue(smartParam).orElse(false));
        assertFalse(parser.getValue(tiredParam).orElse(true));
        assertTrue(parser.getValue(strongParam).orElse(false));
        assertFalse(parser.getValue(fatParam).orElse(true));
    }

    @Test
    public void shouldUseDefaultArgument() throws ParseError {

        final BooleanParameter goodParam = new BooleanParameter.Builder()
            .name("good")
            .defaultValue(true)
            .build();
        final BooleanParameter happyParam = new BooleanParameter.Builder()
            .name("happy")
            .defaultValue(false)
            .build();

        final List<QueryParameter> acceptedParameters = List.of(goodParam, happyParam);
        final HttpServiceRequest request = new MockRequest.Builder().build();
        final QueryParamParser parser = new QueryParamParser(null, acceptedParameters, request);

        assertTrue(parser.getValue(goodParam).orElse(false));
        assertFalse(parser.getValue(happyParam).orElse(true));
    }

    @Test
    public void shouldNonBooleans() throws ParseError {
        final String key = "cool";
        final String value = "123";
        final List<QueryParameter> requiredParameters = List.of(new BooleanParameter.Builder()
            .name(key)
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key, value)
            .build();

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Query parameter '" + key + "' must be true or false, got '" + value + "'.>");
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldReportMissingParameter() throws ParseError {

        final List<QueryParameter> requiredParameters = List
            .of(new BooleanParameter.Builder()
                .name("weekends")
                .build());

        final HttpServiceRequest request = new MockRequest.Builder().build();

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Missing parameter 'weekends'.>");
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldReportMissingDependency() throws ParseError {
        final String sortKey = "sort";
        final QueryParameter itemPerPageParameter = new IntParameter.Builder()
            .name(QueryParameter.ITEM_PER_PAGE)
            .build();
        final QueryParameter sortParameter = new BooleanParameter.Builder().name(sortKey)
            .requires(itemPerPageParameter)
            .build();

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(sortKey, true)
            .build();
        thrown.expect(ParseError.class);
        thrown.expectMessage("<Missing parameter '" + QueryParameter.ITEM_PER_PAGE + "'.>");
        new QueryParamParser(null, List.of(sortParameter), request);
    }

}
