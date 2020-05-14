package eu.arrowhead.core.mscv.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.Target;
import org.springframework.util.Assert;

// Bean created in Main and pre-loaded with all execution handlers
public class ExecutionHandlerFactory {

    private final Map<Class<?>, ExecutionHandler<?>> registrar;
    public ExecutionHandlerFactory() { super();
        registrar = new HashMap<>();
    }

    public void register(final Class<? extends Target> cls, final ExecutionHandler<? extends Target> handler) {
        Assert.notNull(cls, "Class must not be null");
        Assert.notNull(handler, "ExecutionHandler must not be null");
        registrar.put(cls, handler);
    }

    public <T extends Target, H extends ExecutionHandler<T>> Optional<H> find(final T probe) {
        Assert.notNull(probe, "Probing object must not be null");
        return find((Class<T>) probe.getClass());
    }
    public <T extends Target, H extends ExecutionHandler<T>> Optional<H> find(final Class<T> cls) {
        Assert.notNull(cls, "Class must not be null");
        return Optional.ofNullable((H)registrar.get(cls));
    }
}
