package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 */
public class WritableConfigurable implements Configurable {
    private String path;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }
}
