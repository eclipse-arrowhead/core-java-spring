package eu.arrowhead.core.datamanager.service;

import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.exception.BadPayloadException;

@Component
public class DataManagerDriver {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(DataManagerDriver.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
  	public void validateSenMLMessage(final String systemName, final String serviceName, final Vector<SenML> message) {
  		logger.debug("validateSenMLMessage started...");
  		
		try {
		    Assert.notNull(systemName, "systemName is null.");
		    Assert.notNull(serviceName, "serviceName is null.");
		    Assert.notNull(message, "message is null.");
		    Assert.isTrue(!message.isEmpty(), "message is empty");
		
		    final SenML head = (SenML)message.get(0);
		    Assert.notNull(head.getBn(), "bn is null.");
		} catch (final Exception e) {
			throw new BadPayloadException("Missing mandatory field: " + e.getMessage());
		}
    }

  	//-------------------------------------------------------------------------------------------------
    public void validateSenMLContent(final Vector<SenML> message) {
    	try {
    		/* check that bn, bt and bu are included only once, and in the first object */
    		Iterator<SenML> entry = message.iterator();
    		int bnc = 0, btc = 0, buc = 0;
    		while (entry.hasNext()) {
    			final SenML element = entry.next();
    			if (element.getBn() != null) {
    				bnc++;
    			}
    			if (element.getBt() != null) {
    				btc++;
    			}
    			if (element.getBu() != null) {
    				buc++;
    			}
    		}

	    	/* bu can only exist once. bt can only exist one, bu can exist 0 or 1 times */
	    	Assert.isTrue(!(bnc != 1 || btc != 1 || buc > 1), "invalid bn/bt/bu");

	    	/* bn must exist in [0] */
	    	SenML element = (SenML) message.get(0);
	    	Assert.notNull(element.getBn(), "bn is missing");

	    	/* bt must exist in [0] */
	    	Assert.notNull(element.getBt(), "bt is missing");

	    	/* bt cannot be negative */
	    	Assert.isTrue(element.getBt() >= 0.0, "a negative base time is not allowed");

	    	/* bu must exist in [0], if it exists */
	    	Assert.isTrue(!(element.getBu() == null && buc == 1), "invalid use of bu");

	    	/* check that v, bv, sv, etc are included only once per object */
	    	entry = message.iterator();
	    	while (entry.hasNext()) {
	    		element = (SenML) entry.next();

	      		int valueCount = 0;
	      		if (element.getV() != null) {
	        		valueCount++;
	      		}
	      		if (element.getVs() != null) {
	        		valueCount++;
	      		}
	      		if (element.getVd() != null) {
	        		valueCount++;
	      		}
	      		if (element.getVb() != null) {
	        		valueCount++;
	      		}

	      		Assert.isTrue(!(valueCount > 1 && element.getS() == null), "too many value tags");
	    	}
    	} catch (final Exception e) {
    		throw new BadPayloadException("Illegal request: " + e.getMessage());
    	}
    }
}