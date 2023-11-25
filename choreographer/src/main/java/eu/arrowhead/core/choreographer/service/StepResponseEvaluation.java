/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.choreographer.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.lang.String;

import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
class StepResponseEvaluation {
	
	private final Logger logger = LogManager.getLogger(StepResponseEvaluation.class);
	
	public Boolean stepOutputValue(String message, String path, String threshold) {
		// if sth used is null throw exception
		if (path == null || message == null || threshold == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}

		// extract value from message given in path
		String value = getJsonValue(path, message);
		if(value == null)
			throw new IllegalArgumentException("Unable to restore value from path");

		// extract value from threshold
		// convert the extracted value to wrapper type
		// compare and return
		String[] thresholdPair = threshold.split(":");
		if(thresholdPair.length != 2)
			throw new IllegalArgumentException("The threshold valuable must have two component, seperated by a \":\" character.");
		
		if (thresholdPair[0].equals("integer")) {
			Integer limit = Integer.parseInt(thresholdPair[1]);
			return Integer.parseInt(value) >= limit;
		} else if (thresholdPair[0].equals("long")) {
			Long limit = Long.parseLong(thresholdPair[1]);
			return Long.parseLong(value) >= limit;
		} else if (thresholdPair[0].equals("boolean")) {
			return thresholdPair[1].equals(value);
		} else if (thresholdPair[0].equals("string")) {
			return thresholdPair[1].equals(value);
		} else {
			throw new IllegalArgumentException(
					"Invalid threshold type. The threshold type can only be long, integer, boolean or string.");
		}
	}

	private String getJsonValue(String path, String message) {
		ObjectMapper objectMapper = new ObjectMapper();

		String keys[] = path.split("/");

		String value = null;

		try {

			JsonNode jsonNode = objectMapper.readTree(message);

			for (int i = 0; i < keys.length - 1; i += 2) {
				if (keys[i].equals("array")) {
					jsonNode = jsonNode.get(keys[i + 1]);
					if(jsonNode.size() <= Integer.parseInt(keys[i + 1]))
						throw new IndexOutOfBoundsException("The index of json array is out of range.");
					jsonNode = jsonNode.get(Integer.parseInt(keys[i + 1]));
				} else if (keys[i].equals("map")) {
					jsonNode = jsonNode.get(keys[i + 1]);
				} else if (keys[i].equals("value")) {
					jsonNode = jsonNode.get(keys[i + 1]);
					value = jsonNode.asText();
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.debug(e);
		}

		return value;

	}

}
