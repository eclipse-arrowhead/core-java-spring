package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlRuleStoreTest {

    private final String DRIVER_CLASS_NAME = "org.h2.Driver";
    private final String CONNECTION_URL = "jdbc:h2:mem:testdb";

    private final String USERNAME = "root";
    private final String PASSWORD = "password";

    private final int plantDescriptionId = 7;

    @Test
    public void shouldWriteRules() throws RuleStoreException {
        final SqlRuleStore store = new SqlRuleStore();
        store.init(DRIVER_CLASS_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(plantDescriptionId, rules);

        final Set<Integer> storedRules = store.readRules(plantDescriptionId);
        assertEquals(rules, storedRules);
    }

    @Test
    public void shouldRemoveRules() throws RuleStoreException {
        final SqlRuleStore store = new SqlRuleStore();
        store.init(DRIVER_CLASS_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(plantDescriptionId, rules);
        store.removeRules(plantDescriptionId);

        final Set<Integer> storedRules = store.readRules(plantDescriptionId);
        assertTrue(storedRules.isEmpty());
    }

    @Test
    public void shouldRequireInitialization() {
        final Exception exception = assertThrows(IllegalStateException.class,
            () -> new SqlRuleStore().readRules(plantDescriptionId));
        assertEquals("SqlRuleStore has not been initialized.", exception.getMessage());
    }

}
