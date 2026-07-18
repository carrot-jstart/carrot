package org.jstart.carrot.console.comm.utils;

import org.jstart.carrot.console.comm.ConstantFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * jdk8时间日期处理 工具类
 */
public final class LocalDateTimeUtil {
    private LocalDateTimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 默认时间格式化对象
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(ConstantFactory.STR_DATE_FULL);
    /**
     * 默认时间格式化对象
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER_DATE = DateTimeFormatter.ofPattern(ConstantFactory.STR_DATE);
    /**
     * 默认时间(1970-01-01 00:00:00)
     */
    public static final LocalDateTime DEFAULT_DATETIME = LocalDateTime.parse(ConstantFactory.STR_DATE_DEFAULT, DEFAULT_FORMATTER);
    /**
     * 默认时间(1970-01-01)
     */
    public static final LocalDate DEFAULT_DATE = LocalDate.parse(ConstantFactory.STR_DATE_DEFAULT_DATE, DEFAULT_FORMATTER_DATE);

    //************************************** 【LocalDateTime】 **************************************
    /**
     * 获取当前LocalDateTime
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
    /**
     * 获取当前时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String nowDateTimeStr() {
        return LocalDateTimeUtil.toFormatString(LocalDateTime.now());
    }
    /**
     * 获取当前时间字符串（yyyy-MM-dd HH:mm:ss.SSS）
     */
    public static String nowDateTimeFullStr() {
        return LocalDateTimeUtil.toFormatFullString(LocalDateTime.now());
    }
    /**
     * 通过时间戳构建LocalDateTime
     * @param timestamp 时间戳
     */
    public static LocalDateTime getDateTime(Long timestamp) {
        if(NumericUtil.tryParseLong(timestamp).compareTo(0L)<1) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    /**
     * 通过时间戳构建LocalDateTime
     * @param timestamp 时间戳
     */
    public static LocalDateTime getDateTime(Timestamp timestamp) {
        if(null==timestamp) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return LocalDateTimeUtil.getDateTime(timestamp.getTime());
    }
    /**
     * 通过时间字符串和指定时间格式(例：yyyy-MM-dd HH:mm:ss), 构建LocalDateTime
     * @param dateTime 时间字符串
     * @param format 指定时间格式(例：yyyy-MM-dd HH:mm:ss)
     */
    public static LocalDateTime getDateTime(String dateTime, String format) {
        if(StringUtil.isNullOrEmpty(dateTime)) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        if(StringUtil.isNullOrEmpty(format)) {
            format = ConstantFactory.STR_DATE_FULL;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        if(ConstantFactory.STR_DATE.equals(format)) {
            LocalDate localDate = getDate(dateTime);
            return localDate.atStartOfDay();
        }
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
    }
    /**
     * 通过时间字符串和固定时间格式(yyyy-MM-dd HH:mm:ss), 构建LocalDateTime
     * @param dateTime 时间字符串
     */
    public static LocalDateTime getDateTime(String dateTime) {
        return LocalDateTimeUtil.getDateTime(dateTime, ConstantFactory.STR_DATE_FULL);
    }
    /**
     * 将LocalDateTime格式化成时间字符串
     * @param dateTime LocalDateTime对象
     * @param format 指定时间格式(例：yyyy-MM-dd HH:mm:ss)
     */
    public static String toFormatString(LocalDateTime dateTime, String format) {
        if(null==dateTime) {
            return ConstantFactory.STR_DATE_DEFAULT;
        }
        if(StringUtil.isNullOrEmpty(format)) {
            format = ConstantFactory.STR_DATE_FULL;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(dateTime);
    }
    /**
     * 将LocalDateTime按“yyyy-MM-dd HH:mm:ss.SSS”格式化成时间字符串
     * @param dateTime LocalDateTime对象
     */
    public static String toFormatFullString(LocalDateTime dateTime) {
        if(null==dateTime) {
            return ConstantFactory.STR_DATE_DEFAULT_FULL;
        }
        return toFormatString(dateTime, ConstantFactory.STR_DATE_FULLMS);
    }
    /**
     * 将LocalDateTime按“yyyy-MM-dd HH:mm:ss”格式化成时间字符串
     * @param dateTime LocalDateTime对象
     */
    public static String toFormatString(LocalDateTime dateTime) {
        if(null==dateTime) {
            return ConstantFactory.STR_DATE_DEFAULT;
        }
        return toFormatString(dateTime, ConstantFactory.STR_DATE_FULL);
    }
    /**
     * 将LocalDateTime按“yyyy-MM-dd”格式化成时间字符串
     * @param dateTime LocalDateTime对象
     */
    public static String toDateString(LocalDateTime dateTime) {
        if(null==dateTime) {
            return ConstantFactory.STR_DATE_DEFAULT_DATE;
        }
        return toFormatString(dateTime, ConstantFactory.STR_DATE);
    }
    /**
     * 将dateTime格式化成时间字符串
     * @param date LocalDate对象
     * @param format 指定时间格式(例：yyyy-MM-dd)
     */
    public static String toFormatString(LocalDate date, String format) {
        if(null==date) {
            return ConstantFactory.STR_DATE_DEFAULT_DATE;
        }
        if(StringUtil.isNullOrEmpty(format)) {
            format = ConstantFactory.STR_DATE;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(date);
    }
    /**
     * 将LocalDate按“yyyy-MM-dd”格式化成时间字符串
     * @param date LocalDate对象
     */
    public static String toFormatString(LocalDate date) {
        return toFormatString(date, ConstantFactory.STR_DATE);
    }
    /**
     * LocalDateTime偏移指定月数
     * @param dateTime LocalDateTime对象
     * @param addMonth 偏移月数(可以为负数)
     */
    public static LocalDateTime dateTimeAddMonth(LocalDateTime dateTime, Long addMonth) {
        if(null==dateTime) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return dateTime.plusMonths(addMonth);
    }
    /**
     * LocalDateTime偏移指定天数
     * @param dateTime LocalDateTime对象
     * @param addDay 偏移天数(可以为负数)
     */
    public static LocalDateTime dateTimeAddDay(LocalDateTime dateTime, Long addDay) {
        if(null==dateTime) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return dateTime.plusDays(addDay);
    }
    /**
     * LocalDateTime偏移指定小时数
     * @param dateTime LocalDateTime对象
     * @param addHour 偏移小时数(可以为负数)
     */
    public static LocalDateTime dateTimeAddHour(LocalDateTime dateTime, Long addHour) {
        if(null==dateTime) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return dateTime.plusHours(addHour);
    }
    /**
     * LocalDateTime偏移指定分钟数
     * @param dateTime LocalDateTime对象
     * @param addMin 偏移分钟数(可以为负数)
     */
    public static LocalDateTime dateTimeAddMin(LocalDateTime dateTime, Long addMin) {
        if(null==dateTime) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return dateTime.plusMinutes(addMin);
    }
    /**
     * LocalDateTime偏移指定秒数
     * @param dateTime LocalDateTime对象
     * @param addSec 偏移秒数(可以为负数)
     */
    public static LocalDateTime dateTimeAddSec(LocalDateTime dateTime, Long addSec) {
        if(null==dateTime) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return dateTime.plusSeconds(addSec);
    }
    /**
     * LocalDateTime指定时间部分，偏移指定值
     * @param dateTime LocalDateTime对象
     * @param addPart 偏移时间部分
     * @param value 偏移值
     */
    public static LocalDateTime dateTimeAdd(LocalDateTime dateTime, ChronoUnit addPart, Long value) {
        if(null==dateTime) {
            return LocalDateTimeUtil.DEFAULT_DATETIME;
        }
        return dateTime.plus(value, addPart);
    }
    /**
     * 获取当前月份第一天LocalDateTime“00:00:00”
     */
    public static LocalDateTime monthFirstDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0);
    }
    /**
     * 获取当前月份最后一天LocalDateTime“23:59:59”
     */
    public static LocalDateTime monthLatestDateTime() {
        //当前月第一天“00:00:00”
        LocalDateTime monthFirst = monthFirstDateTime();
        //下个月第一天“00:00:00”
        LocalDateTime nextMonthFirst = dateTimeAddMonth(monthFirst, 1L);
        //当前月最后一天“23:59:59”
        return LocalDateTimeUtil.dateTimeAddSec(nextMonthFirst, -1L);
    }
    /**
     * 获取今天LocalDateTime“00:00:00”
     */
    public static LocalDateTime todayFirst() {
        LocalDateTime now = LocalDateTimeUtil.nowDateTime();
        return LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0);
    }
    /**
     * 获取今天LocalDateTime“23:59:59”
     */
    public static LocalDateTime todayLatest() {
        //今天“00:00:00”
        LocalDateTime now = LocalDateTimeUtil.nowDateTime();
        //明天“00:00:00”
        LocalDateTime nextDayFirst = LocalDateTimeUtil.dateTimeAddDay(now, 1L);
        //今天“23:59:59”
        return LocalDateTimeUtil.dateTimeAddSec(nextDayFirst, -1L);
    }
    //************************************** 【LocalDateTime】 **************************************





    //************************************** 【LocalDate】 **************************************
    /**
     * 获取当前LocalDate
     */
    public static LocalDate nowDate() {
        return LocalDate.now();
    }
    /**
     * 获取当前日期字符串(yyyy-MM-dd)
     */
    public static String todayStr() {
        return toFormatString(LocalDateTime.now(), ConstantFactory.STR_DATE);
    }
    /**
     * 根据日期字符串获取LocalDate(必须是yyyy-MM-dd格式)
     * @param date 日期字符串
     */
    public static LocalDate getDate(String date) {
        if(StringUtil.isNullOrEmpty(date)) {
            return LocalDateTimeUtil.DEFAULT_DATE;
        }
        return LocalDate.parse(date);
    }
    /**
     * 获取LocalDate
     * @param year 年份
     * @param month 月份
     * @param day 天
     */
    public static LocalDate getDate(Integer year, Integer month, Integer day) {
        return LocalDate.of(year, month, day);
    }
    /**
     * LocalDate偏移指定天数
     * @param date LocalDate对象
     * @param addDay 偏移天数(可以为负数)
     */
    public static LocalDate dateAddDay(LocalDate date, Long addDay) {
        if(null==date) {
            return LocalDateTimeUtil.DEFAULT_DATE;
        }
        return date.plusDays(addDay);
    }
    /**
     * LocalDate偏移指定月数
     * @param date LocalDate对象
     * @param addMonth 偏移月数(可以为负数)
     */
    public static LocalDate dateAddMonth(LocalDate date, Long addMonth) {
        if(null==date) {
            return LocalDateTimeUtil.DEFAULT_DATE;
        }
        return date.plusMonths(addMonth);
    }
    /**
     * 获取当前月份第一天LocalDate
     */
    public static LocalDate monthFirstDate() {
        LocalDate toDay = nowDate();
        return LocalDate.of(toDay.getYear(), toDay.getMonthValue(), 1);
    }
    /**
     * 获取当前月份最后一天LocalDate
     */
    public static LocalDate monthLatestDate() {
        //当前月第一天
        LocalDate monthFirst = monthFirstDate();
        //下个月第一天
        LocalDate nextMonthDay = dateAddMonth(monthFirst, 1L);
        return LocalDateTimeUtil.dateAddDay(nextMonthDay, -1L);
    }
    //************************************** 【LocalDate】 **************************************





    //************************************** 【时间戳处理】 **************************************
    /**
     * 获取当前时间戳
     */
    public static Long nowTimeStamp() {
        return Instant.now().toEpochMilli();
    }
    /**
     * 获取当前时间戳(字符串)
     */
    public static String nowTimeStampStr() {
        return String.valueOf(nowTimeStamp());
    }
    /**
     * 通过LocalDateTime对象获取时间戳
     * @param dateTime LocalDateTime对象
     */
    public static Timestamp getTimeStamp(LocalDateTime dateTime) {
        if(null==dateTime) {
            return Timestamp.valueOf(LocalDateTimeUtil.DEFAULT_DATETIME);
        }
        return Timestamp.valueOf(dateTime);
    }
    /**
     * 通过时间字符串按指定格式获取时间戳
     * @param dateTime 时间字符串
     * @param format 时间格式
     */
    public static Timestamp getTimeStamp(String dateTime, String format) {
        LocalDateTime localDateTime = LocalDateTimeUtil.getDateTime(dateTime, format);
        return Timestamp.valueOf(localDateTime);
    }
    /**
     * 通过时间字符串按“yyyy-MM-dd HH:mm:ss”格式获取时间戳
     * @param dateTime 时间字符串
     */
    public static Timestamp getTimeStamp(String dateTime) {
        return LocalDateTimeUtil.getTimeStamp(dateTime, ConstantFactory.STR_DATE_FULL);
    }
    /**
     * 通过Unix时间戳获取Timestamp
     * @param timeStamp Unix时间戳
     */
    public static Timestamp getTimeStamp(Long timeStamp) {
        return new Timestamp(timeStamp);
    }
    /**
     * 通过时间戳，按指定格式，转成时间字符串
     * @param timeStamp 时间戳值
     * @param format 时间格式
     */
    public static String toFormatString(Long timeStamp, String format) {
        LocalDateTime dateTime = LocalDateTimeUtil.getDateTime(timeStamp);
        return LocalDateTimeUtil.toFormatString(dateTime, format);
    }
    /**
     * 通过时间戳，按“yyyy-MM-dd HH:mm:ss”转成时间字符串
     * @param timeStamp 时间戳值
     */
    public static String toFormatString(Long timeStamp) {
        return toFormatString(timeStamp, ConstantFactory.STR_DATE_FULL);
    }
    /**
     * 通过时间戳，按“yyyy-MM-dd”转成时间字符串
     * @param timeStamp 时间戳值
     */
    public static String toDateString(Long timeStamp) {
        return toFormatString(timeStamp, ConstantFactory.STR_DATE);
    }
    /**
     * 通过时间戳，按“yyyy-MM-dd HH:mm:ss.SSS”转成时间字符串
     * @param timeStamp 时间戳值
     */
    public static String toFormatFullString(Long timeStamp) {
        return toFormatString(timeStamp, ConstantFactory.STR_DATE_FULLMS);
    }
    /**
     * 通过时间戳，按指定格式，转成时间字符串
     * @param timeStamp 时间戳值
     * @param format 时间格式
     */
    public static String toFormatString(Timestamp timeStamp, String format) {
        LocalDateTime dateTime = LocalDateTimeUtil.getDateTime(timeStamp);
        return LocalDateTimeUtil.toFormatString(dateTime, format);
    }
    /**
     * 通过时间戳，按“yyyy-MM-dd HH:mm:ss”转成时间字符串
     * @param timeStamp 时间戳值
     */
    public static String toFormatString(Timestamp timeStamp) {
        return toFormatString(timeStamp, ConstantFactory.STR_DATE_FULL);
    }
    /**
     * 通过时间戳，按“yyyy-MM-dd”转成时间字符串
     * @param timeStamp 时间戳值
     */
    public static String toDateString(Timestamp timeStamp) {
        return toFormatString(timeStamp, ConstantFactory.STR_DATE);
    }
    /**
     * 通过时间戳，按“yyyy-MM-dd HH:mm:ss.SSS”转成时间字符串
     * @param timeStamp 时间戳值
     */
    public static String toFormatFullString(Timestamp timeStamp) {
        return toFormatString(timeStamp, ConstantFactory.STR_DATE_FULLMS);
    }
    //************************************** 【时间戳处理】 **************************************


    public static String convertToDateFormat(String inputString) {
        try {
            // 将输入字符串解析为日期对象
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyMMddHHmmss");
            Date date = inputFormat.parse(inputString);

            // 将日期对象格式化为目标日期字符串
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    public static String nowTimeBCD(String timeFormat) {
        return LocalDateTimeUtil.time2BCD(LocalDateTimeUtil.nowDateTime(), timeFormat);
    }
    public static String nowTimeBCD() {
        return LocalDateTimeUtil.time2BCD(LocalDateTimeUtil.nowDateTime());
    }

    public static String time2BCD(LocalDateTime nowDateTime) {
        String timeStr = toFormatString(nowDateTime, ConstantFactory.STR_DATE_FULL);
        return LocalDateTimeUtil.number2BCD(timeStr);
    }

    public static String time2BCD(LocalDateTime nowDateTime, String timeFormat) {
        String timeStr = LocalDateTimeUtil.toFormatString(nowDateTime, timeFormat);
        return LocalDateTimeUtil.number2BCD(timeStr);
    }

    public static String number2BCD(String numberStr) {
        // 因为可能修改字符串的内容，所以构造StringBuffer
        StringBuffer sb = new StringBuffer(numberStr);
        // 一个字节包含两个4位的BCD码，byte数组中要包含偶数个BCD码
        // 一个十进制字符对应4位BCD码，所以如果十进制字符串的长度是奇数，要在前面补一个0使长度成为偶数
        if ((sb.length() % 2) != 0) {
            sb.insert(0, '0');
        }

        // 两个十进制数字转换为BCD码后占用一个字节，所以存放BCD码的字节数等于十进制字符串长度的一半
        byte[] bcd = new byte[sb.length() / 2];
        for (int i = 0; i < sb.length();) {
            if (!Character.isDigit(sb.charAt(i)) || !Character.isDigit(sb.charAt(i + 1))) {
                throw new RuntimeException("传入的十进制字符串包含非数字字符!");
            }
            // 每个字节的构成：用两位十进制数字运算的和填充，高位十进制数字左移4位+低位十进制数字
            bcd[i/2] = (byte)((Character.digit(sb.charAt(i), 10) << 4) + Character.digit(sb.charAt(i + 1), 10));
            // 字符串的每两个字符取出来一起处理，所以此处i的自增长要加2，而不是加1
            i += 2;
        }
        StringBuffer sbResult = new StringBuffer();
        for (int i = 0; i < bcd.length; i++) {
            sbResult.append(Integer.toBinaryString(bcd[i]));
        }
        return sbResult.toString();
    }

    public static String calculateExpiresDate(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hour); // 添加一小时
        Date expiresDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return dateFormat.format(expiresDate);
    }
}