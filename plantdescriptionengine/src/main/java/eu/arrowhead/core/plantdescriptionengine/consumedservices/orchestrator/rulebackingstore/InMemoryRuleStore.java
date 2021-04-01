package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backing store that only stores data in memory.
 * <p>
 * Created for development purposes, not to be used in production.
 */
public class InMemoryRuleStore implements RuleStore {

    private Map<Integer, Set<Integer>> rulesPerEntry = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> readRules(final int plantDescriptionId) {
        return rulesPerEntry.getOrDefault(plantDescriptionId, new HashSet<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRules(final int plantDescriptionId, final Set<Integer> newRules) {
        Objects.requireNonNull(newRules, "Expected rules.");
        rulesPerEntry.put(plantDescriptionId, newRules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRules(final int plantDescriptionId) {
        rulesPerEntry.remove(plantDescriptionId);
    }

}