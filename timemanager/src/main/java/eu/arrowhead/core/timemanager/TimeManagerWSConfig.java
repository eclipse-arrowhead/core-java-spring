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
package eu.arrowhead.core.timemanager;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.core.timemanager.TimeWSHandler;
import eu.arrowhead.core.timemanager.service.TimeManagerDriver;

//import eu.arrowhead.core.datamanager.security.DatamanagerACLFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

 
@Configuration
@EnableWebSocket
public class TimeManagerWSConfig implements WebSocketConfigurer {
    
    //=================================================================================================
    // members
        
    private static final String SYSTEM_REF = "systemId";
    private static final String SERVICE_REF = "serviceId";

    private final Logger logger = LogManager.getLogger(TimeManagerWSConfig.class);

    @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
    private boolean sslEnabled;

    @Value(CommonConstants.$WEBSOCKETS_ENABLED_WD)
    private boolean websocketsEnabled;

    @Autowired
    private TimeWSHandler timeWSHandler;

    @Autowired
    private TimeManagerDriver timeManagerDriver;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
	    if (websocketsEnabled) {
            logger.info("WebSocket support enabled");
            webSocketHandlerRegistry.addHandler(timeWSHandler, CommonConstants.TIMEMANAGER_URI + CommonConstants.OP_TIMEMANAGER_TIME + "/ws").addInterceptors(auctionInterceptor());
	    } else {
            logger.info("WebSocket support disabled");
        }
    }

    @Bean
    public HandshakeInterceptor auctionInterceptor() {
        return new HandshakeInterceptor() {
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

                String path = request.getURI().getPath();
                final String serviceId = path.substring(path.lastIndexOf('/') + 1);
		        path = path.substring(0, path.lastIndexOf('/'));
                final String systemId = path.substring(path.lastIndexOf('/') + 1);
                String CN = null;

                if(sslEnabled) {  
                    if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
                        CN = SecurityUtilities.getCertificateCNFromServerRequest((ServletServerHttpRequest)request);
                        if(CN == null) {
                            logger.info("No valid client certificate found");
                            return false;
                        }
                        attributes.put(CommonConstants.COMMON_NAME_FIELD_NAME, CN);
                    } else {
                        return false;
                    }
                }

                // Add to the websocket session
                attributes.put(SYSTEM_REF, systemId);
                attributes.put(SERVICE_REF, serviceId);
                
                return true;
            }

            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
                // tbd
            }

        };
    }
}

