package eu.arrowhead.client.skeleton.common.context;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class CoreServiceProperties extends ConcurrentHashMap<String, CoreServiceUrl> {

	private static final long serialVersionUID = -6893735947296851421L;	
}
