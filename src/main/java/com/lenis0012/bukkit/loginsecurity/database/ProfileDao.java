package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileDao {
    CompletableFuture<PlayerProfile> findById(int id);

    CompletableFuture<PlayerProfile> findByUniqueUserId(UUID uniqueUserId);

    CompletableFuture<PlayerProfile> findByUsername(String username);

    CompletableFuture<List<PlayerProfile>> findAll();

    CompletableFuture<Iterator<PlayerProfile>> iterateAll();

    CompletableFuture<Integer> insertProfile(PlayerProfile profile);

    CompletableFuture<Boolean> deleteProfile(PlayerProfile profile);

    CompletableFuture<Boolean> updateProfile(PlayerProfile profile);
}
