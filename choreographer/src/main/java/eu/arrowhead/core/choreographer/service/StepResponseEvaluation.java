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

class StepResponseEvaluation {

	private String getJsonValue(String path, String message) {
		ObjectMapper objectMapper = new ObjectMapper();

		String keys[] = path.split("/");

		String value = null;

		try {

			JsonNode jsonNode = objectMapper.readTree(message);

			for (int i = 0; i < keys.length - 1; i += 2) {
				if (keys[i].equals("array")) {
					jsonNode = jsonNode.get(keys[i + 1]);
					jsonNode = jsonNode.get(Integer.parseInt(keys[i + 2]));
				} else if (keys[i].equals("map")) {
					jsonNode = jsonNode.get(keys[i + 1]);
				} else if (keys[i].equals("value")) {
					jsonNode = jsonNode.get(keys[i + 1]);
					value = jsonNode.asText();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return value;

	}

	public Boolean stepOutputValue(String message, String path, String threshold) {
		// if sth used is null throw exception
		if (path == null || message == null || threshold == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}

		// extract value from message given in path
		String value = getJsonValue(path, message);

		// extract value from threshold
		// convert the extracted value to wrapper type
		// compare and return
		String[] thresholdPair = threshold.split(":");
		if (thresholdPair[0].equals("Integer")) {
			Integer limit = Integer.parseInt(thresholdPair[1]);
			if (Integer.parseInt(value) >= limit)
				return true;
			else
				return false;
		} else if (thresholdPair[0].equals("Long")) {
			Long limit = Long.parseLong(thresholdPair[1]);
			if (Long.parseLong(value) >= limit)
				return true;
			else
				return false;
		} else if (thresholdPair[0].equals("Boolean")) {
			Boolean limit;
			if (thresholdPair[1].equals(value))
				return true;
			else
				return false;
		} else if (thresholdPair[0].equals("String")) {
			String limit;
			if (thresholdPair[1].equals(value))
				return true;
			else
				return false;
		} else {
			throw new IllegalArgumentException(
					"Invalid threshold type. The threshold type can only be Long, Integer, Boolean or String.");
		}
	}
}
