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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.ditto.service.DittoHttpClient;
import eu.arrowhead.core.ditto.service.DittoWsClient;

@Component
public class DittoApplicationInitListener extends ApplicationInitListener {
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private DittoHttpClient dittoHttpClient;

	@Autowired
	private DittoWsClient dittoWsClient;

	@Value("classpath:ah-ditto-policy.json")
	private Resource resourceFile;

	@Value(Constants.$GLOBAL_DITTO_POLICY)
	private String policyId;

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		if (sslProperties.isSslEnabled()) {
			logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}

		createPolicy();

		dittoWsClient.getAhDittoThings().forEach(thing -> eventPublisher.publishEvent(
			new ThingEvent(this, thing, ThingEventType.ATTACHED)
		));
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventListener(ContextClosedEvent.class)
    private void onClose(ContextClosedEvent contextClosedEvent) {		
		dittoWsClient.getAhDittoThings().forEach(thing -> {
			eventPublisher.publishEvent(
				new ThingEvent(this, thing, ThingEventType.DETACHED)
			);
		});
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of(CoreSystemService.AUTH_PUBLIC_KEY_SERVICE);
	}

	private void createPolicy() {
		String policyString = null;
		try {
			InputStreamReader reader = new InputStreamReader(resourceFile.getInputStream());
			policyString = FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (!dittoHttpClient.createPolicy(policyId, policyString).getStatusCode().is2xxSuccessful()) {
			logger.error("Could not create default policy. Retrying...");
			try {
				Thread.sleep(500l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		logger.info("Default policy created in Eclipse Ditto!");
	}
}
