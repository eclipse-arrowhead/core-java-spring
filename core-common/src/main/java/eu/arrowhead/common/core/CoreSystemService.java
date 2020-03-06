package eu.arrowhead.common.core;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import org.springframework.util.Assert;

import static eu.arrowhead.common.CommonConstants.AUTHORIZATION_URI;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_AUTH_CONTROL_INTER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_AUTH_CONTROL_INTRA;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_AUTH_CONTROL_SUBSCRIPTION;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_AUTH_PUBLIC_KEY;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_AUTH_TOKEN_GENERATION;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_DEVICE_REGISTRY_REGISTER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_DEVICE_REGISTRY_UNREGISTER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_EVENT_HANDLER_PUBLISH;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_EVENT_HANDLER_PUBLISH_AUTH_UPDATE;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_EVENT_HANDLER_SUBSCRIBE;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_EVENT_HANDLER_UNSUBSCRIBE;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_GATEKEEPER_GSD;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_GATEKEEPER_ICN;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_GATEWAY_CONNECT_CONSUMER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_GATEWAY_CONNECT_PROVIDER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_GATEWAY_PUBLIC_KEY;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_ONBOARDING_WITH_CERTIFICATE_AND_CSR;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_ONBOARDING_WITH_CERTIFICATE_AND_NAME;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_ONBOARDING_WITH_SHARED_SECRET_AND_CSR;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_ONBOARDING_WITH_SHARED_SECRET_AND_NAME;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_ORCH_PROCESS;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_QOS_MONITOR_PING_MEASUREMENT;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_SERVICE_REGISTRY_REGISTER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_SERVICE_REGISTRY_UNREGISTER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_SYSTEM_REGISTRY_REGISTER;
import static eu.arrowhead.common.CommonConstants.CORE_SERVICE_SYSTEM_REGISTRY_UNREGISTER;
import static eu.arrowhead.common.CommonConstants.DEVICE_REGISTRY_URI;
import static eu.arrowhead.common.CommonConstants.EVENT_HANDLER_URI;
import static eu.arrowhead.common.CommonConstants.GATEKEEPER_URI;
import static eu.arrowhead.common.CommonConstants.GATEWAY_URI;
import static eu.arrowhead.common.CommonConstants.ONBOARDING_URI;
import static eu.arrowhead.common.CommonConstants.OP_AUTH_INTER_CHECK_URI;
import static eu.arrowhead.common.CommonConstants.OP_AUTH_INTRA_CHECK_URI;
import static eu.arrowhead.common.CommonConstants.OP_AUTH_KEY_URI;
import static eu.arrowhead.common.CommonConstants.OP_AUTH_SUBSCRIPTION_CHECK_URI;
import static eu.arrowhead.common.CommonConstants.OP_AUTH_TOKEN_URI;
import static eu.arrowhead.common.CommonConstants.OP_DEVICE_REGISTRY_REGISTER_URI;
import static eu.arrowhead.common.CommonConstants.OP_DEVICE_REGISTRY_UNREGISTER_URI;
import static eu.arrowhead.common.CommonConstants.OP_EVENT_HANDLER_PUBLISH;
import static eu.arrowhead.common.CommonConstants.OP_EVENT_HANDLER_PUBLISH_AUTH_UPDATE;
import static eu.arrowhead.common.CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE;
import static eu.arrowhead.common.CommonConstants.OP_EVENT_HANDLER_UNSUBSCRIBE;
import static eu.arrowhead.common.CommonConstants.OP_GATEKEEPER_GSD_SERVICE;
import static eu.arrowhead.common.CommonConstants.OP_GATEKEEPER_ICN_SERVICE;
import static eu.arrowhead.common.CommonConstants.OP_GATEWAY_CONNECT_CONSUMER_URI;
import static eu.arrowhead.common.CommonConstants.OP_GATEWAY_CONNECT_PROVIDER_URI;
import static eu.arrowhead.common.CommonConstants.OP_GATEWAY_KEY_URI;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_CERTIFICATE_AND_CSR;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_CERTIFICATE_AND_NAME;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_SHARED_SECRET_AND_CSR;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_SHARED_SECRET_AND_NAME;
import static eu.arrowhead.common.CommonConstants.OP_ORCH_PROCESS;
import static eu.arrowhead.common.CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT;
import static eu.arrowhead.common.CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT_SUFFIX;
import static eu.arrowhead.common.CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
import static eu.arrowhead.common.CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
import static eu.arrowhead.common.CommonConstants.OP_SYSTEM_REGISTRY_REGISTER_URI;
import static eu.arrowhead.common.CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_URI;
import static eu.arrowhead.common.CommonConstants.ORCHESTRATOR_URI;
import static eu.arrowhead.common.CommonConstants.QOS_MONITOR_URI;
import static eu.arrowhead.common.CommonConstants.SERVICE_REGISTRY_URI;
import static eu.arrowhead.common.CommonConstants.SYSTEM_REGISTRY_URI;

