package Protocols;

import Protocols.Authorization.Token;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 */
class ClusterMessage implements Request {
    private Buffer buffer;
    private JsonObject json;
    private Message message;

    ClusterMessage(Message message) {
        if (message.body() instanceof Buffer) {
            this.buffer = (Buffer) message.body();
        } if (message.body() instanceof  String) {
            this.json = new JsonObject((String) message.body());
        } else {
            this.json = (JsonObject) message.body();
        }

        this.message = message;
    }

    @Override
    public void error() {
        message.reply(message(PROTOCOL_ERROR, PROTOCOL_ERROR));
    }

    private JsonObject message(String action, String message) {
        return new JsonObject().put(action, message);
    }

    @Override
    public void unauthorized() {
        message.reply(message(PROTOCOL_ERROR, PROTOCOL_UNAUTHORIZED));
    }

    @Override
    public void write(Object object) {
        message.reply(object);
    }

    @Override
    public void accept() {
        message.reply(message(PROTOCOL_ACTION, PROTOCOL_ACCEPTED));
    }

    @Override
    public void missing() {
        message.reply(message(PROTOCOL_ERROR, PROTOCOL_MISSING));
    }

    @Override
    public void conflict() {
        message.reply(message(PROTOCOL_ERROR, PROTOCOL_CONFLICT));
    }

    @Override
    public String action() {
        return json.getString(ID_ACTION);
    }

    @Override
    public Token token() {
        return Serializer.unpack(json.getJsonObject(ID_TOKEN), Token.class);
    }

    @Override
    public JsonObject data() {
        return json;
    }

    public Buffer buffer() {
        return buffer;
    }

    @Override
    public int timeout() {
        return 0;
    }
}
