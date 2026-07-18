package org.jstart.carrot.console.comm.utils;


import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jstart.carrot.console.comm.ConstantFactory;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字符串处理类
 * @author carrot
 * @date   2024-5-12
 */
public final class StringUtil {
    //region 常量
    private static final String REGX_MOBILEPHONE = "^((\\+86)|(\\(\\+86\\))-)?[0-9]{11}$";

    private static final String REGX_TELEPHONE = "^((\\+86)|(\\(\\+86\\))-)?(((0[1,2]{1}\\d{1})?-?\\d{8})|((0[3-9]{1}\\d{2})?-?\\d{7,8}))$";

    private static final String REGX_IDCARD = "^([0-9]{17}[0-9X]{1})|([0-9]{15})$";

    private static final String REGX_WEBURL = "^(((file|gopher|news|nntp|telnet|http|ftp|https|ftps|sftp)://)|(www\\.))+(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(/[a-zA-Z0-9\\&amp;%_\\./-~-]*)?$";

    private static final String REGX_EMAIL = "^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

    private static final String REGX_MONEY = "^\\d{1,12}(?:\\.\\d{1,4})?$";

    private static final String DEFAULT = "utf-8";
    //endregion

    private StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     *  从当前对象移除头尾指定字符
     * @author xiangyuanzhang
     * @date   2018-12-25
     * @param source 原始字符串
     * @param trimChar 移除字符串
     */
    public static String trim(String source, String trimChar) {
        if(source==null){
            return "";
        }
        source = source.trim();
        if(source.isEmpty()){
            return "";
        }
        String beginChar = source.substring(0, 1);
        if (beginChar.equalsIgnoreCase(trimChar)) {
            source = source.substring(1, source.length());
        }
        String endChar = source.substring(source.length() - 1, source.length());
        if (endChar.equalsIgnoreCase(trimChar)) {
            source = source.substring(0, source.length() - 1);
        }
        return source;
    }

    /**
     *  从当前对象移除头部指定字符
     * @author xiangyuanzhang
     * @date   2018-12-25
     * @param source 原始字符串
     * @param trimChar 移除字符串
     */
    public static String trimStart(String source, String trimChar) {
        if(source==null){
            return "";
        }
        source = source.trim();
        if(source.isEmpty()){
            return "";
        }
        String beginChar = source.substring(0, 1);
        if (beginChar.equalsIgnoreCase(trimChar)) {
            source = source.substring(1, source.length());
        }
        return source;
    }

    /**
     *  从当前对象移除尾部指定字符
     * @author xiangyuanzhang
     * @date   2018-12-25
     * @param source 原始字符串
     * @param trimChar 移除字符串
     */
    public static String trimEnd(String source, String trimChar) {
        if(source==null){
            return "";
        }
        source = source.trim();
        if(source.isEmpty()){
            return "";
        }
        String endChar = source.substring(source.length() - 1, source.length());
        if (endChar.equalsIgnoreCase(trimChar)) {
            source = source.substring(0, source.length() - 1);
        }
        return source;
    }

    /**
     * 字符串枚举值转成枚举对象
     * @author linliu
     * @date   2018-12-24
     * @param enumType 枚举类型
     * @param value 枚举值
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    public static <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
        if(isNullOrEmpty(value)) {
            return null;
        }

        T result = null;
        try {
            T[] values = enumType.getEnumConstants();
            Method getValue = enumType.getMethod("getValue");

            for (T e : values) {
                if(getValue.invoke(e).toString().equals(value)) {
                    result = e;
                    break;
                }
            }
        } catch (Exception e) {
            //TODO log
        }
        return result;
    }

    /**
     * 字符串枚举值转成枚举对象
     * @author linliu
     * @date   2018-12-24
     * @param enumType 枚举类型
     * @param name 枚举名称
     * @param <T> 枚举类型
     * @return 枚举对象
     */
    public static <T extends Enum<T>> T parseEnumByName(Class<T> enumType, String name) {
        if(isNullOrEmpty(name)) {
            return null;
        }

        T result = null;
        try {
            result = EnumUtils.getEnum(enumType, name);
        } catch (Exception e) {
            //TODO log
        }
        return result;
    }

    /**
     * 判断枚举是否申明了传入枚举值
     * @author linliu
     * @date   2018-12-24
     * @param enumType 枚举类型
     * @param value 枚举值
     * @param <T> 枚举类型
     * @return 是否包含
     */
    public static <T extends Enum<T>> boolean isEnum(Class<T> enumType, String value) {
        return (null != parseEnum(enumType, value));
    }

