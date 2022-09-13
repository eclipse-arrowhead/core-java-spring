/********************************************************************************
 * Copyright (c) 2019 AITIA
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

package eu.arrowhead.common;


import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import eu.arrowhead.common.exception.BadPayloadException;

public class CoreUtilities {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(CoreUtilities.class);
	
	private static final  List<LogLevel> logLevelsInOrder = List.of(LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.FATAL, LogLevel.OFF);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static Direction calculateDirection(final String direction, final String origin) {
		logger.debug("calculateDirection started ...");
		final String directionStr = direction != null ? direction.toUpperCase().trim() : "";
		Direction validatedDirection;
		switch (directionStr) {
			case CoreCommonConstants.SORT_ORDER_ASCENDING:
				validatedDirection = Direction.ASC;
				break;
			case CoreCommonConstants.SORT_ORDER_DESCENDING:
				validatedDirection = Direction.DESC;
				break;
			default:
				throw new BadPayloadException("Invalid sort direction flag", org.apache.http.HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return validatedDirection;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ValidatedPageParams validatePageParameters(final Integer page, final Integer size, final String direction, final String origin) {
		logger.debug("validatePageParameters started ...");
		int validatedPage;
		int validatedSize;

		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", org.apache.http.HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = calculateDirection(direction, origin);
		
		return new ValidatedPageParams(validatedPage, validatedSize, validatedDirection);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static List<LogLevel> getLogLevels(final String maxLevel, final String origin) { 
		if (Utilities.isEmpty(maxLevel)) {
			return null;
		}
		
		try {
			final LogLevel maxLogLevel = LogLevel.valueOf(maxLevel.toUpperCase().trim());
			final int index = logLevelsInOrder.indexOf(maxLogLevel); // can't be -1 at this point
			
			return logLevelsInOrder.subList(index, logLevelsInOrder.size()); 
		} catch (final IllegalArgumentException ex) {
			throw new BadPayloadException("Defined log level is not exists.", org.apache.http.HttpStatus.SC_BAD_REQUEST, origin);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreUtilities() {
		throw new UnsupportedOperationException();
	}
	
	//=================================================================================================
	// nested classed
	
	//-------------------------------------------------------------------------------------------------
	public static class ValidatedPageParams {
		
		//=================================================================================================
		// members
		private static final int MAX_BATCH_SIZE = Integer.MAX_VALUE;

		private final int validatedPage;
		private final int validatedSize;
		private final Direction validatedDirection;
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public ValidatedPageParams(final int validatedPage, final int validatedSize, final Direction validatedDirection) {
			this.validatedPage = Math.max(validatedPage, 0);
			this.validatedSize = validatedSize < 1 ? MAX_BATCH_SIZE : validatedSize;
			this.validatedDirection = validatedDirection;
		}

		//-------------------------------------------------------------------------------------------------
		public int getValidatedPage() { return validatedPage; }
		public int getValidatedSize() { return validatedSize; }
		public Direction getValidatedDirection() { return validatedDirection; }

		public Pageable createPageable(final String... properties) {
			return PageRequest.of(validatedPage, validatedSize, validatedDirection, properties);
		}

		public Pageable createPageRequest(final String validatedSortField) {
			return PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
		}
	}
}