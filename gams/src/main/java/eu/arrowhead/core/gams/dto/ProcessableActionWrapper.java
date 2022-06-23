package eu.arrowhead.core.gams.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.HttpUrlApiCall;
import eu.arrowhead.common.database.entity.Knowledge;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.Constants;
import eu.arrowhead.core.gams.service.EventService;
import eu.arrowhead.core.gams.service.KnowledgeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

public abstract class ProcessableActionWrapper extends AbstractActionWrapper implements Runnable {

    private final Logger logger = LogManager.getLogger();
    protected final KnowledgeService knowledgeService;
    protected final HttpUrlApiCall apiCall;

    public ProcessableActionWrapper(final EventService eventService, final Event sourceEvent,
                                    final KnowledgeService knowledgeService, final HttpUrlApiCall apiCall) {
        super(eventService, sourceEvent);
        this.knowledgeService = knowledgeService;
        this.apiCall = apiCall;
    }

    protected Map<String, String> loadKnowledgeMap() {
        final Map<String, String> knowledgeMap = new HashMap<>();
        for (String key : apiCall.getKnowledgeKeys()) {
            final Optional<Knowledge> optionalKnowledge = knowledgeService.get(apiCall.getInstance(), key);
            optionalKnowledge.ifPresent(knowledge -> knowledgeMap.put(key, knowledge.getValue()));
        }
        return knowledgeMap;
    }

    protected void processResults(final String result) {
        if (Objects.isNull(result)) { return; }

        final DocumentContext ctx = JsonPath.parse(result);
        final Map<String, String> processors = apiCall.getProcessors();
        for (Map.Entry<String, String> entry : processors.entrySet()) {
            logger.debug("Processing Body: {}={}", entry.getKey(), entry.getValue());
            final String extractedValue = ctx.read(entry.getKey());
            logger.debug("Saving Knowledge: {}={}", entry.getValue(), extractedValue);
            knowledgeService.put(apiCall.getInstance(), entry.getValue(), extractedValue);
        }
    }

    protected void processResults(final String body, final Sensor eventSensor) {
        if (Objects.isNull(body)) { return; }

        final DocumentContext ctx = JsonPath.parse(body);
        final Map<String, String> processors = apiCall.getProcessors();
        for (Map.Entry<String, String> entry : processors.entrySet()) {
            logger.debug("Processing Body: {}={}", entry.getKey(), entry.getValue());
            final String extractedValue = ctx.read(entry.getKey());
            logger.debug("Saving Knowledge: {}={}", entry.getValue(), extractedValue);
            final AbstractSensorData<?> abstractSensorData = knowledgeService.storeSensorData(eventSensor, extractedValue);
            eventService.createExecuteEvent(eventSensor,abstractSensorData);
        }
    }

    protected String processPlaceholders(final Map<String, String> knowledgeMap, final String s) {
        if (Objects.isNull(s)) { return null; }
        final StrSubstitutor sub = new StrSubstitutor(knowledgeMap, Constants.PLACEHOLDER_START, Constants.PLACEHOLDER_END);
        return sub.replace(s);
    }
}
