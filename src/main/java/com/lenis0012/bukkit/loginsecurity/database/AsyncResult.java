package com.lenis0012.bukkit.loginsecurity.database;

import lombok.Value;

@Value
public class AsyncResult<T> {
    private final boolean success;
    private final T result;
    private final Exception error;
}
