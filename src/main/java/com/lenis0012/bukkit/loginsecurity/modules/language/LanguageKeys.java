package com.lenis0012.bukkit.loginsecurity.modules.language;

public enum LanguageKeys {
    /**
     * General errors
     */
    GENERAL_NOT_AUTHENTICATED("generalNotAuthenticated"),
    GENERAL_NOT_LOGGED_IN("generalNotLoggedIn"),
    GENERAL_PASSWORD_LENGTH("generalPasswordLength"),
    GENERAL_UNKNOWN_HASH("generalUnknownHash"),
    /**
     * Login command
     */
    LOGIN_TRIES_EXCEEDED("loginTriesExceeded"),
    LOGIN_FAIL("loginFail"),
    LOGIN_SUCCESS("loginSeuccess"),
    /**
     * Changepass command
     */
    CHANGE_FAIL("changeFail"),
    CHANGE_SUCCESS("changeSuccess"),
    /**
     * Logout command
     */
    LOGOUT_FAIL("logoutFail"),
    LOGOUT_SUCCESS("logoutSuccess"),
    /**
     * Register command
     */
    REGISTER_ALREADY("registerAlready"),
    REGISTER_CAPTCHA("registerCaptcha"),
    REGISTER_SUCCESS("registerSuccess"),
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
    LAC_IMPORT_FAILED("lacImportFailed"),
    /**
     * Errors
     */
    ERROR_REFRESH_PROFILE("errorRefreshProfile"),
    ERROR_NOT_REGISTERED("errorNotRegistered");

    private final String value;

    LanguageKeys(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
