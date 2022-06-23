package eu.arrowhead.core.gams.dto;

import java.util.Map;
import java.util.Objects;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.HttpBodyApiCall;
import eu.arrowhead.common.database.entity.HttpUrlApiCall;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gams.service.EventService;
import eu.arrowhead.core.gams.service.KnowledgeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

public class HttpCallWrapper extends ProcessableActionWrapper implements Runnable {

    private final Logger logger = LogManager.getLogger();
    protected final HttpService httpService;

    public HttpCallWrapper(final EventService eventService, final Event sourceEvent,
                           final KnowledgeService knowledgeService, final HttpUrlApiCall apiCall, final HttpService httpService) {
        super(eventService, sourceEvent, knowledgeService, apiCall);
        this.httpService = httpService;
    }

    protected ResponseEntity<String> exchange() {
        final Map<String, String> knowledgeMap = loadKnowledgeMap();
        final String uri = processPlaceholders(knowledgeMap, createUri());
        final String body = processPlaceholders(knowledgeMap, getBody());

        final HttpEntity<?> entity = new HttpEntity<>(body);
        addAuthentication(entity);

        logger.info("Performing HTTP {} {}", getHttpMethod(), uri);
        return httpService.sendRequest(UriComponentsBuilder.fromUriString(uri).build(), getHttpMethod(), String.class, body);
    }

    @Override
    protected void innerRun() {
        final ResponseEntity<String> exchange = exchange();
        logger.info("HTTP result {} - parsing body", exchange.getStatusCodeValue());
        processResults(exchange.getBody());
    }

    @Override
    protected void innerRunWithResult(final Sensor eventSensor) {
        final ResponseEntity<String> exchange = exchange();
        logger.info("HTTP result {} - parsing body", exchange.getStatusCodeValue());
        processResults(exchange.getBody(), eventSensor);
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
