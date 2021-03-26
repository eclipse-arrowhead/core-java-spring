package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryRuleStoreTest {

    @Test
    public void shouldReadRules() {
        final InMemoryRuleStore store = new InMemoryRuleStore();
        final Set<Integer> rules = Set.of(1, 2, 3);
        store.setRules(rules);
        final Set<Integer> result = store.readRules();
        assertEquals(rules, result);
    }

    @Test
    public void shouldRemoveRules() {
        final InMemoryRuleStore store = new InMemoryRuleStore();
        store.setRules(Set.of(1, 2, 3));
        store.removeAll();
        final Set<Integer> result = store.readRules();
        assertTrue(result.isEmpty());
    }

}
