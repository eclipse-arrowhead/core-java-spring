package eu.arrowhead.common.database.entity;

import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.ActionType;
import eu.arrowhead.core.gams.rest.dto.HttpMethod;

@Entity
@Table(name = "gams_http_url_api_call")
public class HttpUrlApiCall extends ProcessableAction {

    @Column(nullable = false, unique = false, length = 64)
    protected String serviceUri;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false, length = 16)
    protected HttpMethod method;

    @Column(nullable = true, unique = false, length = 64)
    protected String basicAuthentication;

    public HttpUrlApiCall() {
        super();
    }

    protected HttpUrlApiCall(final GamsInstance instance, final String name, final ActionType actionType, final HttpMethod method,
                             final String serviceUri, final String... knowledgeKeys) {
        super(instance, name, actionType, knowledgeKeys);
        this.method = method;
        this.serviceUri = serviceUri;
    }

    public HttpUrlApiCall(final GamsInstance instance, final String name, final HttpMethod method,
                             final String serviceUri, final String... knowledgeKeys) {
        super(instance, name, ActionType.API_URL_CALL, knowledgeKeys);
        this.method = method;
        this.serviceUri = serviceUri;
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(final String serviceUri) {
        this.serviceUri = serviceUri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(final HttpMethod method) {
        this.method = method;
    }

    public String getBasicAuthentication() {
        return basicAuthentication;
    }

    public void setBasicAuthentication(final String basicAuthentication) {
        this.basicAuthentication = basicAuthentication;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("method=" + method)
                .add("serviceUri='" + serviceUri + "'")
                .add("instance=" + instance)
                .toString();
    }
}
