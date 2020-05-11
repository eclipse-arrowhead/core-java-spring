package eu.arrowhead.core.mscv.handlers;

import java.util.Queue;

import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.core.mscv.service.MscvException;

public interface ExecutionHandler<T extends Target> {
    void login(T target, String username, String password) throws MscvException;
    boolean verifyPasswordlessLogin(T target);
    // this only makes sense for remote command execution handlers, but we don't support anything else yet ...
    void executeScripts(final Queue<Script> scriptsQueue, final T sshTarget);

    Class<T> getType();
}
