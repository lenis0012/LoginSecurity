package com.lenis0012.bukkit.loginsecurity.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.persistence.*;

@Entity
@Table(name = "ls_locations")
public class PlayerLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private int id;

    @Column
    private String world;

    @Column
    private double x;

    @Column
    private double y;

    @Column
    private double z;

    @Column
    private int yaw;

    @Column
    private int pitch;

    public PlayerLocation() {}

    public PlayerLocation(Location location) {
        setWorld(location.getWorld().getName());
        setX(location.getX());
        setY(location.getY());
        setZ(location.getZ());
        setYaw((int) location.getYaw());
        setPitch((int) location.getPitch());
    }

    public Location asLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
}
