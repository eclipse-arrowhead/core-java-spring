package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    private static final Logger logger = LoggerFactory.getLogger(RuleStore.class);

    // File path to the directory for storing the IDs of created Orchestration
    // rules created by the PDE:
    private final String ruleStoreFile;

    /**
     * Class constructor.
     *
     * @param ruleStoreDirectory File path to the directory for storing rules.
     */
    public FileRuleStore(final String ruleStoreDirectory) {
        Objects.requireNonNull(ruleStoreDirectory, "Expected path to Orchestrator Rule directory");
        this.ruleStoreFile = ruleStoreDirectory + "/orchestration_rules.txt";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> readRules() throws RuleStoreException {
        final File file = new File(ruleStoreFile);
        final var result = new HashSet<Integer>();

        if (!file.isFile()) {
            return result;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextInt()) {
                result.add(scanner.nextInt());
            }
        } catch (FileNotFoundException e) {
            throw new RuleStoreException(e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRules(Set<Integer> rules) throws RuleStoreException {

        final File file = new File(ruleStoreFile);

        try {
            // Create the file and parent directories, if they do not already
            // exist:
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new RuleStoreException("Failed to create directory for storing Orchestrator rules.");
                }
            }

            if (file.createNewFile()) {
                logger.info("Created a file for storing Orchestrator rules.");
            }

            // Write each rule ID on a single line:
            final var writer = new BufferedWriter(new FileWriter(file));
            for (Integer rule : rules) {
                writer.write(rule.toString());
                writer.newLine();
            }

            writer.close();

        } catch (IOException e) {
            throw new RuleStoreException("Failed to write orchestration rule to file", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuleStoreException
     */
    @Override
    public void removeAll() throws RuleStoreException {
        if (!new File(ruleStoreFile).delete()) {
            throw new RuleStoreException("Failed to delete orchestration rule directory");
        }
    }
}