package eu.arrowhead.core.timemanager.service;

//import java.util.Vector;
//import java.util.Iterator;
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
	private final Logger logger = LogManager.getLogger(TimeManagerDriver.class);

	@Value("${ntp.server.list}")
    private String serverList;

	@Value("${time.offsetThreshold}")
	private long timeOffsetThreshold =  5000; 

	private AtomicBoolean isTimeTrusted = new AtomicBoolean(true);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public boolean isTimeTrusted() {
		return isTimeTrusted.get();
	}

	//-------------------------------------------------------------------------------------------------
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 1)
    public void checkExternalTimeServer() {
		logger.info("Checking external time!");
	
		final NTPUDPClient client = new NTPUDPClient();
	
		client.setDefaultTimeout(10000);

		try {
		  client.open();
		  //for (final String arg : args) {
			System.out.println();
			try {
			  final InetAddress hostAddr = InetAddress.getByName(serverList);
			  //logger.debug("> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
			  
			  // Get time from server
			  final TimeInfo info = client.getTime(hostAddr);
			  info.computeDetails(); // compute offset/delay if not already done
			 
			  final Long offsetMillis = info.getOffset();
			  final Long delayMillis = info.getDelay();
			  //final long delay = delayMillis == null ? "n/a" : delayMillis.toString();
			  //final Long offset = offsetMillis == null ? "n/a" : offsetMillis.toString();
			  final long offset = offsetMillis.longValue();
			  logger.debug(" Roundtrip delay(ms)=" + delayMillis + ", clock offset(ms)=" + delayMillis); // offset in ms

			  if (offsetMillis == null) {
				isTimeTrusted.set(false);
				logger.debug("Coulnd't get a response, something is wrong!");
				return;
			  }

			  if (offset < timeOffsetThreshold) {
				  isTimeTrusted.set(true);
			  } else {
				  isTimeTrusted.set(false);
				  logger.info("Time offset to large, something is wrong!");
			  }

			} catch (final IOException ioe) {
			  ioe.printStackTrace();
			}
		  //}
		} catch (final SocketException e) {
		  e.printStackTrace();
		}
	
		client.close();
	  }
	//=================================================================================================

}
