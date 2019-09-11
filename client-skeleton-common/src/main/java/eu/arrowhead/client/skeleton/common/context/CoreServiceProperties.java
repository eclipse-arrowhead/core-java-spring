package eu.arrowhead.client.skeleton.common.context;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import eu.arrowhead.common.core.CoreSystemService;

@Component
public class CoreServiceProperties extends ConcurrentHashMap<CoreSystemService, CoreServiceUrl> {

	private static final long serialVersionUID = -6893735947296851421L;	
}
