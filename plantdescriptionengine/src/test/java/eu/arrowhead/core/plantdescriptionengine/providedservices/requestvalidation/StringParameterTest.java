package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringParameterTest {

    final String episodeKey = "episode";
    final String scoreKey = "score";

    final String episode4 = "A New Hope";
    final String episode5 = "The Empire Strikes Back";
    final String episode6 = "Return of the Jedi";

    @Test
    public void shouldParseParameters() throws ParseError {

        final int score = 10;

        final IntParameter scoreParam = new IntParameter.Builder()
            .name(scoreKey)
            .build();
        final StringParameter episodeParam = new StringParameter.Builder()
            .name(episodeKey)
            .legalValues(episode4, episode5, episode6)
            .requires(scoreParam)
            .build();
        final List<QueryParameter> acceptedParameters = List.of(episodeParam);
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(episodeKey, episode5)
            .queryParam(scoreKey, score)
            .build();
        final QueryParamParser parser = new QueryParamParser(null, acceptedParameters, request);

        assertEquals(episode5, parser.getValue(episodeParam).orElse(null));
        assertEquals(score, parser.getValue(scoreParam).orElse(null));
    }

    @Test
    public void shouldUseDefaultArgument() throws ParseError {

        final StringParameter episodeParam = new StringParameter.Builder().name("episode")
            .legalValues(episode4, episode5, episode6)
            .defaultValue(episode4)
            .build();
        final List<QueryParameter> acceptedParameters = List.of(episodeParam);
        final HttpServiceRequest request = new MockRequest.Builder().build();
        final QueryParamParser parser = new QueryParamParser(null, acceptedParameters, request);

        assertEquals(episode4, parser.getRequiredValue(episodeParam));
    }

    @Test
    public void shouldReportMissingParameter() {

        final List<QueryParameter> requiredParameters = List.of(new StringParameter.Builder()
            .name(QueryParameter.SORT_FIELD)
            .legalValues(QueryParameter.ID, QueryParameter.CREATED_AT, QueryParameter.UPDATED_AT)
            .build());

        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder().build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals(exception.getMessage(), "<Missing parameter '" + QueryParameter.SORT_FIELD + "'.>");
    }

    @Test
    public void shouldReportMissingDependency() {
        final String key = "age";
        final List<QueryParameter> acceptedParameters = List.of(new StringParameter.Builder().name("name")
            .requires(new IntParameter.Builder().name(key)
                .build())
            .build());

        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParam("name", "Alice")
                .build();
            new QueryParamParser(null, acceptedParameters, request);
        });
        assertEquals("<Missing parameter '" + key + "'.>", exception.getMessage());
    }

    @Test
    public void shouldOnlyAcceptLegalValues() {

        final String episode4 = "A New Hope";
        final String episode5 = "The Empire Strikes Back";
        final String episode6 = "Return of the Jedi";

        final QueryParameter param = new StringParameter.Builder()
            .name(episodeKey)
            .legalValues(episode4, episode5, episode6)
            .build();

        final String invalidEpisode = "The Rise of Skywalker";

        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParam(episodeKey, invalidEpisode)
                .build();
            new QueryParamParser(List.of(param), null, request);
        });

        assertEquals(exception.getMessage(), "<" + invalidEpisode +
            " is not a legal value for parameter " + episodeKey + ".>");
    }

}
