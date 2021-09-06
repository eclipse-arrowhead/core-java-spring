package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QueryParamParserTest {

    @Test
    public void shouldReturnRequiredBoolean() throws ParseError {
        final String isAnimalString = "is_animal";
        final String isMammalString = "is_mammal";

        final BooleanParameter isAnimal = new BooleanParameter.Builder()
            .name(isAnimalString)
            .build();

        final BooleanParameter isMammal = new BooleanParameter.Builder()
            .name(isMammalString)
            .build();

        final List<QueryParameter> required = List.of(isAnimal, isMammal);
        final List<QueryParameter> accepted = Collections.emptyList();
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(isAnimalString, true)
            .queryParam(isMammalString, false)
            .build();
        final QueryParamParser parser = new QueryParamParser(required, accepted, request);

        assertTrue(parser.getRequiredValue(isAnimal));
        assertFalse(parser.getRequiredValue(isMammal));

    }

}