    /**
     * 判断枚举是否申明了传入枚举值
     * @author linliu
     * @date   2018-12-24
     * @param enumType 枚举类型
     * @param name 枚举名称
     * @param <T> 枚举类型
     * @return 是否包含
     */
    public static <T extends Enum<T>> boolean isEnumName(Class<T> enumType, String name) {
        return EnumUtils.isValidEnum(enumType, name);
    }

    /**
     * 字符串是否为空
     * @author linliu
     * @date   2018-12-25
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isNullOrEmpty(String str){
        return  (str==null || str.equals("") || str.replace(" ","").length() == 0);
    }

    /**
     * 字符串非空
     * @author linliu
     * @date   2018-12-25
     * @param str 字符串
     * @return 是否非空
     */
    public static boolean isNotNullOrEmpty(String str){
        return  !isNullOrEmpty(str);
    }

    /**
     * 集合，使用英文逗号“,”拼接成一个字符串
     * @param list 集合
     */
    public static String join(Collection<String> list){
        return StringUtil.join(list, ",");
    }
    /**
     * 集合，使用指定的字符，拼接成一个字符串
     * @param list 集合
     * @param separator 拼接字符
     */
    public static String join(Collection<String> list, String separator){
        if(ListUtil.isNullOrEmpty(list)){
            return "";
        }
        return StringUtils.join(list.toArray(), separator);
    }
    /**
     * 数组，使用英文逗号“,”拼接成一个字符串
     * @param array 数组
     */
    public static String join(String[] array){
        return StringUtil.join(array, ",");
    }
    /**
     * 数组，使用指定的字符，拼接成一个字符串
     * @param array 数组
     * @param separator 拼接字符
     */
    public static String join(String[] array, String separator){
        if(array == null){
            return "";
        }
        return StringUtils.join(array, separator);
    }

    /**
     * 集合，使用英文逗号“,”拼接成一个字符串
     * @param list 集合
     */
    public static String joinNumber(Collection<Number> list){
        return StringUtil.joinNumber(list, ",");
    }
    /**
     * 集合，使用指定的字符，拼接成一个字符串
     * @param list 集合
     * @param separator 拼接字符
     */
    public static String joinNumber(Collection<Number> list, String separator){
        if(ListUtil.isNullOrEmpty(list)){
            return "";
        }
        return StringUtils.join(list.toArray(), separator);
    }
    /**
     * 数组，使用英文逗号“,”拼接成一个字符串
     * @param array 数组
     */
    public static String joinNumber(Number[] array){
        return joinNumber(array, ",");
    }
    /**
     * 数组，使用指定的字符，拼接成一个字符串
     * @param array 数组
     * @param separator 拼接字符
     */
    public static String joinNumber(Number[] array, String separator){
        if(array == null){
            return "";
        }
        return StringUtils.join(array, separator);
    }

