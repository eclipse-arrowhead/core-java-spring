package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.RuleSystemDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreRuleDto;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class used for creating Orchestrator rules based on connections found in
 * Plant Descriptions.
 */
public class RuleCreator {

    private final PlantDescriptionTracker pdTracker;

    public RuleCreator(final PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker.");
        this.pdTracker = pdTracker;
    }

    /**
     * Create an Orchestrator rule to be passed to the Orchestrator.
     *
     * @param connection A connection between a producer and consumer system
     *                   present in a Plant Description Entry.
     * @return An Orchestrator rule that embodies the specified connection.
     */
    StoreRuleDto createRule(final Connection connection) {

        Objects.requireNonNull(connection, "Expected a connection");

        final String consumerId = connection.consumer().systemId();
        final String providerId = connection.producer().systemId();

        final PdeSystem consumer = pdTracker.getSystem(consumerId);
        final PdeSystem provider = pdTracker.getSystem(providerId);

        final String producerPortName = connection.producer().portName();
        final Port producerPort = provider.getPort(producerPortName);

        final StoreRuleDto.Builder builder = new StoreRuleDto.Builder()
            .consumerSystem(new RuleSystemDto.Builder()
                .systemName(consumer.systemName().orElse(null))
                .metadata(consumer.metadata())
                .build())
            .providerSystem(new RuleSystemDto.Builder()
                .systemName(provider.systemName().orElse(null))
                .metadata(provider.metadata())
                .build())
            .serviceMetadata(producerPort.metadata())
            .serviceInterfaceName(producerPort.serviceInterface()
                .orElse(ApiConstants.DEFAULT_SERVICE_INTERFACE))
            .serviceDefinitionName(producerPort.serviceDefinition())
            .priority(connection.priority().orElse(null));

        return builder.build();
    }

    public List<StoreRuleDto> createRules() {
        final List<StoreRuleDto> rules = new ArrayList<>();
        final List<Connection> connections = pdTracker.getActiveConnections();

        for (final Connection connection : connections) {
            rules.add(createRule(connection));
        }

        return rules;
    }

}
