/********************************************************************************
 * Copyright (c) 2021 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/

package eu.arrowhead.core.timemanager.service;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;


@Component
@EnableScheduling
public class TimeManagerDriver {

	//=================================================================================================
	// members

	private static final String NTP_SERVER_LIST = "${ntp.server.list}";
	private static final String TIME_OFFSET_THRESHOLD = "${time.offset.threshold}";

	private static final int TIME_CHECK_INTERVAL = 1000 * 60;
	private static final int TIME_CHECK_INITIAL = 1000 * 1;

	private final Logger logger = LogManager.getLogger(TimeManagerDriver.class);

	@Value(NTP_SERVER_LIST)
    private String serverList;

	@Value(TIME_OFFSET_THRESHOLD)
	private long timeOffsetThreshold; 

	private AtomicBoolean isTimeTrusted = new AtomicBoolean(true);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public boolean isTimeTrusted() {
		return isTimeTrusted.get();
	}

	//-------------------------------------------------------------------------------------------------
	@Scheduled(fixedDelay = TIME_CHECK_INTERVAL, initialDelay = TIME_CHECK_INITIAL)
    public void checkExternalTimeServer() {
		logger.debug("Checking external time!");
		  
		try {
			final NTPUDPClient client = new NTPUDPClient();
			client.setDefaultTimeout(10000);
			client.open();
			final InetAddress hostAddr = InetAddress.getByName(serverList);
			final TimeInfo info = client.getTime(hostAddr);
			client.close();
			if (info == null) {
				isTimeTrusted.set(false);
				logger.debug("Couldn't get a response, something is wrong!");
				return;
			}
			info.computeDetails();
 
			if (info.getOffset() == null || info.getDelay() == null) {
				isTimeTrusted.set(false);
				logger.debug("Response details are wrong!");
				return;
			}

			final Long offsetMillis = info.getOffset();
			final Long delayMillis = info.getDelay();
			final long offset = offsetMillis.longValue();
			logger.debug("Roundtrip delay(ms)=" + delayMillis + ", clock offset(ms)=" + offsetMillis);

			if (offset < timeOffsetThreshold) {
				isTimeTrusted.set(true);
		  	} else {
			  logger.debug("Time offset too large, something is wrong!");
			  isTimeTrusted.set(false);
		  	}
		} catch (final Exception e) {
			logger.debug("Exception occured: " + e.toString());
			isTimeTrusted.set(false);
			return;
		}
	  }
	//=================================================================================================

}