    /**
     * base64解码(utf-8编码)
     * @author linliu
     * @date   2019-06-14
     * @param str 密文字符串
     * @return 解码后的明文
     */
    public static String base64Encode(String str) {
        return base64Encode(str, DEFAULT);
    }
    /**
     * base64解码
     * @author linliu
     * @date   2019-06-14
     * @param str 密文字符串
     * @param encoding 编码格式[默认utf-8]
     * @return 解码后的明文
     */
    public static String base64Encode(String str, String encoding) {
        if(isNullOrEmpty(str)) {
            return "";
        }
        if(isNullOrEmpty(encoding)) {
            encoding = DEFAULT;
        }
        byte[] bytes = str.getBytes(Charset.forName(encoding));
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base64解码(utf-8编码)
     * @author linliu
     * @date   2019-06-14
     * @param str 密文字符串
     * @return 解码后的明文
     */
    public static String base64Decode(String str) {
        return base64Decode(str, DEFAULT);
    }
    /**
     * base64解码
     * @author linliu
     * @date   2019-06-14
     * @param str 密文字符串
     * @param encoding 编码格式[默认utf-8]
     * @return 解码后的明文
     */
    public static String base64Decode(String str, String encoding) {
        if(isNullOrEmpty(str)) {
            return "";
        }
        if(isNullOrEmpty(encoding)) {
            encoding = DEFAULT;
        }
        byte[] bytes = Base64.getDecoder().decode(str);
        return new String(bytes, Charset.forName(encoding));
    }

    /**
     * 传入字符串是否是有效的手机号码
     * @param str 字符串
     */
    public static boolean isMobilePhone(String str) {
        return Pattern.matches(REGX_MOBILEPHONE, str);
    }

    /**
     * 传入字符串是否是有效的座机号码
     * @param str 字符串
     */
    public static boolean isTelPhone(String str) {
        return Pattern.matches(REGX_TELEPHONE, str);
    }

    /**
     * 传入字符串是否是有效的身份证号码
     * @param str 字符串
     */
    public static boolean isIdCard(String str) {
        return Pattern.matches(REGX_IDCARD, str);
    }

    /**
     * 传入字符串是否是有效的网址
     * @param str 字符串
     */
    public static boolean isWebUrl(String str) {
        return Pattern.matches(REGX_WEBURL, str);
    }

    /**
     * 传入字符串是否是有效的电子邮件邮箱地址
     * @param str 字符串
     */
    public static boolean isEmail(String str) {
        return Pattern.matches(REGX_EMAIL, str);
    }

    /**
     * 传入字符串是否是有效的金额
     * @param str 字符串
     */
    public static boolean isMoney(String str) {
        return Pattern.matches(REGX_MONEY, str);
    }

    /**
     * 是否满足正则表达式规则
     * @author linliu
     * @date   2019-01-04
     * @param str 字符串
     * @param regex 正则表达式
     * @return 是否匹配
     */
    public static Boolean isMatch(String str,String regex) {
        if(StringUtil.isNullOrEmpty(str)) {
            return Boolean.FALSE;
        }
        return Pattern.matches(regex, str);
    }

    /**
     * 正则获取Xml中的内容
     * @author xiangyuanzhang
     * @date   2019-01-09
     * @param xml 传入的xml字符串
     * @param label 指定的标签中的内容
     */
    public static String regexXml(String xml, String label) {
        String context = "";
        String rgex = "<" + label + ">(.*?)</" + label + ">";
        Pattern pattern = Pattern.compile(rgex,Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(xml);
        List<String> list = new ArrayList<>();
        int i = 1;
        while (m.find()) {
            list.add(m.group(i));
            i++;
        }
        if (!list.isEmpty()) {
            context = list.get(0);
        }
        return context;
    }

    /**
     * 获取<![CDATA 中内容
     *  @author xiangyuanzhang
     *  @date   2019-01-14
     * @param str
     * @return
     */
    public static String getCDATAContent(String str) {
        Pattern p = Pattern.compile(".*<!\\[CDATA\\[(.*)\\]\\]>.*");
        Matcher m = p.matcher(str);
        if(m.matches()) {
           return m.group(1);
        }
        return str;
    }

    /**
     * 将传入字符串转成 Integer 类型
     * @param str 字符串
     */
    public static int parseInt(String str) {
        return Integer.parseInt(str);
    }

    /**
     * 将传入字符串转成 Long 类型
     * @param str 字符串
     */
    public static long parseLong(String str) {
        return Long.parseLong(str);
    }

    /**
     * 将传入字符串转成精度为2位小数的 BigDecimal 类型
     * @param str 字符串
     */
    public static BigDecimal parseBigDecimal(String str) {
        return parseBigDecimal(str, Integer.parseInt("2"));
    }

    /**
     * 将传入字符串转成指定精度的 BigDecimal 类型
     * @param str 字符串
     */
    public static BigDecimal parseBigDecimal(String str, Integer length) {
        BigDecimal bd = new BigDecimal(str);
        return bd.setScale(length, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 传入字符串是否是有效的 BigDecimal
     * @param str 字符串
     */
    public static boolean isBigDecimal(String str) {
        try{
            if(isNullOrEmpty(str)) {
                return false;
            }
            new BigDecimal(str);
            return true;
        }catch (NumberFormatException e){
           return false;
        }
    }

    /**
     * 将传入指定格式的字符串转成 Calendar 类型（默认“yyyy-MM-dd HH:mm:ss”）
     * @param str 时间字符串
     * @param format 时间格式
     */
    public static Calendar parseDate(String str, String... format) {
        String f = ConstantFactory.STR_DATE_FULL;
        if(format.length>0) {
            f = format[0];
        }
        return CalendarUtil.fromStr(str, f);
    }


    /**
     * 集合，使用英文逗号“,”拼接成一个字符串
     * @param list 集合
     */
    public static String joinInt(List<Integer> list){
        return joinInt(list, ",");
    }
    /**
     * 集合，使用指定的字符，拼接成一个字符串
     * @param list 集合
     * @param separator 拼接字符
     */
    public static String joinInt(List<Integer> list, String separator){
        if(ListUtil.isNullOrEmpty(list)){
            return "";
        }
        return StringUtils.join(list.toArray(), separator);
    }

    /**
     * 集合，使用指定的字符，拼接成一个字符串
     * @param list 集合
     * @param separator 拼接字符
     */
    public static Object joinDouble(List<Double> list, String separator) {
        if(ListUtil.isNullOrEmpty(list)){
            return "";
        }
        return StringUtils.join(list.toArray(), separator);
    }

    /**
     * 数组，使用英文逗号“,”拼接成一个字符串
     * @param array 数组
     */
    public static String joinInt(Integer[] array){
        return joinInt(array, ",");
    }
    /**
     * 数组，使用指定的字符，拼接成一个字符串
     * @param array 数组
     * @param separator 拼接字符
     */
    public static String joinInt(Integer[] array, String separator){
        if(array == null){
            return "";
        }
        return StringUtils.join(array, separator);
    }

    /**
     * 将传入字符串按指定分隔符，拆分成数组
     * @param str 字符串
     * @param splitChar 分隔符
     */
    public static String[] split(String str, String splitChar) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new String[0];
        }
        return str.split(splitChar);
    }
    /**
     * 将传入字符串按英文逗号“,”分隔，拆分成数组
     * @param str 字符串
     */
    public static String[] split(String str) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new String[0];
        }
        return str.split("\\,");
    }

