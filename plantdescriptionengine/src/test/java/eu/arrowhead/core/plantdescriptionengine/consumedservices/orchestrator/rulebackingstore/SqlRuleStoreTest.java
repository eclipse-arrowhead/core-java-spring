package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlRuleStoreTest {

    @Test
    public void shouldWriteRules() throws RuleStoreException {
        final var store = new SqlRuleStore();
        store.init();
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(rules);

        var storedRules = store.readRules();
        assertEquals(rules, storedRules);
    }

    @Test
    public void shouldRemoveRules() throws RuleStoreException {
        final var store = new SqlRuleStore();
        store.init();
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(rules);
        store.removeAll();

        var storedRules = store.readRules();
        assertTrue(storedRules.isEmpty());
    }

}
