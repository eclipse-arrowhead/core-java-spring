package eu.arrowhead.common.core;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

import static eu.arrowhead.common.core.CoreSystemService.DEVICE_REGISTRY_REGISTER_SERVICE;
import static eu.arrowhead.common.core.CoreSystemService.DEVICE_REGISTRY_UNREGISTER_SERVICE;
import static eu.arrowhead.common.core.CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_CSR;
import static eu.arrowhead.common.core.CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_NAME;
import static eu.arrowhead.common.core.CoreSystemService.ONBOARDING_WITH_SHARED_SECRET_AND_CSR;
import static eu.arrowhead.common.core.CoreSystemService.ONBOARDING_WITH_SHARED_SECRET_AND_NAME;
import static eu.arrowhead.common.core.CoreSystemService.SYSTEM_REGISTRY_REGISTER_SERVICE;
import static eu.arrowhead.common.core.CoreSystemService.SYSTEM_REGISTRY_UNREGISTER_SERVICE;

public enum CoreSystem {

    //=================================================================================================
    // elements

    SERVICE_REGISTRY(Defaults.DEFAULT_SERVICE_REGISTRY_PORT, null),
    SYSTEM_REGISTRY(Defaults.DEFAULT_SYSTEM_REGISTRY_PORT, List.of(SYSTEM_REGISTRY_REGISTER_SERVICE, SYSTEM_REGISTRY_UNREGISTER_SERVICE)),
    DEVICE_REGISTRY(Defaults.DEFAULT_DEVICE_REGISTRY_PORT, List.of(DEVICE_REGISTRY_REGISTER_SERVICE, DEVICE_REGISTRY_UNREGISTER_SERVICE)),
    ONBOARDING_CONTROLLER(Defaults.DEFAULT_ONBOARDING_PORT, List.of(ONBOARDING_WITH_CERTIFICATE_AND_NAME, ONBOARDING_WITH_SHARED_SECRET_AND_NAME,
            ONBOARDING_WITH_CERTIFICATE_AND_CSR, ONBOARDING_WITH_SHARED_SECRET_AND_CSR)),
    AUTHORIZATION(Defaults.DEFAULT_AUTHORIZATION_PORT, List.of(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE, CoreSystemService.AUTH_CONTROL_INTER_SERVICE,
            CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
            CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE)),
    ORCHESTRATOR(Defaults.DEFAULT_ORCHESTRATOR_PORT, List.of(CoreSystemService.ORCHESTRATION_SERVICE)),
    GATEKEEPER(Defaults.DEFAULT_GATEKEEPER_PORT, List.of(CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY, CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION)),
    EVENT_HANDLER(Defaults.DEFAULT_EVENT_HANDLER_PORT, List.of(CoreSystemService.EVENT_PUBLISH_SERVICE, CoreSystemService.EVENT_SUBSCRIBE_SERVICE
            , CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE, CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE)),
    GATEWAY(Defaults.DEFAULT_GATEWAY_PORT, List.of(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE, CoreSystemService.GATEWAY_PROVIDER_SERVICE, CoreSystemService.GATEWAY_CONSUMER_SERVICE)),
    CHOREOGRAPHER(Defaults.DEFAULT_CHOREOGRAPHER_PORT, List.of()), // TODO: add services
    QOS_MONITOR(Defaults.DEFAULT_QOS_MONITOR_PORT, List.of(CoreSystemService.QOS_MONITOR_PING_MEASUREMENT_SERVICE)),
    CERTIFICATE_AUTHORITY(Defaults.DEFAULT_CERTIFICATE_AUTHORITY_PORT, List.of(CoreSystemService.CERTIFICATE_AUTHORITY_SIGN_SERVICE));

    //=================================================================================================
    // members

    private final int defaultPort;
    private final List<CoreSystemService> services;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public int getDefaultPort() {
        return defaultPort;
    }

    public List<CoreSystemService> getServices() {
        return services;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private CoreSystem(final int defaultPort, final List<CoreSystemService> services) {
        Assert.isTrue(defaultPort > CommonConstants.SYSTEM_PORT_RANGE_MIN && defaultPort < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Default port is invalid.");
        this.services = services != null ? Collections.unmodifiableList(services) : List.of();
        this.defaultPort = defaultPort;
    }
}