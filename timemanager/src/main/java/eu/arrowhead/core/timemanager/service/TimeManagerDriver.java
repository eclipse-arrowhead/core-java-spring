package eu.arrowhead.core.timemanager.service;

import java.util.Vector;
import java.util.Iterator;

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
	private static final Logger logger = LogManager.getLogger(TimeManagerDriver.class);

	@Value("${ntp.server.list}")
    private String serverList;

	//=================================================================================================
	// methods

	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 5)
    public void checkExternalTimeServer() {
		logger.info("Checking external time!");
	
		final NTPUDPClient client = new NTPUDPClient();
	
		client.setDefaultTimeout(10000);
		try {
		  client.open();
		  //for (final String arg : args) {
			System.out.println();
			try {
			  // Ntp pool or server
			  final InetAddress hostAddr = InetAddress.getByName("pool.ntp.org");
			  System.out.println("> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
			  
			  // Get time from server
			  final TimeInfo info = client.getTime(hostAddr);
			  info.computeDetails(); // compute offset/delay if not already done
			 
			  // For printing
			  final Long offsetMillis = info.getOffset();
			  final Long delayMillis = info.getDelay();
			  final String delay = delayMillis == null ? "n/a" : delayMillis.toString();
			  final String offset = offsetMillis == null ? "n/a" : offsetMillis.toString();
			  System.out.println(" Roundtrip delay(ms)=" + delay + ", clock offset(ms)=" + offset); // offset in ms
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
