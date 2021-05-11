package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import java.util.Set;

/**
 * Interface for objects that read and write Orchestration rule info to
 * permanent storage.
 * <p>
 * The PDE needs to keep track of which Orchestration rules it has created. This
 * information is stored in memory, but it also needs to be persisted to
 * permanent storage in case the PDE is restarted. This interface defines the
 * operations of classes providing such storage.
 */
public interface RuleStore {

    /**
     * @param plantDescriptionId ID of the Plant Description whose rules should
     *                           be read.
     * @return A set containing the IDs of all Orchestrator rules known to exist
     * for the specified Plant Description.
     * @throws RuleStoreException If an exception occurs when reading rules.
     */
    Set<Integer> readRules(int plantDescriptionId) throws RuleStoreException;


    /**
     * Replaces the current set of rules for the given Plant Description with
     * the provided ones.
     *
     * @param plantDescriptionId ID of the Plant Description whose rules should
     *                           be written.
     * @throws RuleStoreException If an exception occurs when writing rules.
     */
    void writeRules(int plantDescriptionId, Set<Integer> rules) throws RuleStoreException;

    /**
     * Removes all rules belonging to the specified Plant Description.
     *
     * @param plantDescriptionId ID of the Plant Description whose rules should
     *                           be removed.
     * @throws RuleStoreException If an exception occurs when removing rules.
     */
    void removeRules(int plantDescriptionId) throws RuleStoreException;

}