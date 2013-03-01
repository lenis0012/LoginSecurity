package com.lenis0012.bukkit.ls.encryption;

import java.math.BigInteger;

public class LoginHash1 implements Encryptor {

	@Override
	public boolean check(String check, String real) {
		check = this.hash(check);
		return check.equals(real);
	}

	public String hash(String value) {
		byte[] data = value.getBytes();
		Byte[] values = new Byte[data.length];
		for(int i = 0; i < data.length; i++)
			values[i] = Byte.valueOf(data[i]);
		byte[] hash = new byte[data.length];
		int i = 0;
		for(Byte b : values) {
			hash[i] = getFromInt((int)Math.exp(Math.ceil(b.intValue() & 0xff99ff >> 4)) + Math.abs(11 & 0xff3388 + b.intValue() & 0x19 >> 4 & 0xffffff + 11 & 0x1944f7 * 0xff12ff & 0x99ff999));
		}
		
		String result = new BigInteger(1, hash).toString(32);
		return result;
	}
	
	private byte getFromInt(int value) {
		return (byte) value;
	}
}
