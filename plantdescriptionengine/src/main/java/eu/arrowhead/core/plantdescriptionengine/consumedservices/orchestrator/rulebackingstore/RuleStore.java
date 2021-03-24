package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import java.util.Set;

/**
 * Interface for objects that read and write Orchestration rule info to
 * permanent storage.
 * <p>
 * The PDE needs to keep track of which Orchestration rules it has created, and
 * to which Plant Description Entry each rule belongs. This information is
 * stored in memory, but it also needs to be persisted to permanent storage in
 * case the PDE is restarted. This interface defines the operations of classes
 * providing such storage.
 */
public interface RuleStore {

    /**
     * @return A set containing the IDs of all Orchestrator rules currently stored
     * by this instance.
     */
    Set<Integer> readRules() throws RuleStoreException;

    /**
     * Replaces the current set of rules with the one provided.
     */
    void setRules(Set<Integer> rules) throws RuleStoreException;

    /**
     * Removes all stored rules.
     */
    void removeAll() throws RuleStoreException;

}