package com.telsoft.cbs.utils;

import com.github.f4b6a3.uuid.codec.base.BaseN;
import com.github.f4b6a3.uuid.codec.base.BaseNCodec;

/* Same Base58BtcCodec without "F" Character */
public final class CustomUUIDCodec extends BaseNCodec {
	private static final BaseN BASE_N = new BaseN("123456789ABCDEGHJKLMNPQRSTUVWXYZabcdeghijkmnopqrstuvwxyz");
	public static final CustomUUIDCodec INSTANCE = new CustomUUIDCodec();

	public CustomUUIDCodec() {
		super(BASE_N);
	}
}
