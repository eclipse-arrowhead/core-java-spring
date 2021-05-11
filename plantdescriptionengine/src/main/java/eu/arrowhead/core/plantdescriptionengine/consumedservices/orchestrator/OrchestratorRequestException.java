package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

public class OrchestratorRequestException extends Exception {

    private OrchestratorRequestException(String message) {
        super(message);
    }

    public static OrchestratorRequestException ruleDeletionFailure(int ruleId) {
        return new OrchestratorRequestException("Failed to delete store rule with ID " + ruleId);
    }

}
