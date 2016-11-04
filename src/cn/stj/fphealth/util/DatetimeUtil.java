
package cn.stj.fphealth.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatetimeUtil {

    /**
     * 判断当前日期为星期几
     * 
     * @param date
     * @return
     */
    public static int dayOfWeek(Date date) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(date);
        int weekDay = aCalendar.get(Calendar.DAY_OF_WEEK);
        return weekDay;
    }
    
    public static String format(long dateTime,String pattern) {
        return new SimpleDateFormat(pattern).format(dateTime);
    }

}
