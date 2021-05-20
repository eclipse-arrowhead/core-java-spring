package eu.arrowhead.core.gams.dto;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.HttpUrlApiCall;
import eu.arrowhead.common.database.entity.Knowledge;
import eu.arrowhead.core.gams.Constants;
import eu.arrowhead.core.gams.service.EventService;
import eu.arrowhead.core.gams.service.KnowledgeService;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

public abstract class ProcessableActionWrapper extends AbstractActionWrapper implements Runnable {

    protected final KnowledgeService knowledgeService;
    protected final HttpUrlApiCall apiCall;

    public ProcessableActionWrapper(final EventService eventService, final Event sourceEvent,
                                    final KnowledgeService knowledgeService, final HttpUrlApiCall apiCall) {
        super(eventService, sourceEvent);
        this.knowledgeService = knowledgeService;
        this.apiCall = apiCall;
    }

    protected void loadKnowledgeMap(final Map<String, String> knowledgeMap) {

        for (String key : apiCall.getKnowledgeKeys()) {
            final Optional<Knowledge> optionalKnowledge = knowledgeService.get(apiCall.getInstance(), key);
            optionalKnowledge.ifPresent(knowledge -> knowledgeMap.put(key, knowledge.getValue()));
        }
    }

    protected void processResults(final String result) {
        if (Objects.isNull(result)) { return; }

        final DocumentContext ctx = JsonPath.parse(result);
        for (Map.Entry<String, String> entry : apiCall.getProcessors().entrySet()) {
            final String extractedValue = ctx.read(entry.getKey());
            knowledgeService.put(apiCall.getInstance(), entry.getValue(), extractedValue);
        }
    }

    protected String processPlaceholders(final Map<String, String> knowledgeMap, final String s) {
        if (Objects.isNull(s)) { return null; }
        final StrSubstitutor sub = new StrSubstitutor(knowledgeMap, Constants.PLACEHOLDER_START, Constants.PLACEHOLDER_END);
        return sub.replace(s);
    }
}
