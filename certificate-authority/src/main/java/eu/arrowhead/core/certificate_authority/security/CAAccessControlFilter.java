package eu.arrowhead.core.certificate_authority.security;

import java.util.Map;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true)
public class CAAccessControlFilter extends CoreSystemAccessControlFilter {

	// =================================================================================================
	// members
	private static final CoreSystem[] allowedCoreSystemsForTrustedKeyHandling = { CoreSystem.ONBOARDING_CONTROLLER };
	private static final CoreSystem[] allowedCoreSystemsForCertificateHandling = { CoreSystem.ONBOARDING_CONTROLLER };

	// =================================================================================================
	// assistant methods

	// -------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget,
			final String requestJSON, final Map<String, String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);

		final String cloudCN = getServerCloudCN();

		if (requestTarget.contains(CommonConstants.OP_CA_MGMT_TRUSTED_KEYS_URI)
				|| requestTarget.contains(CommonConstants.OP_CA_CHECK_TRUSTED_KEY_URI)) {
			checkIfClientIsAnAllowedCoreSystemOrSysop(clientCN, cloudCN, allowedCoreSystemsForTrustedKeyHandling,
					requestTarget);
		}

		if (requestTarget.contains(CommonConstants.OP_CA_MGMT_CERTIFICATES_URI)
				|| requestTarget.contains(CommonConstants.OP_CA_SIGN_CERTIFICATE_URI)) {
			checkIfClientIsAnAllowedCoreSystemOrSysop(clientCN, cloudCN, allowedCoreSystemsForCertificateHandling,
					requestTarget);
		}

		if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		}
	}

	private void checkIfClientIsAnAllowedCoreSystemOrSysop(final String clientCN, final String cloudCN,
			final CoreSystem[] allowedCoreSystems, final String requestTarget) {

		boolean result = checkIfClientIsAnAllowedCoreSystemNoException(clientCN, cloudCN,
				allowedCoreSystemsForTrustedKeyHandling, requestTarget);


		if (!result) {
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		}
	}
}
