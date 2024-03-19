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

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
class StepResponseEvaluation {
	
	private final Logger logger = LogManager.getLogger(StepResponseEvaluation.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public Boolean stepOutputValue(String message, String path, String threshold) {

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
		} else if (thresholdPair[0].equals("double")) {
			Double limit = Double.parseDouble(thresholdPair[1]);
			return Double.parseDouble(value) >= limit;
		} else if (thresholdPair[0].equals("boolean")) {
			return thresholdPair[1].equals(value);
		} else if (thresholdPair[0].equals("string")) {
			return thresholdPair[1].equals(value);
		} else {
			throw new IllegalArgumentException(
					"Invalid threshold type. The threshold type can only be double, integer, boolean or string.");
		}
	}

	public String getJsonValue(String path, String message) {
		
		String keys[] = path.split("/");

		String value = null;

		try {

			JsonNode jsonNode = objectMapper.readTree(message);

			for (int i = 0; i < keys.length - 1; i += 2) {
				if (keys[i].equals("array")) {
					if(jsonNode.size() <= Integer.parseInt(keys[i + 1]))
						throw new IndexOutOfBoundsException("The index of json array is out of range.");
					jsonNode = jsonNode.get(Integer.parseInt(keys[i + 1]));
				} else if (keys[i].equals("map")) {
					jsonNode = jsonNode.get(keys[i + 1]);
				} else if (keys[i].equals("value")) {
					jsonNode = jsonNode.get(keys[i + 1]);
					value = jsonNode.asText();
				} else {
					throw new IllegalArgumentException("The path can contain \"array\", \"map\" and \"value\" keys with the name or index of the attribute. ");
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.debug(e);
		}

		return value;

	}

}
