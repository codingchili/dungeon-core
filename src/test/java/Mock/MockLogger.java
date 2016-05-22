package Mock;

import Authentication.Model.Account;
import Configuration.InstanceSettings;
import Configuration.RealmSettings;
import Utilities.Logger;
import io.vertx.core.http.HttpServerRequest;

/**
 * Mock implementation of a logger to disable logging.
 */
public class MockLogger implements Logger {
    @Override
    public void onServerStarted() {

    }

    @Override
    public void onServerStopped() {

    }

    @Override
    public void onInstanceStarted(RealmSettings realm, InstanceSettings instance) {

    }

    @Override
    public void onRealmStarted(RealmSettings realm) {

    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {

    }

    @Override
    public void onAuthenticated(Account account, String host) {

    }

    @Override
    public void onRegistered(Account account, String host) {

    }

    @Override
    public void onRealmRegistered(RealmSettings realm) {

    }

    @Override
    public void onRealmDeregistered(RealmSettings realm) {

    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {

    }

    @Override
    public void onRealmRejected(RealmSettings realm) {

    }

    @Override
    public void onPageLoaded(HttpServerRequest request) {

    }
}