public enum CoreSystemService {

    //=================================================================================================
    // elements

    // Authorization services
    AUTH_CONTROL_INTRA_SERVICE(CORE_SERVICE_AUTH_CONTROL_INTRA, AUTHORIZATION_URI + OP_AUTH_INTRA_CHECK_URI),
    AUTH_CONTROL_INTER_SERVICE(CORE_SERVICE_AUTH_CONTROL_INTER, AUTHORIZATION_URI + OP_AUTH_INTER_CHECK_URI),
    AUTH_TOKEN_GENERATION_SERVICE(CORE_SERVICE_AUTH_TOKEN_GENERATION, AUTHORIZATION_URI + OP_AUTH_TOKEN_URI),
    AUTH_PUBLIC_KEY_SERVICE(CORE_SERVICE_AUTH_PUBLIC_KEY, AUTHORIZATION_URI + OP_AUTH_KEY_URI),
    AUTH_CONTROL_SUBSCRIPTION_SERVICE(CORE_SERVICE_AUTH_CONTROL_SUBSCRIPTION, AUTHORIZATION_URI + OP_AUTH_SUBSCRIPTION_CHECK_URI),

    // Orchestrator services
    ORCHESTRATION_SERVICE(CORE_SERVICE_ORCH_PROCESS,
            ORCHESTRATOR_URI + OP_ORCH_PROCESS),

    // Gatekeeper services
    GATEKEEPER_GLOBAL_SERVICE_DISCOVERY(CORE_SERVICE_GATEKEEPER_GSD, GATEKEEPER_URI + OP_GATEKEEPER_GSD_SERVICE),
    GATEKEEPER_INTER_CLOUD_NEGOTIATION(CORE_SERVICE_GATEKEEPER_ICN, GATEKEEPER_URI + OP_GATEKEEPER_ICN_SERVICE),

    // Gateway services
    GATEWAY_PUBLIC_KEY_SERVICE(CORE_SERVICE_GATEWAY_PUBLIC_KEY, GATEWAY_URI + OP_GATEWAY_KEY_URI),
    GATEWAY_PROVIDER_SERVICE(CORE_SERVICE_GATEWAY_CONNECT_PROVIDER, GATEWAY_URI + OP_GATEWAY_CONNECT_PROVIDER_URI),
    GATEWAY_CONSUMER_SERVICE(CORE_SERVICE_GATEWAY_CONNECT_CONSUMER, GATEWAY_URI + OP_GATEWAY_CONNECT_CONSUMER_URI),

    // Eventhandler services
    EVENT_PUBLISH_SERVICE(CORE_SERVICE_EVENT_HANDLER_PUBLISH, EVENT_HANDLER_URI + OP_EVENT_HANDLER_PUBLISH),
    EVENT_SUBSCRIBE_SERVICE(CORE_SERVICE_EVENT_HANDLER_SUBSCRIBE, EVENT_HANDLER_URI + OP_EVENT_HANDLER_SUBSCRIBE),
    EVENT_UNSUBSCRIBE_SERVICE(CORE_SERVICE_EVENT_HANDLER_UNSUBSCRIBE, EVENT_HANDLER_URI + OP_EVENT_HANDLER_UNSUBSCRIBE),
    EVENT_PUBLISH_AUTH_UPDATE_SERVICE(CORE_SERVICE_EVENT_HANDLER_PUBLISH_AUTH_UPDATE, EVENT_HANDLER_URI + OP_EVENT_HANDLER_PUBLISH_AUTH_UPDATE),

