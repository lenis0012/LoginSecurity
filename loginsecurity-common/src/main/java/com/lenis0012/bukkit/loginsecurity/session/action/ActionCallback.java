package com.lenis0012.bukkit.loginsecurity.session.action;

public interface ActionCallback {
    ActionCallback EMPTY = new ActionCallback() {
        @Override
        public void call(ActionResponse response) {
        }
    };

    /**
     * Method callback.
     *
     * @param response Response
     */
    void call(ActionResponse response);
}
