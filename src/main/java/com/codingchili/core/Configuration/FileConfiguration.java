package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Website.Configuration.WebserverSettings;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;


/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class FileConfiguration implements ConfigurationLoader {
    private static ConfigurationLoader instance;
    private AuthServerSettings authentication;
    private LogServerSettings logserver;
    private RealmServerSettings gameserver;
    private PatchServerSettings patchserver;
    private WebserverSettings webserver;
    private RoutingSettings routing;
    private VertxSettings vertxSettings;

    private FileConfiguration() throws IOException {
        authentication = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_AUTHSERVER), AuthServerSettings.class);
        gameserver = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_GAMESERVER), RealmServerSettings.class);
        logserver = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_LOGSERVER), LogServerSettings.class);
        webserver = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_WEBSERVER), WebserverSettings.class);
        routing = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_ROUTING), RoutingSettings.class);
        vertxSettings = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_VERTX), VertxSettings.class);
        patchserver = loadPatchSettings();
        loadRealms(gameserver);
    }

    /**
     * Expose the loader for loading settings during runtime.
     *
     * @return PatchServerSettings instantiated from JSON at #Strings.PATH_PATCHSERVER
     */
    public static PatchServerSettings loadPatchSettings() throws IOException {
        return Serializer.unpack(JsonFileStore.readObject(Strings.PATH_PATCHSERVER), PatchServerSettings.class);
    }

    private void loadRealms(RealmServerSettings gameserver) {
        ArrayList<RealmSettings> realms = new ArrayList<>();
        try {
            ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(Strings.PATH_REALM);

            for (JsonObject configuration : configurations)
                realms.add(Serializer.unpack(configuration, RealmSettings.class));

            gameserver.setRealms(realms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized ConfigurationLoader instance() {
        if (instance == null) {
            try {
                instance = new FileConfiguration();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (((FileConfiguration) instance).containsAllSettings())
                TokenRefresher.refresh();
        }
        return instance;
    }

    private boolean containsAllSettings() {
        return (authentication != null && logserver != null && gameserver != null
                && patchserver != null && webserver != null && routing != null);
    }

    @Override
    public VertxSettings getVertxSettings() {
        return vertxSettings;
    }

    @Override
    public PatchServerSettings getPatchServerSettings() {
        return patchserver;
    }

    @Override
    public RealmServerSettings getGameServerSettings() {
        return gameserver;
    }

    @Override
    public LogServerSettings getLogSettings() {
        return logserver;
    }

    @Override
    public AuthServerSettings getAuthSettings() {
        return authentication;
    }

    @Override
    public WebserverSettings getWebsiteSettings() {
        return webserver;
    }

    @Override
    public RoutingSettings getRoutingSettings() {
        return routing;
    }

    void save() {
        Configurable[] configurables = {authentication, logserver, gameserver, patchserver, webserver, routing};

        for (Configurable configurable : configurables) {
            JsonFileStore.writeObject(Serializer.json(configurable), getConfigPath(configurable));
        }

        for (RealmSettings realm : gameserver.getRealms()) {
            JsonObject json = Serializer.json(realm);

            json.remove(Strings.GAME_AFFLICTIONS);
            json.remove(Strings.GAME_CLASSES);

            JsonFileStore.writeObject(json, getRealmPath(realm));
        }
    }

    private String getRealmPath(RealmSettings realm) {
        return Strings.PATH_REALM + realm.getName() + Strings.EXT_JSON;
    }

    private String getConfigPath(Configurable configurable) {
        return configurable.getPath();
    }
}