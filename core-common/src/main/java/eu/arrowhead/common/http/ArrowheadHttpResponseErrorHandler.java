package eu.arrowhead.common.http;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

@Component
public class ArrowheadHttpResponseErrorHandler extends DefaultResponseErrorHandler {
	
	private final Logger logger = LogManager.getLogger(HttpService.class);
	
	@Override
	public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
		//TODO: proper error handling
		logger.error("Error occured at " + url + ". Returned with " + response.getRawStatusCode());
		throw new RuntimeException("Error occured at " + url + ". Returned with " + response.getRawStatusCode());
	}
}