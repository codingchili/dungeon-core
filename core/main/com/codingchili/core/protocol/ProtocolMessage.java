package com.codingchili.core.protocol;

import io.vertx.core.json.JsonObject;

import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 *
 * Basic form of a request response.
 */
public abstract class ProtocolMessage {
    private ResponseStatus status = ACCEPTED;

    public ProtocolMessage() {
    }

    public ProtocolMessage(ResponseStatus status) {
        this.status = status;
    }

    public JsonObject json() {
        return Serializer.json(this);
    }

    public boolean is(ResponseStatus status) {
        return this.status.equals(status);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public ProtocolMessage setStatus(ResponseStatus status) {
        this.status = status;
        return this;
    }
}