    /**
     * 将传入字符串按指定分隔符，拆分成集合
     * @param str 字符串
     * @param splitChar 分隔符
     */
    public static List<String> splitList(String str, String splitChar) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        return ListUtil.toList(split(str, splitChar));
    }
    /**
     * 将传入字符串按英文逗号“,”分隔，拆分成集合
     * @param str 字符串
     */
    public static List<String> splitList(String str) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        return splitList(str, ",");
    }

    /**
     * 将传入字符串按指定分隔符，拆分成集合
     * @param str 字符串
     * @param splitChar 分隔符
     */
    public static List<Integer> splitIntList(String str, String splitChar) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        return ListUtil.toIntList(splitList(str, splitChar));
    }
    /**
     * 将传入字符串按英文逗号“,”分隔，拆分成集合
     * @param str 字符串
     */
    public static List<Integer> splitIntList(String str) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        return splitIntList(str, ",");
    }

    /**
     * 将传入字符串按指定分隔符，拆分成集合
     * @param str 字符串
     * @param splitChar 分隔符
     */
    public static List<Long> splitLongList(String str, String splitChar) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        return ListUtil.toLongList(splitList(str, splitChar));
    }
    /**
     * 将传入字符串按英文逗号“,”分隔，拆分成集合
     * @param str 字符串
     */
    public static List<Long> splitLongList(String str) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        return splitLongList(str, ",");
    }

    /**
     * 集合，使用英文逗号“,”拼接成一个字符串
     * @param list 集合
     */
    public static String joinLong(Collection<Long> list) {
        return StringUtil.joinLong(list, ",");
    }
    /**
     * 集合，使用指定的字符，拼接成一个字符串
     * @param list 集合
     * @param separator 拼接字符
     */
    public static String joinLong(Collection<Long> list, String separator) {
        if(ListUtil.isNullOrEmpty(list)){
            return "";
        }
        return StringUtils.join(list.toArray(), separator);
    }

    /**
     * 数组，使用英文逗号“,”拼接成一个字符串
     * @param array 数组
     */
    public static String joinLong(Long[] array) {
        return joinLong(array, ",");
    }
    /**
     * 数组，使用指定的字符，拼接成一个字符串
     * @param array 数组
     * @param separator 拼接字符
     */
    public static String joinLong(Long[] array, String separator){
        if(array == null){
            return "";
        }
        return StringUtils.join(array, separator);
    }

    /**
     * 将字符串按英文逗号“,”分割，拆分成日期集合
     * @param str
     * @param dateFormat
     * @return
     */
    public static List<Date> splitListDate(String str, String dateFormat) {
        return splitListDate(str, dateFormat, ",");
    }
    /**
     * 将字符串按指定分隔符，拆分成日期集合
     * @param str 字符串
     * @param dateFormat 日期格式
     * @return
     */
    public static List<Date> splitListDate(String str, String dateFormat, String separator) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        List<String> lstDate = splitList(str, separator);
        return lstDate.stream().map(d -> {
            try {
                return CalendarUtil.fromStr(d, dateFormat).getTime();
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    /**
     * 按指定字符串分割字符串
     * @author linliu
     * @date   2019-06-19
     * @param str 字符串
     * @param onceQuantity 拆分后单个字符串的字符数
     */
    public static String[] split(String str, Integer onceQuantity) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new String[]{};
        }
        if(Integer.valueOf(0).compareTo(onceQuantity)>-1) {
            onceQuantity = str.length();
        }
        int size = str.length() / onceQuantity;
        String[] result = new String[size];
        if(size==1) {
            result[0] = str;
            return result;
        }
        for (int idx=0; idx<size; idx++) {
            result[idx] = substring(str, onceQuantity*idx, onceQuantity*(idx+1));
        }
        return result;
    }

    /**
     * 按指定字符串分割字符串
     * @author linliu
     * @date   2019-06-19
     * @param str 字符串
     * @param onceQuantity 拆分后单个字符串的字符数
     */
    public static List<String> splitList(String str, Integer onceQuantity) {
        if(StringUtil.isNullOrEmpty(str)) {
            return new ArrayList<>();
        }
        if(Integer.valueOf(0).compareTo(onceQuantity)>-1) {
            onceQuantity = str.length();
        }
        int size = str.length() / onceQuantity;
        List<String> result = new ArrayList<>();
        if(size==1) {
            result.add(str);
            return result;
        }
        if(str.length()%onceQuantity != 0) {
            size += 1;
        }
        for (int idx=0; idx<size; idx++) {
            result.add(substring(str, onceQuantity*idx, onceQuantity*(idx+1)));
        }
        return result;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     * @author linliu
     * @date   2019-06-19
     * @param str 原始字符串
     * @param start 开始位置
     * @param end 结束位置
     */
    public static String substring(String str, Integer start, Integer end) {
        if (StringUtil.isNullOrEmpty(str) || start > str.length()) {
            return "";
        }
        if (end > str.length()) {
            return str.substring(start, str.length());
        } else {
            return str.substring(start, end);
        }
    }

    /**
     * 截取字符串长度
     * @param content 字符串
     * @param length 截取长度
     */
    public static String cutStr(String content, Integer length) {
       return StringUtil.substring(content, 0, length);
    }

    /**
     * 判断是否为空，如果为空返回空字符串
     * @param str 字符串
     */
    public static String getStringDefault(String str) {
        if (StringUtil.isNullOrEmpty(str)) {
            return "";
        }
        return str;
    }

    /**
     * 将小驼峰字符串转为_连接的
     * @param input 字符串
     */
    public static String camelCaseToUnderscore(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        boolean prevIsLowerCase = false;

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (prevIsLowerCase) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
                prevIsLowerCase = false;
            } else {
                result.append(c);
                prevIsLowerCase = true;
            }
        }

        return result.toString();
    }

    /**
     * 字符串转16进制字符
     * @param input 字符串
     */
    public static String toHexString(String input) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int ch = input.charAt(i);
            String s4 = Integer.toHexString(ch);
            str.append(s4);
        }
        return str.toString();
    }

    public static String getStringBefore(String str, String key) {
        if (StringUtil.isNullOrEmpty(str)){
            return "";
        }
        int index = str.indexOf(key);
        if (index != -1) {
            return str.substring(0, index);
        }
        return "";
    }
    public static String getStringAfter(String str, String key) {
        if (StringUtil.isNullOrEmpty(str)){
            return "";
        }
        int index = str.indexOf(key);
        if (index != -1) {
            return str.substring(index + key.length());
        }
        return "";
    }

    /**
     * 截取字符串
     * @param source
     * @param begin
     * @param end
     * @return
     */
    public static String getSubString(String source, String begin, String end) {
        if(source==null || source.isEmpty()){
            return "";
        }
        int beginIndex = source.indexOf(begin);
        if (beginIndex < 0) {
            return "";
        }
        int endIndex = source.indexOf(end, beginIndex + begin.length());
        if (endIndex < 0) {
            return "";
        }
        return source.substring(beginIndex + begin.length(), endIndex);
    }
}