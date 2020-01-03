package eu.arrowhead.common.core;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

import static eu.arrowhead.common.core.CoreSystemService.*;

public enum CoreSystem
{

    //=================================================================================================
    // elements

    SERVICE_REGISTRY(Defaults.DEFAULT_SERVICE_REGISTRY_PORT, List.of(SERVICE_REGISTRY_REGISTER_SERVICE, SERVICE_REGISTRY_UNREGISTER_SERVICE)),
    SYSTEM_REGISTRY(Defaults.DEFAULT_SYSTEM_REGISTRY_PORT, List.of(SYSTEM_REGISTRY_REGISTER_SERVICE, SYSTEM_REGISTRY_UNREGISTER_SERVICE)),
    DEVICE_REGISTRY(Defaults.DEFAULT_DEVICE_REGISTRY_PORT, List.of(DEVICE_REGISTRY_REGISTER_SERVICE, DEVICE_REGISTRY_UNREGISTER_SERVICE)),
    ONBOARDING(Defaults.DEFAULT_ONBOARDING_PORT, List.of(ONBOARDING_SERVICE)),
    AUTHORIZATION(Defaults.DEFAULT_AUTHORIZATION_PORT, List.of(AUTH_CONTROL_INTRA_SERVICE, AUTH_CONTROL_INTER_SERVICE,
                                                               AUTH_TOKEN_GENERATION_SERVICE, AUTH_PUBLIC_KEY_SERVICE,
                                                               AUTH_CONTROL_SUBSCRIPTION_SERVICE)),
    ORCHESTRATOR(Defaults.DEFAULT_ORCHESTRATOR_PORT, List.of(ORCHESTRATION_SERVICE)),
    GATEKEEPER(Defaults.DEFAULT_GATEKEEPER_PORT, List.of(GATEKEEPER_GLOBAL_SERVICE_DISCOVERY, GATEKEEPER_INTER_CLOUD_NEGOTIATION)),
    EVENT_HANDLER(Defaults.DEFAULT_EVENT_HANDLER_PORT, List.of(EVENT_PUBLISH_SERVICE, EVENT_SUBSCRIBE_SERVICE,
                                                               EVENT_UNSUBSCRIBE_SERVICE, EVENT_PUBLISH_AUTH_UPDATE_SERVICE)),
    GATEWAY(Defaults.DEFAULT_GATEWAY_PORT, List.of(GATEWAY_PUBLIC_KEY_SERVICE, GATEWAY_PROVIDER_SERVICE, GATEWAY_CONSUMER_SERVICE)),
    CHOREOGRAPHER(Defaults.DEFAULT_CHOREOGRAPHER_PORT, List.of()), // TODO: add services
    CERTIFICATE_AUTHORITY(Defaults.DEFAULT_CERTIFICATE_AUTHORITY_PORT, List.of()); // TODO: add services

    //=================================================================================================
    // members

    private final int defaultPort;
    private final List<CoreSystemService> services;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    private CoreSystem(final int defaultPort, final List<CoreSystemService> services)
    {
        Assert.isTrue(defaultPort > CommonConstants.SYSTEM_PORT_RANGE_MIN && defaultPort < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Default port is invalid.");
        this.services = services != null ? Collections.unmodifiableList(services) : List.of();
        this.defaultPort = defaultPort;
    }

    //-------------------------------------------------------------------------------------------------
    public int getDefaultPort() { return defaultPort; }

    //=================================================================================================
    // assistant methods

    public List<CoreSystemService> getServices() { return services; }
}