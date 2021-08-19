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

package eu.arrowhead.core.choreographer.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import eu.arrowhead.common.filter.ArrowheadFilter;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;

public class ChoreographerExecutorNotifyAccessControlFilter extends ArrowheadFilter {
	
	@Autowired
	private ChoreographerSessionDBService sessionDBService;

	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		//TODO: implement this
		
		chain.doFilter(request, response);
	}
}
