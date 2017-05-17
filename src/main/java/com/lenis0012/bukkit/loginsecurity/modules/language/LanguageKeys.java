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
    LOGIN_SUCCESS("loginSuccess"),
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
    LAC_RELOAD("lacReload"),
    LAC_RELOAD_COMPLETE("lacReloadComplete"),
    LAC_RMPASS("lacRmpass"),
    LAC_RMPASS_ARGS("lacRmpassArgs"),
    LAC_NOT_REGISTERED("lacNotRegistered"),
    LAC_RESET_PLAYER("lacResetPlayer"),
    LAC_IMPORT("lacImport"),
    LAC_UNKNOWN_SOURCE("lacUnknownSource"),
    LAC_IMPORT_FAILED("lacImportFailed"),
    /**
     * Messages
     */
    SESSION_CONTINUE("sessionContinue"),
    MESSAGE_LOGIN("messageLogin"),
    MESSAGE_REGISTER("messageRegister"),
    /**
     * Errors
     */
    ERROR_REFRESH_PROFILE("errorRefreshProfile"),
    ERROR_NOT_REGISTERED("errorNotRegistered"),
    /**
     * Kick messages
     */
    KICK_ALREADY_ONLINE("kickAlreadyOnline"),
    KICK_USERNAME_CHARS("kickUsernameChars"),
    KICK_USERNAME_LENGTH("kickUsernameLength"),
    KICK_TIME_OUT("kickTimeOut");

    private final String value;

    LanguageKeys(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
