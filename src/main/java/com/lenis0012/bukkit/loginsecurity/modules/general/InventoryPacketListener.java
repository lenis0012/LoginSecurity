package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import org.bukkit.plugin.Plugin;

public class InventoryPacketListener extends PacketAdapter {

    public static void register(Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new InventoryPacketListener(plugin));
    }

    public InventoryPacketListener(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_DATA);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if(!LoginSecurity.getConfiguration().isHideInventory()) {
            return;
        }

        if(event.getPacket().getIntegers().read(0) != 0) {
            return;
        }

        if(LoginSecurity.getSessionManager().getPlayerSession(event.getPlayer()).isAuthorized()) {
            return;
        }

        event.setCancelled(true);
    }
}
