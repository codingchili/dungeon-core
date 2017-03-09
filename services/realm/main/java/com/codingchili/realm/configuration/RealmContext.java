package com.codingchili.realm.configuration;

import com.codingchili.realm.instance.configuration.InstanceSettings;
import com.codingchili.realm.instance.model.PlayerCharacter;
import com.codingchili.realm.instance.model.PlayerClass;
import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.ArrayList;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

import static com.codingchili.common.Strings.*;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 */
public class RealmContext extends ServiceContext {
    private AsyncCharacterStore characters;
    private String settings;

    public RealmContext(Vertx vertx) {
        super(vertx);
    }

    public static void create(Future<RealmContext> future, RealmSettings realm, Vertx vertx) {
        RealmContext context = new RealmContext(vertx);

        new StorageLoader<PlayerCharacter>(new StorageContext<>(vertx))
                .withPlugin(context.service().getStorage())
                .withClass(PlayerCharacter.class)
                .withCollection(COLLECTION_CHARACTERS)
                .build(prepare -> {
                    context.characters = new CharacterDB(prepare.result());
                    context.settings = realm.getPath();
                    future.complete(context);
                });
    }

    public ArrayList<EnabledRealm> getEnabled() {
        return service().getEnabled();
    }

    public AsyncCharacterStore getCharacterStore() {
        return characters;
    }

    public RealmSettings realm() {
        return Configurations.get(settings, RealmSettings.class);
    }

    public RealmServerSettings service() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }

    public String address() {
        return realm().getRemote();
    }

    public ArrayList<InstanceSettings> instances() {
        return realm().getInstances();
    }

    public ArrayList<PlayerClass> getClasses() {
        return realm().getClasses();
    }

    public PlayerCharacter getTemplate() {
        return realm().getTemplate();
    }

    public boolean verifyToken(Token token) {
        return new TokenFactory(realm().getTokenBytes()).verifyToken(token);
    }

    public int updateRate() {
        return service().getRealmUpdates();
    }

    public void onRealmStarted(String realm) {
        log(event(LOG_REALM_START, Level.STARTUP)
                .put(ID_REALM, realm));
    }

    public void onRealmRejected(String realm) {
        log(event(LOG_REALM_REJECTED, Level.WARNING)
                .put(ID_REALM, realm));
    }

    public void onRealmStopped(Future<Void> future, String realm) {
        log(event(LOG_REALM_STOP, Level.SEVERE)
                .put(ID_REALM, realm));

        Delay.forShutdown(future);
    }

    public void onRealmRegistered(String realm) {
        log(event(LOG_REALM_REGISTERED)
                .put(ID_REALM, realm));
    }

    public void onDeployRealmFailure(String realm) {
        log(event(LOG_REALM_DEPLOY_ERROR, Level.SEVERE)
                .put(LOG_MESSAGE, getDeployFailError(realm)));
    }
}
