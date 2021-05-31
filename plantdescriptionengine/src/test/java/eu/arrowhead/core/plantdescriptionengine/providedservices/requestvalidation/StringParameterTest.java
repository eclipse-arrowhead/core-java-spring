package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StringParameterTest {

    final String episodeKey = "episode";
    final String scoreKey = "score";

    final String episode4 = "A New Hope";
    final String episode5 = "The Empire Strikes Back";
    final String episode6 = "Return of the Jedi";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        assertEquals(score, (int) parser.getValue(scoreParam).orElse(-1));
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
    public void shouldReportMissingParameter() throws ParseError {

        final List<QueryParameter> requiredParameters = List.of(new StringParameter.Builder()
            .name(QueryParameter.SORT_FIELD)
            .legalValues(QueryParameter.ID, QueryParameter.CREATED_AT, QueryParameter.UPDATED_AT)
            .build());

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Missing parameter '" + QueryParameter.SORT_FIELD + "'.>");

        final HttpServiceRequest request = new MockRequest.Builder().build();
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldReportMissingDependency() throws ParseError {
        final String key = "age";
        final List<QueryParameter> acceptedParameters = List.of(new StringParameter.Builder().name("name")
            .requires(new IntParameter.Builder().name(key)
                .build())
            .build());

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Missing parameter '" + key + "'.>");

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam("name", "Alice")
            .build();
        new QueryParamParser(null, acceptedParameters, request);
    }

    @Test
    public void shouldOnlyAcceptLegalValues() throws ParseError {

        final String episode4 = "A New Hope";
        final String episode5 = "The Empire Strikes Back";
        final String episode6 = "Return of the Jedi";

        final QueryParameter param = new StringParameter.Builder()
            .name(episodeKey)
            .legalValues(episode4, episode5, episode6)
            .build();

        final String invalidEpisode = "The Rise of Skywalker";

        thrown.expect(ParseError.class);
        thrown.expectMessage("<" + invalidEpisode + " is not a legal value for parameter " + episodeKey + ".>");

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(episodeKey, invalidEpisode)
            .build();
        new QueryParamParser(List.of(param), null, request);
    }

}
