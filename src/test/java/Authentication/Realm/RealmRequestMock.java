package Authentication.Realm;

import Authentication.Controller.RealmRequest;
import Protocols.Util.Serializer;
import Routing.Controller.Transport.RealmConnection;
import Protocols.Util.Token;
import Realm.Configuration.RealmSettings;
import Shared.ResponseListener;
import Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.ID_ACTION;

/**
 * @author Robin Duda
 */
public class RealmRequestMock implements RealmRequest {
    private ResponseListener listener;
    private JsonObject data;
    private String action;

    RealmRequestMock(JsonObject data, ResponseListener listener, String action) {
        this.data = data;
        this.listener = listener;
        this.action = action;
        this.data.put(ID_ACTION, action);
    }

    @Override
    public RealmSettings realm() {
        return null;
    }

    @Override
    public int players() {
        return 0;
    }

    @Override
    public String realmName() {
        return null;
    }

    @Override
    public String sender() {
        return null;
    }

    @Override
    public RealmConnection connection() {
        return null;
    }

    @Override
    public Token token() {
        return null;
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return 0;
    }

    @Override
    public String account() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void error() {
        listener.handle(null, ResponseStatus.ERROR);
    }

    @Override
    public void write(Object object) {
        listener.handle(Serializer.json(object), ResponseStatus.ACCEPTED);
    }

    @Override
    public void unauthorized() {
        listener.handle(null, ResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void missing() {
        listener.handle(null, ResponseStatus.MISSING);
    }

    @Override
    public void conflict() {
        listener.handle(null, ResponseStatus.CONFLICT);
    }

    @Override
    public String action() {
        return action;
    }


    @Override
    public void accept() {
        listener.handle(null, ResponseStatus.ACCEPTED);
    }


    @Override
    public String target() {
        return null;
    }
}
