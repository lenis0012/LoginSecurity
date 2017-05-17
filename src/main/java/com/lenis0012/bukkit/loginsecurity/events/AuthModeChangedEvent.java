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

package com.lenis0012.bukkit.loginsecurity.events;

import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * When a session's Authentication mode was changed.
 */
public class AuthModeChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerSession session;
    private final AuthMode previous;
    private final AuthMode current;

    public AuthModeChangedEvent(PlayerSession session, AuthMode previous, AuthMode current) {
        this.session = session;
        this.previous = previous;
        this.current = current;
    }

    /**
     * Get the session which had it's mode changed.
     *
     * @return Player session
     */
    public PlayerSession getSession() {
        return session;
    }

    /**
     * Get the previous auth mode of the player.
     *
     * @return AuthMode
     */
    public AuthMode getPreviousMode() {
        return previous;
    }

    /**
     * Get the new auth mode of the player.
     *
     * @return AuthMode
     */
    public AuthMode getCurrentMode() {
        return current;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
