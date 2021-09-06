/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.drivers;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static eu.arrowhead.common.CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE;
import static eu.arrowhead.common.CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS;
import static eu.arrowhead.common.CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT;
import static eu.arrowhead.common.CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME;

@Service
public class EventDriver extends AbstractDriver {

    //=================================================================================================
    // members
	
	private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Logger logger = LogManager.getLogger(EventDriver.class);

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	@Autowired
    public EventDriver(final DriverUtilities driverUtilities, final HttpService httpService) {
        super(driverUtilities, httpService);
    }

    //-------------------------------------------------------------------------------------------------
	public void publish(final EventPublishRequestDTO request) throws DriverUtilities.DriverException {
        logger.traceEntry("publish: {}", request);
        Assert.notNull(request, "EventPublishRequestDTO must not be null");

        final UriComponents uri = driverUtilities.findUri(CoreSystemService.EVENT_PUBLISH_SERVICE);
        httpService.sendRequest(uri, HttpMethod.POST, Void.class, request);
    }

    //-------------------------------------------------------------------------------------------------
	public void subscribe(final SubscriptionRequestDTO request) throws DriverUtilities.DriverException {
        logger.traceEntry("subscribe: {}", request);
        Assert.notNull(request, "SubscriptionRequestDTO must not be null");

        final UriComponents uri = driverUtilities.findUri(CoreSystemService.EVENT_SUBSCRIBE_SERVICE);
        httpService.sendRequest(uri, HttpMethod.POST, Void.class, request);
    }

    //-------------------------------------------------------------------------------------------------
	public void unsubscribe(final String eventType, final String subscriberName,
                            final String subscriberAddress, final int subscriberPort) throws DriverUtilities.DriverException {
        logger.traceEntry("unsubscribe: ({},{},{},{})", eventType, subscriberName, subscriberAddress, subscriberPort);

        final UriComponents uri = driverUtilities.findUri(CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE);
        final UriComponents uriParams = UriComponentsBuilder.newInstance()
                                                            .uriComponents(uri)
                                                            .queryParam(OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE, eventType)
                                                            .queryParam(OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME, subscriberName)
                                                            .queryParam(OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS, subscriberAddress)
                                                            .queryParam(OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT, subscriberPort)
                                                            .build();
        httpService.sendRequest(uriParams, HttpMethod.DELETE, Void.class);
    }

    //-------------------------------------------------------------------------------------------------
	public void publishSubscriberAuthorizationUpdate(final EventPublishRequestDTO request) throws DriverUtilities.DriverException {
        logger.traceEntry("publishSubscriberAuthorizationUpdate: {}", request);
        Assert.notNull(request, "EventPublishRequestDTO must not be null");

        final UriComponents uri = driverUtilities.findUri(CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE);
        httpService.sendRequest(uri, HttpMethod.POST, Void.class, request);
    }

    //-------------------------------------------------------------------------------------------------
	// null is a valid input for object
    public String convert(final Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    //-------------------------------------------------------------------------------------------------
	// null is a valid input for String
    public <T> T convert(final String string, final Class<T> clz) throws IOException {
        Assert.notNull(clz, "Class<T> must not be null");
        return mapper.readValue(string, clz);
    }
}