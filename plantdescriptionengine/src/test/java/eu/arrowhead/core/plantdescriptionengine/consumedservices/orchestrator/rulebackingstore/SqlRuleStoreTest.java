package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SqlRuleStoreTest {

    private final String DRIVER_CLASS_NAME = "org.h2.Driver";
    private final String CONNECTION_URL = "jdbc:h2:mem:testdb";
    private final String USERNAME = "root";
    private final String PASSWORD = "password";
    private final int plantDescriptionId = 7;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldWriteRules() throws RuleStoreException {
        final SqlRuleStore store = new SqlRuleStore();
        store.init(DRIVER_CLASS_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.writeRules(plantDescriptionId, rules);

        final Set<Integer> storedRules = store.readRules(plantDescriptionId);
        assertEquals(rules, storedRules);
    }

    @Test
    public void shouldRemoveRules() throws RuleStoreException {
        final SqlRuleStore store = new SqlRuleStore();
        store.init(DRIVER_CLASS_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.writeRules(plantDescriptionId, rules);
        store.removeRules(plantDescriptionId);

        final Set<Integer> storedRules = store.readRules(plantDescriptionId);
        assertTrue(storedRules.isEmpty());
    }

    @Test
    public void shouldRequireInitialization() throws RuleStoreException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("SqlRuleStore has not been initialized.");
        new SqlRuleStore().readRules(plantDescriptionId);
    }

}
