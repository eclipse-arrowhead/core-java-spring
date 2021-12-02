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
package eu.arrowhead.core.datamanager;
 
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.core.datamanager.security.DatamanagerACLFilter;
import eu.arrowhead.core.datamanager.service.DataManagerDriver;
import eu.arrowhead.core.datamanager.service.HistorianService;

@Component
public class HistorianWSHandler extends TextWebSocketHandler {
 
    private final Logger logger = LogManager.getLogger(HistorianWSHandler.class);
    private final Gson gson = new Gson();
    
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
 
    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    @Autowired
    private HistorianService historianService;

    @Autowired
    private DataManagerDriver dataManagerDriver;

    @Autowired
    private DatamanagerACLFilter dataManagerACLFilter;

    //=================================================================================================
    // methods

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
    	logger.debug("afterConnectionEstablished started...");
    	
        sessions.add(session);
        super.afterConnectionEstablished(session);
    }
 
    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws Exception {
    	logger.debug("afterConnectionClosed started...");
    	
        sessions.remove(session);
        super.afterConnectionClosed(session, status);
    }
 
    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
    	logger.debug("handleTextMessage started...");
    	
        super.handleTextMessage(session, message);
        
        final Map<String,Object> attributes = session.getAttributes();
	    String systemName, serviceName, payload;

	    try {
		    systemName = (String) session.getAttributes().get(WSConstants.SYSTEM_ID);
		    serviceName = (String) session.getAttributes().get(WSConstants.SERVICE_ID);
            String CN = systemName;
            if (sslEnabled ) {
                CN = (String) attributes.get(WSConstants.COMMON_NAME);
            }
            
            payload = message.getPayload();

		    logger.debug("Got message for {}/{}", systemName, serviceName);
            final boolean authorized = dataManagerACLFilter.checkRequest(CN, DatamanagerACLFilter.ACL_METHOD_PUT, CommonConstants.DATAMANAGER_URI + CommonConstants.OP_DATAMANAGER_HISTORIAN + "/ws/" + attributes.get(WSConstants.SYSTEM_ID) + "/" +
            															 attributes.get(WSConstants.SERVICE_ID));
            if (authorized) {
                final Vector<SenML> sml = gson.fromJson(payload, new TypeToken<Vector<SenML>>(){}.getType());
		        dataManagerDriver.validateSenMLMessage(systemName, serviceName, sml);

		        final SenML head = sml.firstElement();
		        if (head.getBt() == null) {
			        head.setBt((double)System.currentTimeMillis() / 1000);
		        } else {
//                    double deltaTime = ((double)System.currentTimeMillis() / 1000) - head.getBt();
//                    deltaTime *= 1000.0;
		        	  //logger.info("Message took {} ms ", String.format("%.3f", deltaTime));
                }

		        dataManagerDriver.validateSenMLContent(sml);
                historianService.createEndpoint(systemName, serviceName);
    		    historianService.updateEndpoint(systemName, serviceName, sml);
            } else {
		        session.close();
            }
	    } catch (final Exception ex) {
	    	logger.debug(ex);
		    session.close();
		    return;

	    }

	    //logger.debug("Incoming msg: \n" + payload + "\n from " + systemName + "/" + serviceName); //TODO: decide if EventHandler like forwaridng should be added!
        /*sessions.forEach(webSocketSession -> {
            try {
                webSocketSession.sendMessage(message); //XXX: only send to sessions that are connected to the system+service combo!!
            } catch (IOException e) {
                logger.error("Error occurred.", e);
            }
        });*/
    }
}