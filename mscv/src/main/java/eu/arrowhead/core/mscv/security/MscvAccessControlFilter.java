package eu.arrowhead.core.mscv.security;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.filter.thirdparty.MultiReadRequestWrapper;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true)
public class MscvAccessControlFilter extends CoreSystemAccessControlFilter {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            log.debug("Checking access in MscvAccessControlFilter...");
            try {
                final MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper((HttpServletRequest) request);
                final String requestTarget = Utilities.stripEndSlash(requestWrapper.getRequestURL().toString());
                final String cloudCN = getServerCloudCN();
                final String clientCN = SecurityUtilities.getCertificateCNFromRequest(requestWrapper);

                Assert.notNull(requestTarget, "Unable to determine request target");
                Assert.notNull(clientCN, "Unable to extract common name from request");

                if (requestTarget.endsWith(CommonConstants.OP_MSCV_PUBLISH_URI)) {
                    // Only event handler can use these methods
                    checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, new CoreSystem[]{CoreSystem.EVENT_HANDLER}, requestTarget);
                } else if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
                    // Only the local System Operator can use these methods
                    checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
                }

                log.debug("Using MultiReadRequestWrapper in the filter chain from now...");
                chain.doFilter(requestWrapper, response);
            } catch (final ArrowheadException ex) {
                handleException(ex, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON,
                                         final Map<String, String[]> queryParams) {
        final String cloudCN = getServerCloudCN();
        if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
            // Only the local System Operator can use these methods
            checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
        }
    }
}