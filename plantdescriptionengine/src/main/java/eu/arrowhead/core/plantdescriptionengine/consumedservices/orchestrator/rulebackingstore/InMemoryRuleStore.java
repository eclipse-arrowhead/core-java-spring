package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import java.util.HashSet;
import java.util.Set;

/**
 * Backing store that only stores data in memory.
 * <p>
 * Created for development purposes, not to be used in production.
 */
public class InMemoryRuleStore implements RuleStore {

    private Set<Integer> rules = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> readRules() {
        return new HashSet<>(rules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRules(Set<Integer> newRules) {
        rules = new HashSet<>(newRules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll() {
        rules.clear();
    }

}