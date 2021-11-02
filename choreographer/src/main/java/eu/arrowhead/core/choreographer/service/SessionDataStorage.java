package eu.arrowhead.core.choreographer.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@SuppressWarnings("serial")
@Component
public class SessionDataStorage extends ConcurrentHashMap<Long,SessionExecutorCache> {
}