package com.lenis0012.bukkit.loginsecurity.modules.language;

public enum LanguageKeys {
    /**
     * Admin Command
     */
    COMMAND_ERROR("commandError"),
    COMMAND_UNKNOWN("commandUnknown"),
    COMMAND_NOT_ENOUGH_ARGS("commandArguments"),
    LAC_HELP("lacHelp"),
    LAC_RMPASS("lacRmpass"),
    LAC_RMPASS_ARGS("lacRmpassArgs"),
    LAC_NOT_REGISTERED("lacNotRegistered"),
    LAC_RESET_PLAYER("lacResetPlayer"),
    LAC_IMPORT("lacImport"),
    LAC_UNKNOWN_SOURCE("lacUnknownSource"),
    LAC_IMPORT_FAILED("lacImportFailed");

    private final String value;

    LanguageKeys(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
