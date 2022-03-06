/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.websocket;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.websocket.DeploymentException;

import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.tomcat.websocket.Constants;
import org.awaitility.Awaitility;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import com.rabbitmq.client.Connection;

import eu.arrowhead.core.hbconfmgr.SSLProperties;
import eu.arrowhead.core.hbconfmgr.config.InitArrowheadMockServers;
import eu.arrowhead.core.hbconfmgr.hawkbit.util.HawkbitDmfMockServer;
import eu.arrowhead.core.hbconfmgr.hawkbit.util.Message;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketControllerIT {
    
	@Value("${testParameters.token}")
    private String testToken;

    @Value("${testParameters.incorrectToken}")
    private String incorrectTestToken;

    @Autowired
    private Connection mockConnection;

//    @Autowired
//    private TrustManager[] tm;
    
    @Autowired
    private SSLProperties sslProps;
    
    @LocalServerPort
    private Integer port;

    private HawkbitDmfMockServer hawkbitMockServer;
    private StandardWebSocketClient wsClient;

    private static InitArrowheadMockServers initMockServer;

    @BeforeAll
    public static void beforeAll() throws JsonProcessingException {
        WebSocketControllerIT.initMockServer = new InitArrowheadMockServers();
        WebSocketControllerIT.initMockServer.setUp();
    }

    @BeforeEach
    public void init() throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        wsClient = new StandardWebSocketClient();
        final Map<String, Object> userProperties = new HashMap<>();
        final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
            @Override
            public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                log.error(chain);
                return true;
            }
        }).loadKeyMaterial(sslProps.getKeyStore().getURL(), sslProps.getKeyStorePassword().toCharArray(), sslProps.getKeyPassword().toCharArray())
          .build();
        

//        sslContext.init(null, tm, new java.security.SecureRandom());
        userProperties.put(Constants.SSL_CONTEXT_PROPERTY, sslContext);

        wsClient.setUserProperties(userProperties);

        // Mock the HawkBit DMF API (RabbitMQ)
        hawkbitMockServer = new HawkbitDmfMockServer(mockConnection.createChannel());
    }

    @Test
    public void testWebsocketWithCorrectTokenAndCorrectPayload()
            throws InterruptedException, ExecutionException, IOException, JSONException {
        final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBearerAuth(testToken);

        final WebSocketSession session = wsClient.doHandshake(new TextWebSocketHandler() {

        }, headers, URI.create("wss://localhost:" + port)).get();

        final String message = "{" 
            + "\"actionId\": 11,"
            + "\"softwareModuleId\": 12,"
            + "\"actionStatus\": \"DOWNLOAD\","
            + "\"message\": ["
            + "\"value\""
            + "]"
            + "}";

        session.sendMessage(new TextMessage(message));

        // Mock server should contain two messages up to this point:
        // 1. the device creation message resulting from the call of doHandshake()
        // 2. the action update status message

        Awaitility.await().atMost(Duration.ofMillis(500)).until(() -> {
            if (hawkbitMockServer.getMessages().size() == 2) {
                return true;
            } else {
                return false;
            }
        });

        final List<Message> receivedMessages = hawkbitMockServer.getMessages();
        final Message receivedUpdateActionStatus = receivedMessages.get(1);
        final String receivedUpdateActionStatusBody = receivedUpdateActionStatus.getBody();

        JSONAssert.assertEquals(message, receivedUpdateActionStatusBody, true);
    }

    @Test
    public void testWebsocketWithIncorrectToken() throws IOException, InterruptedException, ExecutionException {
        final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBearerAuth(incorrectTestToken);

        
        final Exception e = assertThrows(ExecutionException.class, () -> {
            wsClient.doHandshake(new TextWebSocketHandler(), headers, URI.create("wss://localhost:" + port)).get();
        });
        
        assertEquals(DeploymentException.class, Throwables.getRootCause(e).getClass());
    }

    @AfterAll
    public static void afterAll() {
        WebSocketControllerIT.initMockServer.shutDown();
    }
}