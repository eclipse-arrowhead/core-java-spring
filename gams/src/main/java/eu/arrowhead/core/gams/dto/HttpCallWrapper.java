package eu.arrowhead.core.gams.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.HttpBodyApiCall;
import eu.arrowhead.common.database.entity.HttpUrlApiCall;
import eu.arrowhead.core.gams.service.EventService;
import eu.arrowhead.core.gams.service.KnowledgeService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpCallWrapper extends ProcessableActionWrapper implements Runnable {

    protected final RestTemplate restTemplate;

    public HttpCallWrapper(final EventService eventService, final Event sourceEvent,
                           final KnowledgeService knowledgeService, final HttpUrlApiCall apiCall) {
        super(eventService, sourceEvent, knowledgeService, apiCall);
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void innerRun() {
        final Map<String, String> knowledgeMap = new HashMap<>();
        loadKnowledgeMap(knowledgeMap);

        final String uri = processPlaceholders(knowledgeMap, createUri());
        final String body = processPlaceholders(knowledgeMap, getBody());

        final HttpEntity<?> entity = new HttpEntity<>(body);
        addAuthentication(entity);

        final ResponseEntity<String> exchange = restTemplate.exchange(uri, getHttpMethod(), entity, String.class);
        final String result = exchange.getBody();
        processResults(result);
    }

    protected void addAuthentication(final HttpEntity<?> entity) {
        if (Objects.nonNull(apiCall.getBasicAuthentication())) {
            entity.getHeaders().add(HttpHeaders.AUTHORIZATION, apiCall.getBasicAuthentication());
        }
    }

    protected String getBody() {
        if (apiCall instanceof HttpBodyApiCall) {
            return ((HttpBodyApiCall) apiCall).getBody();
        } else {
            return null;
        }
    }

    protected HttpMethod getHttpMethod() {
        switch (apiCall.getMethod()) {
            case GET: return HttpMethod.GET;
            case PUT: return HttpMethod.PUT;
            case POST: return HttpMethod.POST;
            case PATCH: return HttpMethod.PATCH;
            case DELETE: return HttpMethod.DELETE;
            default: return null;
        }
    }

    protected String createUri() {
        return apiCall.getServiceUri();
    }
}
