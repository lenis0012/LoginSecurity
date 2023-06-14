package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.plugin.Plugin;

public class InventoryPacketListener extends PacketAdapter {

    public static void register(Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new InventoryPacketListener(plugin));
    }

    public InventoryPacketListener(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_DATA, PacketType.Play.Client.WINDOW_CLICK);
    }

    private boolean hideInvCheck(PacketEvent event) {
        if(!LoginSecurity.getConfiguration().isHideInventory()) {
            return false;
        }

        if(event.getPacket().getIntegers().read(0) != 0) {
            return false;
        }

        PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(event.getPlayer());
        if(session.isAuthorized() || !session.isRegistered()) {
            return false;
        }

        return true;
    }
    
    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (hideInvCheck(event) == true) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (hideInvCheck(event) == true) {
            event.setCancelled(true);
        }
    }
}
