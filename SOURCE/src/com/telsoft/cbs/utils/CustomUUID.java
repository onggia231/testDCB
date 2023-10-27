package com.telsoft.cbs.utils;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.codec.base.Base58BtcCodec;

import java.util.Date;
import java.util.UUID;

public class CustomUUID {


	//https://www.wolframalpha.com/input/?i=convert+1582-10-15+UTC+to+unix+time
	final static long START_OF_UUID_RELATIVE_TO_UNIX_EPOCH_SECONDS = 12219292800L;
	final static long START_OF_UUID_RELATIVE_TO_UNIX_EPOCH_MILLIS = START_OF_UUID_RELATIVE_TO_UNIX_EPOCH_SECONDS * 1000L;

	public static UUID generateType1UUID() {
		return UuidCreator.getTimeBased();
	}

	public static UUID generateType1UUIDWithMAC() {
		return UuidCreator.getTimeBasedWithMac();
	}

	/**
	 * timestamp() from UUID is measured in 100-nanosecond units since midnight, October 15, 1582 UTC,
	 * so we must convert for 100ns units to millisecond procession
	 *
	 * @param tuid
	 * @return
	 */
	public static long getMillisecondsFromUuid(UUID tuid) {

		assert tuid.version() == 1;      //ensure its a time based UUID

		// timestamp returns in 10^-7 (100 nano second chunks),
		// java Date constructor  assumes 10^-3 (millisecond precision)
		// so we have to divide by 10^4 (10,000) to get millisecond precision
		return tuid.timestamp() / 10000L - START_OF_UUID_RELATIVE_TO_UNIX_EPOCH_MILLIS;

	}

	public static Date getDateFromUuid(UUID tuid) {
		assert tuid.version() == 1; //ensure its a time based UUID
		// Allocates a Date object and initializes it to represent the specified number of milliseconds since the
		// standard java (unix) base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT
		// have to add relative offset from UUID start date of unix epoch to get start date in unix time milliseconds
		return new Date(tuid.timestamp() / 10000L - START_OF_UUID_RELATIVE_TO_UNIX_EPOCH_MILLIS);
	}

	public static String genCustomCodecUUIDVer1(){
		UUID uuid = CustomUUID.generateType1UUID();
		return CustomUUIDCodec.INSTANCE.encode(uuid);
	}

	public static Date getDateFromCustomCodecUUID(String customCodecUUID){
		UUID uuid = CustomUUIDCodec.INSTANCE.decode(customCodecUUID);
		return CustomUUID.getDateFromUuid(uuid);
	}


}
