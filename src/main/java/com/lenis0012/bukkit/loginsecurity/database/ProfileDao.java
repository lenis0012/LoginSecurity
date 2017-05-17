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
