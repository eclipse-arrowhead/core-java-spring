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
     * @param plantDescriptionId ID of a Plant Description.
     * @return A set containing the IDs of all Orchestrator rules known to exist
     *         for the specified Plant Description.
     * @throws RuleStoreException
     */
    Set<Integer> readRules(int plantDescriptionId) throws RuleStoreException;


    /**
     * Replaces the current set of rules for the given Plant Description with
     * the provided set.
     *
     * @param PlantDescriptionId ID of a Plant Description.
     * @param rules Rules to set.
     * @throws RuleStoreException
     */
    void setRules(int plantDescriptionId, Set<Integer> rules) throws RuleStoreException;

    /**
     * Removes all rules belonging to the specified Plant Description.
     *
     * @param plantDescriptionId ID of a Plant Description.
     * @throws RuleStoreException
     */
    void removeRules(int plantDescriptionId) throws RuleStoreException;

}