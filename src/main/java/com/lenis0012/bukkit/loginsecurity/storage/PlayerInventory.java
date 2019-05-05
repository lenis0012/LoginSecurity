package com.lenis0012.bukkit.loginsecurity.storage;

public class PlayerInventory {
    private int id;

    private String helmet;

    private String chestplate;

    private String leggings;

    private String boots;

    private String offHand;

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
