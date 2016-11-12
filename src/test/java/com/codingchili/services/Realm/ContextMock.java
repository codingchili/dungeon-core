package com.codingchili.services.Realm;

import io.vertx.core.Vertx;

import com.codingchili.core.Security.*;
import com.codingchili.core.Testing.AsyncMapMock;

import com.codingchili.services.Realm.Configuration.RealmContext;
import com.codingchili.services.Realm.Configuration.RealmSettings;
import com.codingchili.services.Realm.Instance.Model.PlayerClass;
import com.codingchili.services.Realm.Model.AsyncCharacterStore;
import com.codingchili.services.Realm.Model.HazelCharacterDB;

/**
 * @author Robin Duda
 */
public class ContextMock extends RealmContext {
    private RealmSettings realm = new RealmSettings();
    private AsyncCharacterStore characters;

    public ContextMock() {
        super(Vertx.vertx());

        realm = new RealmSettings()
                .setName("realmName")
                .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName"));

        realm.getClasses().add(new PlayerClass().setName("class.name"));

        characters = new HazelCharacterDB(new AsyncMapMock<>());
        vertx = Vertx.vertx();
    }

    @Override
    public AsyncCharacterStore getCharacterStore() {
        return characters;
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(realm.getAuthentication());
    }

    @Override
    public RealmSettings realm() {
        return realm;
    }
}