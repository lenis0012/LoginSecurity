package com.lenis0012.bukkit.loginsecurity.util;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;

/**
 * Serializes inventories into database objects.
 */
public class InventorySerializer {

    public static PlayerInventory serializeInventory(final org.bukkit.inventory.PlayerInventory inventory) {
        PlayerInventory entry = new PlayerInventory();
        entry.setHelmet(serialize(inventory.getHelmet()));
        entry.setChestplate(serialize(inventory.getChestplate()));
        entry.setLeggings(serialize(inventory.getLeggings()));
        entry.setBoots(serialize(inventory.getBoots()));
        entry.setContents(serialize(inventory.getContents()));
        return entry;
    }

    public static void deserializeInventory(final PlayerInventory entry, org.bukkit.inventory.PlayerInventory inventory) {
        inventory.setHelmet((ItemStack) deserialize(entry.getHelmet()));
        inventory.setChestplate((ItemStack) deserialize(entry.getChestplate()));
        inventory.setLeggings((ItemStack) deserialize(entry.getLeggings()));
        inventory.setBoots((ItemStack) deserialize(entry.getBoots()));
        inventory.setContents((ItemStack[]) deserialize(entry.getContents()));
    }

    private static String serialize(Object item) {
        if(item == null) return null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream output = new BukkitObjectOutputStream(baos);
            output.writeObject(item);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch(IOException e) {
            LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to serialize item", e);
            return null;
        } finally {
            if(baos != null) {
                try {
                    baos.close();
                } catch(IOException e1) {}
            }
        }
    }

    private static Object deserialize(String item) {
        if(item == null) return null;
        BukkitObjectInputStream input = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(item);
            input = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
            return input.readObject();
        } catch(Exception e) {
            LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to deserialize item", e);
            return null;
        } finally {
            if(input != null) {
                try {
                    input.close();
                } catch(IOException e1) {}
            }
        }
    }
}
