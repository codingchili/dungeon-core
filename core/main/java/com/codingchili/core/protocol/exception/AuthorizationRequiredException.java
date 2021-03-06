package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.ERROR_NOT_AUTHORIZED;

/**
 * Throw when authorization is required but was not possible, for example
 * when authentication is missing.
 */
public class AuthorizationRequiredException extends CoreRuntimeException {

    public AuthorizationRequiredException() {
        super(ERROR_NOT_AUTHORIZED, ResponseStatus.UNAUTHORIZED);
    }

    public AuthorizationRequiredException(String message) {
        super(message, ResponseStatus.UNAUTHORIZED);
    }
}
