package com.lenis0012.bukkit.loginsecurity.storage;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.MySqlClob;

import javax.persistence.*;

@Entity
@Table(name="ls_inventories")
public class PlayerInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, updatable = false)
    private int id;

    @Column(columnDefinition = "TEXT")
    private String helmet;

    @Column(columnDefinition = "TEXT")
    private String chestplate;

    @Column(columnDefinition = "TEXT")
    private String leggings;

    @Column(columnDefinition = "TEXT")
    private String boots;

    @Column(columnDefinition = "TEXT")
    private String offHand;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contents;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHelmet() {
        return helmet;
    }

    public void setHelmet(String helmet) {
        this.helmet = helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public void setChestplate(String chestplate) {
        this.chestplate = chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public void setLeggings(String leggings) {
        this.leggings = leggings;
    }

    public String getBoots() {
        return boots;
    }

    public void setBoots(String boots) {
        this.boots = boots;
    }

    public String getOffHand() {
        return offHand;
    }

    public void setOffHand(String offHand) {
        this.offHand = offHand;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
