package vn.com.telsoft.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

	public static long getDateDiff(Date startDate, Date endDate, TimeUnit timeUnit) {
		long diffInMillies = endDate.getTime() - startDate.getTime();
		return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}

}
