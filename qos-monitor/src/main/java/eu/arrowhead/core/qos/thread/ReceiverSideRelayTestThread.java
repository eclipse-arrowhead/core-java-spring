package eu.arrowhead.core.qos.thread;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReceiverSideRelayTestThread extends Thread implements MessageListener {
	
	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(ReceiverSideRelayTestThread.class);
	
	private boolean interrupted = false;
	private boolean initialized = false;

	
	//=================================================================================================
	// methods

	//TODO: implementation

	public ReceiverSideRelayTestThread() {
		// TODO Auto-generated constructor stub
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}


	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub

	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void interrupt() {
		logger.debug("interrupt started...");
		
		super.interrupt();
		interrupted = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setInterrupted(final boolean interrupted) { this.interrupted = interrupted; }

}