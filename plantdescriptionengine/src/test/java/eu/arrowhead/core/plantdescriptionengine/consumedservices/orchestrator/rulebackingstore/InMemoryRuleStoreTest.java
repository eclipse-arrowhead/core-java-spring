package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InMemoryRuleStoreTest {

    private final int plantDescriptionId = 23;

    @Test
    public void shouldReadRules() {
        final InMemoryRuleStore store = new InMemoryRuleStore();
        final Set<Integer> rules = Set.of(1, 2, 3);
        store.writeRules(plantDescriptionId, rules);
        final Set<Integer> result = store.readRules(plantDescriptionId);
        assertEquals(rules, result);
    }

    @Test
    public void shouldRemoveRules() {
        final InMemoryRuleStore store = new InMemoryRuleStore();
        store.writeRules(plantDescriptionId, Set.of(1, 2, 3));
        store.removeRules(plantDescriptionId);
        final Set<Integer> result = store.readRules(plantDescriptionId);
        assertTrue(result.isEmpty());
    }

}
