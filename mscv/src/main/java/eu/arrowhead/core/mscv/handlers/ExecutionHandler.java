package eu.arrowhead.core.mscv.handlers;

import java.util.Collection;

import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.core.mscv.service.MscvException;

public interface ExecutionHandler<T extends Target> {
    void login(T target, String username, String password) throws MscvException;

    boolean verifyPasswordlessLogin(T target);

    void performVerification(final VerificationResult intermediateResult, final Collection<VerificationResultDetail> resultDetails) throws MscvException;

    Class<T> getType();
}
