package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * Class that reads and writes Orchestration rules to file.
 * <p>
 * The PDE needs to keep track of which Orchestration rules it has created, and
 * to which Plant Description Entry each rule belongs. This information is
 * stored in memory, but it also needs to be persisted to permanent storage in
 * case the PDE is restarted. This class provides that functionality, writing
 * rules and their relationship to Plant Descriptions to file.
 */
public class FileRuleStore implements RuleStore {

    private static final Logger logger = LoggerFactory.getLogger(FileRuleStore.class);

    // File path to the directory for storing the IDs of Orchestration rules
    // created by the PDE:
    private final String ruleDirectory;

    /**
     * Class constructor.
     *
     * @param ruleDirectory File path to the directory for storing rules.
     */
    public FileRuleStore(final String ruleDirectory) {
        Objects.requireNonNull(ruleDirectory, "Expected path to Orchestrator Rule directory");
        this.ruleDirectory = ruleDirectory;
    }

    /**
     * @param plantDescriptionId Plant Description ID
     * @return File for storing Orchestration rules for the specified Plant
     * Description.
     */
    private File getRuleFile(final int plantDescriptionId) {
        return Paths.get(ruleDirectory, String.valueOf(plantDescriptionId)).toFile();
    }

    @Override
    public void writeRules(final int plantDescriptionId, final Set<Integer> rules) throws RuleStoreException {
        Objects.requireNonNull(rules, "Expected rules.");

        final File file = getRuleFile(plantDescriptionId);

        // Create the file and parent directories, if they do not already
        // exist:
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuleStoreException("Failed to create directory for storing Orchestrator rules.");
            }
        }

        try {
            if (file.createNewFile()) {
                logger.info("Created a file for storing Orchestrator rules.");
            }
        } catch (final IOException e) {
            throw new RuleStoreException("Failed to create orchestration rule file.", e);
        }

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write each rule ID on a single line:
            for (final Integer rule : rules) {
                writer.write(rule.toString());
                writer.newLine();
            }

        } catch (final IOException e) {
            throw new RuleStoreException("Failed to write orchestration rules to file", e);
        }
    }

    @Override
    public Set<Integer> readRules(final int plantDescriptionId) throws RuleStoreException {
        final File file = getRuleFile(plantDescriptionId);
        final Set<Integer> result = new HashSet<>();

        if (!file.isFile()) {
            return result;
        }

        try (final Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextInt()) {
                result.add(scanner.nextInt());
            }
        } catch (final FileNotFoundException e) {
            throw new RuleStoreException(e);
        }
        return result;
    }

    @Override
    public void removeRules(final int plantDescriptionId) throws RuleStoreException {
        final File file = getRuleFile(plantDescriptionId);
        if (!file.delete()) {
            throw new RuleStoreException("Failed to delete orchestration rule file.");
        }
    }
}