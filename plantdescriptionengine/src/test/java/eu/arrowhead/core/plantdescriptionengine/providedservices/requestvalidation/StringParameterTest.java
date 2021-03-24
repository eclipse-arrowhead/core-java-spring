package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringParameterTest {

    @Test
    public void shouldParseParameters() throws ParseError {

        final var scoreParam = new IntParameter.Builder()
            .name("score")
            .build();
        final var episodeParam = new StringParameter.Builder()
            .name("episode")
            .legalValues("A New Hope", "The Empire Strikes Back", "Return of the Jedi")
            .requires(scoreParam)
            .build();

        final List<QueryParameter> acceptedParameters = List.of(episodeParam);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("episode", List.of("The Empire Strikes Back"), "score", List.of("10")))
            .build();

        final var parser = new QueryParamParser(null, acceptedParameters, request);

        assertEquals("The Empire Strikes Back", parser.getValue(episodeParam).orElse(null));
        assertEquals(10, parser.getValue(scoreParam).orElse(null));
    }

    @Test
    public void shouldUseDefaultArgument() throws ParseError {

        final var episodeParam = new StringParameter.Builder().name("episode")
            .legalValues("A New Hope", "The Empire Strikes Back", "Return of the Jedi")
            .defaultValue("A new Hope")
            .build();

        final List<QueryParameter> acceptedParameters = List.of(episodeParam);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of())
            .build();

        final var parser = new QueryParamParser(null, acceptedParameters, request);

        assertEquals("A new Hope", parser.getRequiredValue(episodeParam));
    }

    @Test
    public void shouldReportMissingParameter() {

        final List<QueryParameter> requiredParameters = List.of(new StringParameter.Builder()
            .name("sort_field")
            .legalValues("id", "createdAt", "updatedAt")
            .build());

        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParameters(Map.of())
                .build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals(exception.getMessage(), "<Missing parameter 'sort_field'.>");
    }

    @Test
    public void shouldReportMissingDependency() {

        final List<QueryParameter> acceptedParameters = List.of(new StringParameter.Builder().name("name")
            .requires(new IntParameter.Builder().name("age")
                .build())
            .build());

        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParameters(Map.of("name", List.of("Alice")))
                .build();
            new QueryParamParser(null, acceptedParameters, request);
        });
        assertEquals("<Missing parameter 'age'.>", exception.getMessage());
    }

    @Test
    public void shouldOnlyAcceptLegalValues() {

        final List<QueryParameter> requiredParameters = List.of(new StringParameter.Builder()
            .name("episode")
            .legalValues("A New Hope", "The Empire Strikes Back", "Return of the Jedi")
            .build());

        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParameters(Map.of("episode", List.of("The Rise of Skywalker")))
                .build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals(exception.getMessage(), "<The Rise of Skywalker is not a legal value for parameter episode.>");
    }

}