	// CA services
	CERTIFICATE_AUTHORITY_SIGN_SERVICE(CommonConstants.CORE_SERVICE_CERTIFICATE_AUTHORITY_SIGN, CommonConstants.CERTIFICATE_AUTHRORITY_URI + CommonConstants.OP_CA_SIGN_CERTIFICATE_URI),

	// QoS Monitor services
	QOS_MONITOR_PING_MEASUREMENT_SERVICE(CORE_SERVICE_QOS_MONITOR_PING_MEASUREMENT,
            QOS_MONITOR_URI + OP_QOS_MONITOR_PING_MEASUREMENT + OP_QOS_MONITOR_PING_MEASUREMENT_SUFFIX),

	// Onboarding services
    ONBOARDING_WITH_CERTIFICATE_AND_NAME(CORE_SERVICE_ONBOARDING_WITH_CERTIFICATE_AND_NAME, ONBOARDING_URI + OP_ONBOARDING_WITH_CERTIFICATE_AND_NAME),
    ONBOARDING_WITH_CERTIFICATE_AND_CSR(CORE_SERVICE_ONBOARDING_WITH_CERTIFICATE_AND_CSR, ONBOARDING_URI + OP_ONBOARDING_WITH_CERTIFICATE_AND_CSR),
    ONBOARDING_WITH_SHARED_SECRET_AND_NAME(CORE_SERVICE_ONBOARDING_WITH_SHARED_SECRET_AND_NAME, ONBOARDING_URI + OP_ONBOARDING_WITH_SHARED_SECRET_AND_NAME),
    ONBOARDING_WITH_SHARED_SECRET_AND_CSR(CORE_SERVICE_ONBOARDING_WITH_SHARED_SECRET_AND_CSR, ONBOARDING_URI + OP_ONBOARDING_WITH_SHARED_SECRET_AND_CSR),

    // Device Registry services
    DEVICE_REGISTRY_REGISTER_SERVICE(CORE_SERVICE_DEVICE_REGISTRY_REGISTER, DEVICE_REGISTRY_URI + OP_DEVICE_REGISTRY_REGISTER_URI),
    DEVICE_REGISTRY_UNREGISTER_SERVICE(CORE_SERVICE_DEVICE_REGISTRY_UNREGISTER, DEVICE_REGISTRY_URI + OP_DEVICE_REGISTRY_UNREGISTER_URI),

    // System Registry services
    SYSTEM_REGISTRY_REGISTER_SERVICE(CORE_SERVICE_SYSTEM_REGISTRY_REGISTER, SYSTEM_REGISTRY_URI + OP_SYSTEM_REGISTRY_REGISTER_URI),
    SYSTEM_REGISTRY_UNREGISTER_SERVICE(CORE_SERVICE_SYSTEM_REGISTRY_UNREGISTER, SYSTEM_REGISTRY_URI + OP_SYSTEM_REGISTRY_UNREGISTER_URI),

    // Service Registry services
    SERVICE_REGISTRY_REGISTER_SERVICE(CORE_SERVICE_SERVICE_REGISTRY_REGISTER, SERVICE_REGISTRY_URI + OP_SERVICE_REGISTRY_REGISTER_URI),
    SERVICE_REGISTRY_UNREGISTER_SERVICE(CORE_SERVICE_SERVICE_REGISTRY_UNREGISTER, SERVICE_REGISTRY_URI + OP_SERVICE_REGISTRY_UNREGISTER_URI);

    //TODO: additional services

    //=================================================================================================
    // members

    private final String serviceDefinition;
    private final String serviceUri;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public String getServiceDefinition() {
        return serviceDefinition;
    }

    public String getServiceUri() {
        return serviceUri;
    }


    //=================================================================================================
    // constructors

	//-------------------------------------------------------------------------------------------------
	private CoreSystemService(final String serviceDefinition, final String serviceUri) {
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "Service definition is null or blank");
		Assert.isTrue(!Utilities.isEmpty(serviceUri), "Service URI is null or blank");
		
		this.serviceDefinition = serviceDefinition;
		this.serviceUri = serviceUri;
	}
}