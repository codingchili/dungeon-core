package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Authentication.Model.AsyncAccountStore;
import com.codingchili.core.Authentication.Model.AsyncRealmStore;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Authentication.RealmRegister;
import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.Realm.CharacterResponse;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import io.vertx.core.Future;

import static com.codingchili.core.Configuration.Strings.*;
import static com.codingchili.core.Protocols.Access.AUTHORIZED;
import static com.codingchili.core.Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 *         Routing used to authenticate realms and generate realmName lists.
 */
public class AuthenticationHandler extends AbstractHandler {
    private Protocol<RequestHandler<AuthenticationRequest>> protocol = new Protocol<>();
    private AsyncRealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;
    private Logger logger;
    private TokenFactory tokens;

    public AuthenticationHandler(AuthProvider provider) {
        super(NODE_AUTHENTICATION_REALMS);

        logger = provider.getLogger();
        accounts = provider.getAccountStore();
        settings = provider.getAuthserverSettings();
        realmStore = provider.getRealmStore();
        tokens = provider.getClientTokenFactory();

        protocol.use(REALM_REGISTER, this::register, PUBLIC)
                .use(REALM_UPDATE, this::update)
                .use(CLIENT_CLOSE, this::disconnected)
                .use(REALM_CHARACTER_REQUEST, this::character);
    }

    private Access authenticate(Request request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? AUTHORIZED : PUBLIC;
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(authenticate(request), request.action()).handle((AuthenticationRequest) request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
            logger.onHandlerMissing(request.action());
        }
    }

    private void register(AuthenticationRequest request) {
        Future<Void> realmFuture = Future.future();
        RealmSettings realm = request.realm();

        realm.setTrusted(settings.isTrustedRealm(realm.getName()));

        realmFuture.setHandler(insert -> {
            if (insert.succeeded()) {
                request.write(new RealmRegister(true));
            } else {
                request.error();
            }
        });

        realmStore.put(realmFuture, realm);
    }

    private void update(AuthenticationRequest request) {
        Future<Void> updateFuture = Future.future();
        String realmName = request.realmName();
        int players = request.players();

        updateFuture.setHandler(update -> {
            if (update.succeeded()) {
                request.write(new RealmRegister(true));
            } else {
                request.error();
            }
        });

        realmStore.update(updateFuture, realmName, players);
    }

    private void disconnected(AuthenticationRequest request) {
        Future<Void> realmFuture = Future.future();

        realmFuture.setHandler(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error();
            }
        });

        realmStore.remove(realmFuture, request.realm().getName());
    }

    private void character(AuthenticationRequest request) {
        Future<PlayerCharacter> find = Future.future();

        find.setHandler(result -> {
            if (result.succeeded()) {
                request.write(new CharacterResponse(result.result(), request.sender()));
            } else
                request.error();
        });
        accounts.findCharacter(find, request.realmName(), request.account(), request.name());
    }
}