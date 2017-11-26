/*
 * This file is a part of LoginSecurity.
 *
 * Copyright (c) 2017 Lennart ten Wolde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lenis0012.bukkit.loginsecurity.storage;

public class PlayerInventory extends AbstractEntity {
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
        markChanged();
        this.helmet = helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public void setChestplate(String chestplate) {
        markChanged();
        this.chestplate = chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public void setLeggings(String leggings) {
        markChanged();
        this.leggings = leggings;
    }

    public String getBoots() {
        return boots;
    }

    public void setBoots(String boots) {
        markChanged();
        this.boots = boots;
    }

    public String getOffHand() {
        return offHand;
    }

    public void setOffHand(String offHand) {
        markChanged();
        this.offHand = offHand;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        markChanged();
        this.contents = contents;
    }
}
