package eu.arrowhead.core.gams.security;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.filter.thirdparty.MultiReadRequestWrapper;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true)
public class GamsAccessControlFilter extends CoreSystemAccessControlFilter {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            log.debug("Checking access in GamsAccessControlFilter...");
            try {
                final MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper((HttpServletRequest) request);
                final String requestTarget = Utilities.stripEndSlash(requestWrapper.getRequestURL().toString());

                if (Objects.requireNonNull(requestTarget).contains(CoreCommonConstants.MGMT_URI)) {
                    // Only the local System Operator can use these methods
                    final String cloudCN = getServerCloudCN();
                    final String clientCN = SecurityUtilities.getCertificateCNFromRequest(requestWrapper);

                    if (clientCN == null) {
                        log.error("Unauthorized access: {}", requestTarget);
                        throw new AuthException("Unauthorized access: " + requestTarget);
                    }
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
}