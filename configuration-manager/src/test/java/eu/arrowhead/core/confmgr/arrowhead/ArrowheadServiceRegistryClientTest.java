/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.arrowhead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import eu.arrowhead.core.confmgr.arrowhead.model.request.ServiceRegistryRequestDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.request.SystemRequestDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.response.ServiceDefinitionResponseDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.response.ServiceInterfaceResponseDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.response.ServiceRegistryResponseDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.response.SystemResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class ArrowheadServiceRegistryClientTest {
    private static Locale defaultLocale;

    @BeforeAll
	public static void setUp()  {
		defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.UK);
	}
	
	@AfterAll
	public static void tearDown() {
		Locale.setDefault(defaultLocale);
	}

    @Test
    public void givenServiceIsNotRegistered_whenRegisterService_thenServiceIsRegistered() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody("{\n" +
                        "    \"id\": 22,\n" +
                        "    \"serviceDefinition\": {\n" +
                        "        \"id\": 7,\n" +
                        "        \"serviceDefinition\": \"definition3\",\n" +
                        "        \"createdAt\": \"2020-04-21T11:27:39.143Z\",\n" +
                        "        \"updatedAt\": \"2020-04-21T11:27:39.143Z\"\n" +
                        "    },\n" +
                        "    \"provider\": {\n" +
                        "        \"id\": 9,\n" +
                        "        \"systemName\": \"conf-system\",\n" +
                        "        \"address\": \"192.168.1.1\",\n" +
                        "        \"port\": 1234,\n" +
                        "        \"authenticationInfo\": \"test\",\n" +
                        "        \"createdAt\": \"2020-05-05T06:37:19.143Z\",\n" +
                        "        \"updatedAt\": \"2020-05-05T06:37:19.143Z\"\n" +
                        "    },\n" +
                        "    \"serviceUri\": \"/\",\n" +
                        "    \"secure\": \"TOKEN\",\n" +
                        "    \"version\": 1,\n" +
                        "    \"interfaces\": [{\n" +
                        "            \"id\": 3,\n" +
                        "            \"interfaceName\": \"HTTPS-SECURE-JSON\",\n" +
                        "            \"createdAt\": \"2020-04-21T11:27:39.143Z\",\n" +
                        "            \"updatedAt\": \"2020-04-21T11:27:39.143Z\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"createdAt\": \"2020-05-05T06:37:19.143Z\",\n" +
                        "    \"updatedAt\": \"2020-05-05T06:37:19.143Z\"\n" +
                        "}"));
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        ArrowheadServiceRegistryClient client = new ArrowheadServiceRegistryClient(baseUrl);
        ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder()
                .serviceDefinition("definition3")
                .providerSystem(SystemRequestDTO.builder()
                        .systemName("conf-system")
                        .address("192.168.1.1")
                        .port(1234)
                        .authenticationInfo("test")
                        .build())
                .serviceUri("/")
                .secure(ServiceRegistryRequestDTO.SecurityLevel.TOKEN)
                .version(1)
                .interfaces(Collections.singletonList("HTTPS-SECURE-JSON"))
                .build();
        ServiceRegistryResponseDTO responseDTO = client.registerService(requestDTO);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/serviceregistry/register");
        JSONAssert.assertEquals("{\n" +
                "    \"serviceDefinition\": \"definition3\",\n" +
                "    \"providerSystem\": {\n" +
                "        \"systemName\": \"conf-system\",\n" +
                "        \"address\": \"192.168.1.1\",\n" +
                "        \"port\": 1234,\n" +
                "        \"authenticationInfo\": \"test\"\n" +
                "    },\n" +
                "    \"serviceUri\": \"/\",\n" +
                "    \"endOfValidity\": null,\n" +
                "    \"secure\": \"TOKEN\",\n" +
                "    \"metadata\": null,\n" +
                "    \"version\": 1,\n" +
                "    \"interfaces\": [\"HTTPS-SECURE-JSON\"]\n" +
                "}", recordedRequest.getBody().readUtf8(), JSONCompareMode.NON_EXTENSIBLE);

        assertThat(responseDTO).isEqualTo(ServiceRegistryResponseDTO.builder()
                .id(22L)
                .serviceDefinition(ServiceDefinitionResponseDTO.builder()
                        .id(7L)
                        .serviceDefinition("definition3")
                        .createdAt("2020-04-21T11:27:39.143Z")
                        .updatedAt("2020-04-21T11:27:39.143Z")
                        .build())
                .provider(SystemResponseDTO.builder()
                        .id(9L)
                        .systemName("conf-system")
                        .address("192.168.1.1")
                        .port(1234)
                        .authenticationInfo("test")
                        .createdAt("2020-05-05T06:37:19.143Z")
                        .updatedAt("2020-05-05T06:37:19.143Z")
                        .build())
                .serviceUri("/")
                .secure("TOKEN")
                .version(1)
                .interfaces(Collections.singletonList(ServiceInterfaceResponseDTO.builder()
                        .id(3L)
                        .interfaceName("HTTPS-SECURE-JSON")
                        .createdAt("2020-04-21T11:27:39.143Z")
                        .updatedAt("2020-04-21T11:27:39.143Z")
                        .build()))
                .createdAt("2020-05-05T06:37:19.143Z")
                .updatedAt("2020-05-05T06:37:19.143Z")
                .build());
        mockWebServer.close();
    }

    @Test
    public void givenServiceIsAlreadyRegistered_whenRegisterService_thenWebClientResponseExceptionIsThrown() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json")
                .setBody("{\n" +
                        "    \"errorMessage\": \"Service Registry entry with provider: (conf-system, 192.168.1.1:1234) and service definition: definition3 already exists.\",\n" +
                        "    \"errorCode\": 400,\n" +
                        "    \"exceptionType\": \"INVALID_PARAMETER\"\n" +
                        "}"));
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        ArrowheadServiceRegistryClient client = new ArrowheadServiceRegistryClient(baseUrl);
        ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder()
                .serviceDefinition("definition3")
                .providerSystem(SystemRequestDTO.builder()
                        .systemName("conf-system")
                        .address("192.168.1.1")
                        .port(1234)
                        .authenticationInfo("test")
                        .build())
                .serviceUri("/")
                .secure(ServiceRegistryRequestDTO.SecurityLevel.TOKEN)
                .version(1)
                .interfaces(Collections.singletonList("HTTPS-SECURE-JSON"))
                .build();

        WebClientResponseException exception = catchThrowableOfType(() ->
                client.registerService(requestDTO), WebClientResponseException.class);

        assertThat(exception).hasMessageContaining("400 Bad Request from POST");
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        mockWebServer.close();
    }

    @Test
    public void givenServiceRegistryRequestDTOIsInvalid_whenRegisterService_thenConstraintViolationExceptionIsThrown() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        ArrowheadServiceRegistryClient client = new ArrowheadServiceRegistryClient(baseUrl);
        ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder().build();

        assertThatThrownBy(() -> client.registerService(requestDTO))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("providerSystem: must not be null")
                .hasMessageContaining("serviceDefinition: must not be null")
                .hasMessageContaining("interfaces: must not be empty")
                .hasMessageContaining("serviceUri: must not be null");
        assertThat(mockWebServer.getRequestCount()).isZero();

        mockWebServer.close();
    }

    @Test
    public void givenSystemRequestDTOIsInvalid_whenRegisterService_thenConstraintViolationExceptionIsThrown() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        ArrowheadServiceRegistryClient client = new ArrowheadServiceRegistryClient(baseUrl);
        ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder()
                .serviceDefinition("definition3")
                .providerSystem(SystemRequestDTO.builder().build())
                .serviceUri("/")
                .secure(ServiceRegistryRequestDTO.SecurityLevel.TOKEN)
                .version(1)
                .interfaces(Collections.singletonList("HTTPS-SECURE-JSON"))
                .build();

        assertThatThrownBy(() -> client.registerService(requestDTO))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("providerSystem.authenticationInfo: must not be null")
                .hasMessageContaining("providerSystem.address: must not be null")
                .hasMessageContaining("providerSystem.port: must not be null")
                .hasMessageContaining("providerSystem.systemName: must not be null");
        assertThat(mockWebServer.getRequestCount()).isZero();

        mockWebServer.close();
    }

}
