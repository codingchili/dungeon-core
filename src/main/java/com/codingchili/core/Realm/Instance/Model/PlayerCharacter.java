package com.codingchili.core.Realm.Instance.Model;

import com.codingchili.core.Configuration.LoadableConfigurable;

import java.io.Serializable;

import static com.codingchili.core.Configuration.Strings.PATH_GAME_PLAYERTEMPLATE;

/**
 * @author Robin Duda
 *         Model for player characters.
 */
public class PlayerCharacter extends Attributes implements Serializable, LoadableConfigurable {
    private String name;
    private String className;
    private Inventory inventory;

    public PlayerCharacter() {
    }

    public PlayerCharacter(PlayerCharacter template, String name, String className) {
        this.name = name;
        this.className = className;
        this.attributes = template.attributes;
        this.inventory = template.inventory;
    }

    public String getClassName() {
        return className;
    }

    public PlayerCharacter setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getName() {
        return name;
    }

    public PlayerCharacter setName(String name) {
        this.name = name;
        return this;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public String getPath() {
        return PATH_GAME_PLAYERTEMPLATE;
    }
}
