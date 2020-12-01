package eu.arrowhead.core.translator.services.fiware.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FiwareUrlServices {

    //=================================================================================================
    // members
    private final String entitiesUrl;
    private final String typesUrl;
    private final String subscriptionsUrl;
    private final String registrationsUrl;

    @JsonCreator
    public FiwareUrlServices(
            @JsonProperty("entities_url") String entitiesUrl,
            @JsonProperty("types_url") String typesUrl,
            @JsonProperty("subscriptions_url") String subscriptionsUrl,
            @JsonProperty("registrations_url") String registrationsUrl
    ) {
        this.entitiesUrl = entitiesUrl;
        this.typesUrl = typesUrl;
        this.subscriptionsUrl = subscriptionsUrl;
        this.registrationsUrl = registrationsUrl;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public String getEntitiesURL() {
        return entitiesUrl;
    }

    //-------------------------------------------------------------------------------------------------
    public String getTypesURL() {
        return typesUrl;
    }

    //-------------------------------------------------------------------------------------------------
    public String getSubscriptionsURL() {
        return subscriptionsUrl;
    }

    //-------------------------------------------------------------------------------------------------
    public String getRegistrationsURL() {
        return registrationsUrl;
    }

}
