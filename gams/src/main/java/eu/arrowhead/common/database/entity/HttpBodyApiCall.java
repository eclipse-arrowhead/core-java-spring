package eu.arrowhead.common.database.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.ActionType;
import eu.arrowhead.core.gams.rest.dto.HttpMethod;

@Entity
@Table(name = "gams_http_body_api_call")
public class HttpBodyApiCall extends HttpUrlApiCall {

    @Column(nullable = true, unique = false, length = 512)
    protected String body;

    public HttpBodyApiCall() {
        super();
    }

    public HttpBodyApiCall(final GamsInstance instance, final String name, final HttpMethod method,
                           final String serviceUri, final String body, final String... knowledgeKeys) {
        super(instance, name, ActionType.API_BODY_CALL, method, serviceUri, knowledgeKeys);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
