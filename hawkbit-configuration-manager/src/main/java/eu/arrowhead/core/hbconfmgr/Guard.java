/********************************************************************************
 * Copyright (c) 2021 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.hbconfmgr;

import java.util.Optional;
import java.util.ServiceConfigurationError;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Guard {
	
	@Value(Constants.SSL_ENABLED)
	private boolean sslEnabled;

	@Bean
	public Optional<String> guardBean() {
		if (!sslEnabled) {
			throw new ServiceConfigurationError("HawkBit Configuration Manager only supports SECURE mode. Please set server.ssl.enabled to true in the application.properties file.");
		}
		
		return Optional.empty();
	}
}