package com.lenis0012.bukkit.loginsecurity.util;

import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.Filter.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.logging.log4j.core.Filter.Result.DENY;
import static org.apache.logging.log4j.core.Filter.Result.NEUTRAL;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class LoggingFilterTest {

	@Parameterized.Parameters
	public static Collection<Object[]> parameters() {
		LoginSecurityConfig shortcutsEnabled = Mockito.mock(LoginSecurityConfig.class);
		Mockito.when(shortcutsEnabled.isUseCommandShortcut()).thenReturn(true);
		Mockito.when(shortcutsEnabled.getLoginCommandShortcut()).thenReturn("/l");
		Mockito.when(shortcutsEnabled.getRegisterCommandShortcut()).thenReturn("/reg");
		LoginSecurityConfig shortcutsDisabled = Mockito.mock(LoginSecurityConfig.class);
		Mockito.when(shortcutsEnabled.isUseCommandShortcut()).thenReturn(true);
		return Arrays.asList(new Object[][] {
				/* shortcuts enabled */
				{new LoggingFilter(shortcutsEnabled), "/l qwerty", DENY},
				{new LoggingFilter(shortcutsEnabled), "/luckperms help", NEUTRAL},
				{new LoggingFilter(shortcutsEnabled), "Hey, use /l to log in!", NEUTRAL},
				{new LoggingFilter(shortcutsEnabled), "/reg qwerty", DENY},
				{new LoggingFilter(shortcutsEnabled), "Hey, use /reg to register!", NEUTRAL},
				{new LoggingFilter(shortcutsEnabled), "_voidpointer issued server command: /l qwerty", DENY},
				{new LoggingFilter(shortcutsEnabled), "_voidpointer issued server command: /reg qwerty", DENY},
				/* shortcuts disabled */
				{new LoggingFilter(shortcutsDisabled), "/l qwerty", NEUTRAL},
				{new LoggingFilter(shortcutsEnabled), "/luckperms help", NEUTRAL},
				{new LoggingFilter(shortcutsDisabled), "Hey, use /l to log in!", NEUTRAL},
				{new LoggingFilter(shortcutsDisabled), "/reg qwerty", NEUTRAL},
				{new LoggingFilter(shortcutsDisabled), "Hey, use /reg to register!", NEUTRAL},
				{new LoggingFilter(shortcutsDisabled), "_voidpointer issued server command: /l qwerty", NEUTRAL},
				{new LoggingFilter(shortcutsDisabled), "_voidpointer issued server command: /reg qwerty", NEUTRAL},
		});
	}

	private final LoggingFilter loggingFilter;
	private final String message;
	private final Result expected;

	@Test
	public void testDenyIfExposesPassword() {
		Assert.assertEquals(expected, loggingFilter.denyIfExposesPassword(message));
	}
}