package eu.arrowhead.core.orchestrator.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.OrchestrationFlags;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class OrchestratorAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);

		final String cloudCN = getServerCloudCN();
		if (requestTarget.contains(CommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (Utilities.isEmpty(requestJSON)) {
			// If request body is empty (example: GET..../orchestrator/{systemId}), than everybody in the local cloud can use these methods => no further check is necessary
		} else {
			final OrchestrationFormRequestDTO orchestrationFormRequestDTO = Utilities.fromJson(requestJSON, OrchestrationFormRequestDTO.class);
			final OrchestrationFlags orchestrationFlags = orchestrationFormRequestDTO.getOrchestrationFlags();
			
			if (orchestrationFlags.getOrDefault(CommonConstants.ORCHESTRATON_FLAG_EXTERNAL_SERVICE_REQUEST, false)) {
				// If this is an external service request, only the local Gatekeeper can use these methods
				final CoreSystem[] allowedCoreSystems = { CoreSystem.GATEKEEPER };
				checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystems, requestTarget);				
			} else {
				// Otherwise all request from the local cloud are allowed, but requester system has to be match with the certificate
				checkIfRequesterSystemNameisEqualsWithClientNameFromCN(orchestrationFormRequestDTO.getRequesterSystem().getSystemName(), clientCN);				
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkIfRequesterSystemNameisEqualsWithClientNameFromCN(final String requesterSystemName, final String clientCN) {
		final String clientNameFromCN = getClientNameFromCN(clientCN);
		if(!requesterSystemName.equalsIgnoreCase(clientNameFromCN) && !requesterSystemName.replaceAll("_", "").equalsIgnoreCase(clientNameFromCN)) {
			log.debug("Requester system name and client name from certificate do not match!");
			throw new AuthException("Requester system name(" + requesterSystemName + ") and client name from certificate (" + clientNameFromCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}