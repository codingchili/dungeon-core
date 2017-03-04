package com.codingchili.core.protocol;

import io.vertx.core.*;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;

/**
 * @author Robin Duda
 *
 * A node in the cluster, all startable services should
 * implement this class for the Launcher to accept it.
 */
public abstract class ClusterNode implements Verticle {
    protected Vertx vertx;
    protected SystemSettings settings;

    @Override
    public void init(Vertx vertx, Context context) {
        this.settings = Configurations.system();
        this.vertx = vertx;

        if (!vertx.isClustered()) {
            throw new RuntimeException(CoreStrings.ERROR_CLUSTERING_REQUIRED);
        }
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        start.complete();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}