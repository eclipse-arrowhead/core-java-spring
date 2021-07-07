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
 
import java.io.IOException;
import java.util.List;
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

import eu.arrowhead.core.datamanager.service.HistorianService;
import eu.arrowhead.core.datamanager.service.DataManagerDriver;
//import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
import eu.arrowhead.common.dto.shared.SenML;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@Component
public class HistorianWSHandler extends TextWebSocketHandler {
 
    private final Logger logger = LogManager.getLogger(HistorianWSHandler.class);
    private Gson gson = new Gson();
    
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
 
    @Autowired
    private HistorianService historianService;

    @Autowired
    private DataManagerDriver dataManagerDriver;

    //=================================================================================================
    // methods

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        super.afterConnectionEstablished(session);
	    //logger.info("Got new connection!");
    }
 
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        super.afterConnectionClosed(session, status);
	    //logger.info("Connection lost!");
    }
 
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        
	    String systemName, serviceName, payload;

	    try {
		    systemName = (String) session.getAttributes().get("systemId");
		    serviceName = (String) session.getAttributes().get("serviceId");
		    payload = message.getPayload();
		    logger.debug("Got message from {}/{}", systemName, serviceName);

		    Vector<SenML> sml = gson.fromJson(payload, new TypeToken<Vector<SenML>>(){}.getType());
		    dataManagerDriver.validateSenMLMessage(systemName, serviceName, sml);

		    SenML head = sml.firstElement();
		    if(head.getBt() == null) {
			    head.setBt((double)System.currentTimeMillis() / 1000);
		    }
		    //System.out.println("bn: " + sml.get(0).getBn() + ", bt: " + sml.get(0).getBt());

		    dataManagerDriver.validateSenMLContent(sml);

		    final boolean statusCode = historianService.updateEndpoint(systemName, serviceName, sml);
		    
	    } catch(Exception e) {
		    //logger.debug("got incorrect payload");
		    session.close();
		    return;

	    }

	    logger.debug("Incoming msg: \n" + payload + "\n from " + systemName + "/" + serviceName);
            sessions.forEach(webSocketSession -> {
                try {
                    webSocketSession.sendMessage(message); //XXX: only send to sessions that are connected to the system+service combo!!
                } catch (IOException e) {
                    logger.error("Error occurred.", e);
                }
            });
    }
}

