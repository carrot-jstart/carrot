package org.jstart.carrot.console.comm.utils;


import org.jstart.carrot.console.comm.ConstantFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * 日期处理类
 */
public final class CalendarUtil {
    static final Long ONE_DAYMS = 86400000L;
    private static final int ONE_SECONDS = 1000;

    private CalendarUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取当前时区时间
     * @param origin
     */
    public static Calendar toLocal(Calendar origin) {
        if(origin == null) {
            return null;
        } else if(origin.getTimeZone().equals(TimeZone.getDefault())) {
            return origin;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(origin.getTimeInMillis());
            return calendar;
        }
    }

    /**
     * 计算两个Calendar对象之间相隔的天数
     * 它通过将两个时间戳相减得到时间差，然后将时间差除以一天的毫秒数得到相隔的天数。最后将结果转换为整数并返回。
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public static int daysBetween(Calendar timeStart, Calendar timeEnd) {
        long time1 = timeStart.getTimeInMillis();
        long time2 = timeEnd.getTimeInMillis();
        long betweenDays = (time2 - time1) / ONE_DAYMS;
        return Integer.parseInt(String.valueOf(betweenDays));
    }

    /**
     * 获取当前时间
     */
    public static Calendar getNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar;
    }

    /**
     * 该函数用于获取当前日期并根据参数days向后偏移指定天数的日期
     * @param days
     * @return
     */
    public static Calendar getNow(int days) {
        Calendar calendar = getNow();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar;
    }

    /**
     * 将一个Calendar对象转换为日期字符串
     * 然后再将该日期字符串转换为一个新的Calendar对象返回。如果转换过程中发生异常，则捕获异常并进行日志记录，最后返回null
     * @param time
     * @return
     */
    public static Calendar fromCalendarInDate(Calendar time) {
        Calendar calendar = null;
        if(time != null) {
            try {
                String ex = toDateStr(time);
                calendar = fromDateStr(ex);
            } catch (Exception e) {
                //TODO log
            }
        }

        return calendar;
    }

    /**
     * 将一个长整型时间戳（毫秒为单位）转换为Calendar对象
     * @param ms
     * @return
     */
    public static Calendar fromMillisInDate(Long ms) {
        Calendar calendar = fromMillis(ms);
        return CalendarUtil.fromCalendarInDate(calendar);
    }

    /**
     * 将一个长整型时间戳（毫秒为单位）转换为Calendar对象。
     * 它通过实例化一个Calendar对象，并使用setTimeInMillis方法将时间戳设置为指定的毫秒数，
     * 最后返回这个设置了时间的Calendar对象。这样可以方便地对日期时间进行进一步的操作和计算
     * @param ms
     * @return
     */
    public static Calendar fromMillis(Long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);
        return calendar;
    }

    /**
     * 将java.sql.Timestamp类型转换为java.util.Calendar类型。
     * @param ts
     * @return
     */
    public static Calendar fromTimestamp(Timestamp ts) {
        Calendar calendar = null;
        if(ts != null) {
            calendar = Calendar.getInstance();
            long time = ts.getTime();
            calendar.setTimeInMillis(time);
        }
        return calendar;
    }

    /**
     * 通过yyyy-MM-dd获取日期
     * @param dateStr 日期字符串
     */
    public static Calendar fromDateStr(String dateStr) {
        return CalendarUtil.fromStr(dateStr, ConstantFactory.STR_DATE);
    }

    /**
     * 通过yyyy-MM-dd HH:mm:ss.SSS获取日期
     * @param dateTimeMSStr 带毫秒的时间字符串
     */
    public static Calendar fromDateTimeMSStr(String dateTimeMSStr) {
        Calendar calendar = null;
        if(StringUtil.isNotNullOrEmpty(dateTimeMSStr) && !"null".equals(dateTimeMSStr)) {
            calendar = CalendarUtil.fromStr(dateTimeMSStr, ConstantFactory.STR_DATE_FULLMS);
        }
        return calendar;
    }

    /**
     * 通过yyyy-MM-dd HH:mm:ss获取日期
     * @param dateTimeStr 时间字符串
     */
    public static Calendar fromDateTimeStr(String dateTimeStr) {
        return CalendarUtil.fromStr(dateTimeStr, ConstantFactory.STR_DATE_FULL);
    }

    /**
     * 通过指定时间格式，获取Calendar对象
     * @param dateStr 时间字符串
     * @param format 时间格式
     */
    public static Calendar fromStr(String dateStr, String format) {
        Calendar calendar;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = sdf.parse(dateStr);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException var5) {
            //TODO log
            return null;
        }
    }

    public static String toNowDateStr() {
        return CalendarUtil.toDateStr(getNow());
    }

    public static String toNowDateTimeStr() {
        return CalendarUtil.toDateTimeStr(getNow());
    }

    public static String toNowDateTimeMSStr() {
        return CalendarUtil.toDateTimeMSStr(getNow());
    }

    public static String toDateStr(Calendar calendar) {
        return CalendarUtil.toDateTimeStr(calendar, ConstantFactory.STR_DATE);
    }

    public static String toDateTimeStr(Calendar calendar) {
        return CalendarUtil.toDateTimeStr(calendar, ConstantFactory.STR_DATE_FULL);
    }

    public static String toDateTimeMSStr(Calendar calendar) {
        return CalendarUtil.toDateTimeStr(calendar, ConstantFactory.STR_DATE_FULLMS);
    }

    public static String toDateTimeStr(Calendar calendar, String format) {
        if(null == calendar) {
            calendar = CalendarUtil.fromDateTimeStr(ConstantFactory.STR_DATE_DEFAULT);
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(calendar.getTime());
    }

    public static String toStringByTimestamp(Timestamp ts) {
        Calendar calendar = CalendarUtil.fromTimestamp(ts);
        return CalendarUtil.toDateTimeStr(calendar, ConstantFactory.STR_DATE_FULLMS);
    }

    /**
     * 获取当前时间戳
     * @return 当前时间戳
     */
    public static Timestamp getNowTimestamp() {
        return new Timestamp(CalendarUtil.nowTimestamp());
    }

    /**
     * 获取当前时间戳
     */
    public static Long nowTimestamp() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 获取当前时间戳
     */
    public static String nowTimestampStr() {
        return Long.toString(CalendarUtil.nowTimestamp());
    }

    public static Calendar get1970() {
        Calendar result;
        try {
            result = fromStr("1970-01-01 08:00:01", ConstantFactory.STR_DATE_FULL);
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    public static Integer getAge(Calendar birthDay) {
        if(birthDay==null) {
            return 0;
        }
        Calendar now = getNow();

        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int nowDay = now.get(Calendar.DAY_OF_MONTH);

        int birthDayYear= birthDay.get(Calendar.YEAR);
        int birthDayMonth = birthDay.get(Calendar.MONTH);
        int birthDayDay = birthDay.get(Calendar.DAY_OF_MONTH);

        int age = nowYear - birthDayYear;
        if(nowMonth < birthDayMonth) {
            age--;
        } else if(nowMonth == birthDayMonth && nowDay < birthDayDay) {
            age--;
        } else {
            age = 0;
        }
        return age;
    }

    public static Integer getAge(String birthDay) {
        Calendar cBirthDay = CalendarUtil.fromStr(birthDay, ConstantFactory.STR_DATE);
        return getAge(cBirthDay);
    }

    /**
     * 方法为获取时间段内的所有日期
     * @param dateFrom 时间起
     * @param dateEnd  时间至
     * @return List<Long> 返回该时间段内的所有日期的时间戳队列。
     * @throws ParseException
     */
    public static List<String> getDates(String dateFrom, String dateEnd) throws ParseException {
        return CalendarUtil.getDates(dateFrom, dateEnd, "-1");
    }

    /**
     * 方法为指定时间段内获取指定周几的所有日期
     * @param dateFrom 时间起
     * @param dateEnd  时间至
     * @param weekDays 需要在时间段中指定查询的周数，建议使用“,”隔开（不隔开也没问题），需要获取时间段内所有日期时参数为"-1"。星期日=0，星期一=1，星期二=2，星期三=3，星期四=4，星期五=5，星期六=6。
     * @return List<Long> 返回符合条件日期的时间戳队列。
     * @throws ParseException
     */
    public static List<String> getDates(String dateFrom, String dateEnd, String weekDays) throws ParseException {
        boolean isAll = weekDays.contains("-1");
        long time;
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConstantFactory.STR_DATE);
        dateFrom = simpleDateFormat.format(simpleDateFormat.parse(dateFrom).getTime() - ONE_DAYMS);
        while (true) {
            time = simpleDateFormat.parse(dateFrom).getTime();
            time = time + ONE_DAYMS;
            Date date = new Date(time);
            dateFrom = simpleDateFormat.format(date);
            if (dateFrom.compareTo(dateEnd) <= 0) {
                if (isAll) {
                    dateList.add(dateFrom);
                } else {
                    Integer weekDay = dayForWeek(date);
                    if (weekDays.contains(weekDay.toString())) {
                        dateList.add(dateFrom);
                    }
                }
            } else {
                break;
            }
        }
        return dateList;
    }

    private static Integer dayForWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static Timestamp get1970Timestamp() {
        Timestamp result;
        try {
            Calendar date = get1970();
            if(null!=date) {
                result = new Timestamp(date.getTimeInMillis());
            } else {
                result = null;
            }
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * 从指定格式时间字符串，获取时间戳
     */
    public static Timestamp getTimestamp(String dateStr, String format) {
        Long timspamp = Long.parseLong("1000");
        Calendar date = CalendarUtil.fromStr(dateStr, format);
        if(null!=date) {
            timspamp = date.getTimeInMillis();
        }
        return new Timestamp(timspamp);
    }

    /**
     * 获取今日 00:00:00
     */
    public static Calendar getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String today = CalendarUtil.toDateTimeStr(calendar, ConstantFactory.STR_DATE);
        return CalendarUtil.fromStr(today, ConstantFactory.STR_DATE);
    }

    /**
     * 获取本月第一天零时 xxxx-xx-01 00:00:00
     */
    public static Calendar getCurrentMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String toMonth = CalendarUtil.toDateTimeStr(calendar, "yyyy-MM")+"-01";
        return CalendarUtil.fromStr(toMonth, ConstantFactory.STR_DATE);
    }

    /**
     * 从指定Calendar获取时间戳
     */
    public static Timestamp getTimestamp(Calendar date) {
        Long timspamp = Long.parseLong("1000");
        if(null!=date) {
            timspamp = date.getTimeInMillis();
        }
        return new Timestamp(timspamp);
    }

    public static boolean isValid(Calendar calendar) {
        if(null==calendar|| calendar.compareTo(CalendarUtil.get1970())<0) {
            return false;
        }
        return true;
    }
    public static boolean isValid(Timestamp timestamp) {
        if(null==timestamp || timestamp.compareTo(CalendarUtil.get1970Timestamp())<0) {
            return false;
        }
        return true;
    }
    public static boolean isValid(String dateStr, String format) {
        Calendar calendar = CalendarUtil.fromStr(dateStr, format);
        if(null==calendar || calendar.compareTo(CalendarUtil.get1970())<0) {
            return false;
        }
        return true;
    }

    public static Integer getTimestampSeconds(Calendar time) {
        long mils = time.getTimeInMillis();
        String timestamp = String.valueOf(mils / ONE_SECONDS);
        return Integer.valueOf(timestamp);
    }
}