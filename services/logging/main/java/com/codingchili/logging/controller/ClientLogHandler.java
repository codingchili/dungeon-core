package com.codingchili.logging.controller;

import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Request;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 *
 * Log handler for messages incoming from clients.
 */
public class ClientLogHandler<T extends LogContext> extends AbstractLogHandler<T> {

    public ClientLogHandler(T context) {
        super(context, NODE_CLIENT_LOGGING);
    }

    @Override
    protected void log(Request request) {
        JsonObject logdata = request.data();

        if (verifyToken(logdata)) {
            logdata.remove(ID_TOKEN);
            logdata.remove(PROTOCOL_ROUTE);
            console.log(logdata);
            store.log(logdata);
        } else {
            request.error(new AuthorizationRequiredException());
        }
    }

    private boolean verifyToken(JsonObject logdata) {
        JsonObject token = (JsonObject) logdata.remove(ID_TOKEN);
        return context.verifyToken(Serializer.unpack(token, Token.class));
    }
}