package eu.arrowhead.client.skeleton.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.http.HttpService;

@Component
public class ArrowheadService {
	
	@Autowired
	private HttpService httpService;
	
	public void echoServiceRegistry() {
		ResponseEntity<String> response = httpService.sendRequest(Utilities.createURI("http", "127.0.0.1", 8443, "/serviceregistry/echo"), HttpMethod.GET, String.class);
		System.out.println(response.getBody());
	}
}
