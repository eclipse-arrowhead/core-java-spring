package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.RuleSystemBuilder;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreRuleBuilder;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreRuleDto;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import se.arkalix.dto.DtoWritable;

/**
 * Class used for creating Orchestrator rules based on connections found in
 * Plant Descriptions.
 */
public class RuleCreator {

    private final PlantDescriptionTracker pdTracker;

    public RuleCreator(PlantDescriptionTracker pdTracker) {
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
    StoreRuleDto createRule(Connection connection) {

        Objects.requireNonNull(connection, "Expected a connection");

        final String consumerId = connection.consumer().systemId();
        final String providerId = connection.producer().systemId();

        final PdeSystem consumer = pdTracker.getSystem(consumerId);
        final PdeSystem provider = pdTracker.getSystem(providerId);

        String producerPortName = connection.producer().portName();
        Port producerPort = provider.getPort(producerPortName);

        final Map<String, String> providerMetadata = provider.portMetadata(producerPortName);
        final Map<String, String> consumerMetadata = consumer.metadata().orElse(null);

        var builder = new StoreRuleBuilder()
            .consumerSystem(new RuleSystemBuilder()
                .systemName(consumer.systemName().orElse(null))
                .metadata(consumerMetadata)
                .build())
            .providerSystem(new RuleSystemBuilder()
                .systemName(provider.systemName().orElse(null))
                .metadata(providerMetadata)
                .build())
            .serviceInterfaceName(producerPort.serviceInterface().orElse(null))
            .serviceDefinitionName(producerPort.serviceDefinition());

        return builder.build();
    }

    public List<DtoWritable> createRules() {
        List<DtoWritable> rules = new ArrayList<>();
        var connections = pdTracker.getActiveConnections();

        for (var connection : connections) {
            rules.add(createRule(connection));
        }

        return rules;
    }

}
