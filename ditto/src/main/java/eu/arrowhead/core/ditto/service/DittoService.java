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

package eu.arrowhead.core.ditto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class DittoService implements ApplicationListener<ThingEvent> {

	// =================================================================================================
	// members

	@Autowired
	private ServiceRegistryClient serviceRegistryClient;

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@Override
	public void onApplicationEvent(ThingEvent event) {
		serviceRegistryClient.registerService();
	}

}
