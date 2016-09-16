package net.kyouko.cloudier.util;

import android.content.Context;

import net.kyouko.cloudier.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Util class for parsing date and time.
 *
 * @author beta
 */
public class DateTimeUtil {

    public static String getDateTimeDescription(Context context, String timestamp) {
        return getDateTimeDescription(context, convertTimestampToCalendar(Long.valueOf(timestamp)));
    }


    public static String getDateTimeDescription(Context context, Calendar calendar) {
        Calendar now = new GregorianCalendar();

        DateFormat timeFormat = getBestDateFormat(context, "HH:mm");
        DateFormat dateFormat = getBestDateFormat(context, "MMM d HH:mm");
        DateFormat dateFormatWithYear = getBestDateFormat(context, "MMM d, yyyy HH:mm");

        if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            if (calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
                int deltaSeconds = (now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND))
                        - (calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND));
                if (deltaSeconds == 0) {
                    return context.getString(R.string.text_time_just_now);
                } else if (deltaSeconds < 0) {
                    return timeFormat.format(calendar.getTime());
                }
                if (deltaSeconds == 1) {
                    return context.getString(R.string.text_time_1_second_ago);
                } else if (deltaSeconds < 60) {
                    return context.getString(R.string.text_time_seconds_ago, deltaSeconds);
                } else {
                    int deltaMinutes = (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE))
                            - (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                    if (deltaMinutes == 1) {
                        return context.getString(R.string.text_time_1_minute_ago);
                    } else if (deltaMinutes < 60) {
                        return context.getString(R.string.text_time_minutes_ago, deltaMinutes);
                    } else {
                        return timeFormat.format(calendar.getTime());
                    }
                }
            } else if (calendar.get(Calendar.DAY_OF_MONTH) + 1 == now.get(Calendar.DAY_OF_MONTH)) {
                return context.getString(R.string.text_time_yesterday, timeFormat.format(calendar.getTime()));
            } else {
                return dateFormat.format(calendar.getTime());
            }
        } else {
            return dateFormatWithYear.format(calendar.getTime());
        }
    }


    public static DateFormat getBestDateFormat(Context context, String pattern) {
        Locale locale = context.getResources().getConfiguration().locale;
        String formatString = android.text.format.DateFormat.getBestDateTimePattern(locale, pattern);
        return new SimpleDateFormat(formatString, locale);
    }


    public static Calendar convertTimestampToCalendar(long timestamp) {
        long timestampInMillis = timestamp * 1000;
        Date date = new Date(timestampInMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

}
