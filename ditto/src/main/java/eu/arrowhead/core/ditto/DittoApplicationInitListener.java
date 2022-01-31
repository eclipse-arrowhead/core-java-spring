/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.ditto;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class DittoApplicationInitListener extends ApplicationInitListener {

	// =================================================================================================
	// assistant methods

	// -------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		if (sslProperties.isSslEnabled()) {
			logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}
	}

	// -------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of(CoreSystemService.ORCHESTRATION_SERVICE);
	}
}