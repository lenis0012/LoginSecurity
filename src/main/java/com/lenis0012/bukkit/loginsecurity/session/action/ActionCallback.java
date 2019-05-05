package com.lenis0012.bukkit.loginsecurity.session.action;

@FunctionalInterface
public interface ActionCallback {
    ActionCallback EMPTY = response -> {
    };

    /**
     * Method callback.
     *
     * @param response Response
     */
    void call(ActionResponse response);
}
