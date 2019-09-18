package eu.arrowhead.common.security;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.AuthException;

public abstract class CoreSystemAccessControlFilter extends AccessControlFilter {

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	protected void checkIfLocalSystemOperator(final String clientCN, final String cloudCN, final String requestTarget) {
		final String sysopCN = CoreCommonConstants.LOCAL_SYSTEM_OPERATOR_NAME + "." + cloudCN;
		if (!clientCN.equalsIgnoreCase(sysopCN)) {
			log.debug("Only the local system operator can use {}, access denied!", requestTarget);
		    throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	protected void checkIfClientIsAnAllowedCoreSystem(final String clientCN, final String cloudCN, final CoreSystem[] allowedCoreSystems, final String requestTarget) {
		final boolean checkResult = checkIfClientIsAnAllowedCoreSystemNoException(clientCN, cloudCN, allowedCoreSystems, requestTarget);

		if (!checkResult) {
			// client is not an allowed core system
			log.debug("Only dedicated core systems can use {}, access denied!", requestTarget);
			throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	protected boolean checkIfClientIsAnAllowedCoreSystemNoException(final String clientCN, final String cloudCN, final CoreSystem[] allowedCoreSystems, final String requestTarget) {
		for (final CoreSystem coreSystem : allowedCoreSystems) {
			final String coreSystemCN = coreSystem.name().toLowerCase() + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return true;
			}
		}
		
		return false;
	}
}