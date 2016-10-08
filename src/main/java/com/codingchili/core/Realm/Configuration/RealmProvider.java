package com.codingchili.core.Realm.Configuration;

import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

import static com.codingchili.core.Configuration.Strings.NODE_REALM;

/**
 * @author Robin Duda
 */
public class RealmProvider implements Provider {
    private RealmServerSettings server;
    private RealmSettings realm;
    private Logger logger;
    private Vertx vertx;

    public RealmProvider(Vertx vertx, RealmServerSettings server, RealmSettings realm) {
        this.server = server;
        this.realm = realm;
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, realm.getAuthentication());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public RealmServerSettings getServer() {
        return server;
    }

    public RealmSettings getRealm() {
        return realm;
    }
}