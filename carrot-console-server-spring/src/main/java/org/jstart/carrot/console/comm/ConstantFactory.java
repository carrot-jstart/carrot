package org.jstart.carrot.console.comm;

public final class ConstantFactory {

    private ConstantFactory() {
        throw new IllegalStateException("Utility class");
    }



    //********************************** 字符串 **********************************
    /**
     * 获取字符串换行符
     */
    public static final String STR_NEWLINE = "\r\n";
    /**
     * 获取html换行符
     */
    public static final String STR_HTMLBR = "<br />";
    /**
     * 1个空格字符串
     */
    public static final String STR_SPACE = " ";
    /**
     * 正则中的1个空格字符串
     */
    public static final String STR_REGEX_SPACE = "\\s";
    /**
     * ~
     */
    public static final String STR_TIME_RANGE = "~";

    /**
     * #
     */
    private static final String STR_HASH = "#";
    /**
     * @
     */
    private static final String STR_AT = "@";
    /**
     * #
     */
    public static final String STR_HASH_SIGN = "#";
    /**
     * _
     */
    private static final String STR_UNDERLINE = "_";

    /**
     * /
     */
    public static final String STR_SLASH = "/";

    /**
     * ;
     */
    public static final String STR_SEMICOLON = ";";

    /**
     * =
     */
    public static final String STR_EQUAL = "=";
    /**
     * 逗号“,”
     */
    public static final String STR_COMMA = ",";

    /**
     * #
     */
    public static final String STR_JIN="#";
    //********************************** 字符串 **********************************





    //********************************** 时间日期 相关 **********************************
    /**
     * 日期格式“yyyy-MM-dd”
     */
    public static final String STR_DATE = "yyyy-MM-dd";
    /**
     * 时间格式“yyyy-MM-dd HH:mm:ss”
     */
    public static final String STR_DATE_FULL = "yyyy-MM-dd HH:mm:ss";
    /**
     * 时间格式“yyyy-MM-dd HH:mm:ss.SSS”
     */
    public static final String STR_DATE_FULLMS = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * “ 00:00:00”
     */
    public static final String STR_DATE_DAYSTART = " 00:00:00";
    /**
     * “ 23:59:59”
     */
    public static final String STR_DATE_DAYEND = " 23:59:59";
    /**
     * “1970-01-01”
     */
    public static final String STR_DATE_DEFAULT_DATE = "1970-01-01";
    /**
     * “1970-01-01 00:00:00”
     */
    public static final String STR_DATE_DEFAULT = "1970-01-01 00:00:00";
    /**
     * “1970-01-01 08:00:01”
     */
    public static final String STR_DATE_DEFAULT_UTC8 = "1970-01-01 08:00:01";
    /**
     * “1970-01-01 00:00:00”
     */
    public static final String STR_DATE_DEFAULT_FULL = "1970-01-01 00:00:00.000";
    //********************************** 时间日期 相关 **********************************




    //********************************** SQL关键字 相关 **********************************
    /**
     * 等于“ = ?”
     */
    public static final String SQL_EQUAL = " = ?";
    /**
     * 不等于
     */
    public static final String SQL_NOTEQUAL = " <> ?";
    /**
     * 大于
     */
    public static final String SQL_GREATER = " > ?";
    /**
     * 大于等于
     */
    public static final String SQL_GREATERTHAN = " >= ?";
    /**
     * 小于
     */
    public static final String SQL_LESS = " < ?";
    /**
     * 小于等于
     */
    public static final String SQL_LESSTHAN = " <= ?";
    /**
     * 模糊等于“ like ?”
     */
    public static final String SQL_LIKE = " like ?";
    /**
     * 模糊不等于“ not like ?”
     */
    public static final String SQL_NOTLIKE = " not like ?";
    /**
     * “ IN”
     */
    public static final String SQL_IN = " IN";
    //public static final String SQL_IN = " FIND_IN_SET(%s, ?) > 0";//由于“FIND_IN_SET”无法使用索引 2020-06-17 改为“in”语法
    /**
     * “ NOT IN”
     */
    public static final String SQL_NOTIN = " NOT IN";
    //public static final String SQL_NOTIN = " FIND_IN_SET(%s, ?) = 0";//由于“FIND_IN_SET”无法使用索引 2020-06-17 改为“not in”语法


    /**
     * 并且“AND”
     */
    public static final String SQL_AND = " AND";
    /**
     * 或者“OR”
     */
    public static final String SQL_OR = " OR";

    /**
     * 逗号“,”
     */
    public static final String SQL_STR_COMMA = " ,";
    /**
     * “ ASC”
     */
    public static final String SQL_ASC = " ASC";
    /**
     * “ DESC”
     */
    public static final String SQL_DESC = " DESC";
    public static final String TB_A = "a.";
    public static final String TB_B = "b.";
    //********************************** SQL关键字 相关 **********************************






    //********************************** redis 相关 **********************************
    /**
     * 分布式锁前缀
     */
    public static final String REDIS_LOCKPREFIX = "lock_";
    /**
     * 分布式锁超时时间10秒
     */
    public static final Long REDIS_LOCKTIMEOUT = 10 * 1000L;
    /**
     * token哈希key
     */
    public static final String REDIS_HASH_TOKEN = "HASH_TOKEN";
    /**
     * 分布式锁超时时间24小时
     */
    public static final Long REDIS_EXPIRE_DEFAULT = 24 * 60 * 60L;
    //********************************** redis 相关 **********************************






    //********************************** request请求 相关 **********************************

    /**
     * token的secret
     */
    public static final String REQUEST_SECRET = "djyx";
    /**
     * token存在于headers的键值
     */
    public static final String REQUEST_TOKENKEY = "Token";

    /**
     * userId
     */
    public static final String USERID= "UserId";
    /**
     * 基础角色
     */
    public static final String BASE_ROLENAME= "BaseRole";

    /**
     * userName
     */
    public static final String USERNAME= "UserName";

    /**
     * 数据权限
     */
    public static final String DATA_PERMISSION= "DataPermission";

    /**
     * 路由权限
     */
    public static final String ROUTER_PERMISSION= "Router";

    /**
     * 用户头像
     */
    public static final String USER_AVATAR= "Avatar";

    /**
     * 角色
     */
    public static final String ROLE_PERMISSION= "ItemRole";

    /**
     * token过期时间
     */
    public static final Long TOKEN_EXPIRE_DEFAULT = 2 * 60 * 60L;
    /**
     * 刷新token过期时间
     */
    public static final Long LONG_TOKEN_EXPIRE_DEFAULT = 7 * 24 * 60 * 60L;
    //********************************** request请求 相关 **********************************


    //********************************** MQTT相关相关 **********************************
    public static final String MQTT_QUEUE="$queue/";
    public static final String MQTT_TOPIC="$share/";
    //********************************** MQTT相关相关 **********************************




    //********************************** 正则表达式相关 **********************************
    /**
     * 字符串格式为日期“yyyy-MM-dd”的正则
     */
    public static final String REGEX_DATE = "^[0-9]{4}\\-((0[1-9])|(1[0-2]))\\-((0[1-9])|(1[0-9])|(2[0-9])|(3[0-1]))$";
    /**
     * 字符串格式为时间“yyyy-MM-dd HH:mm:ss”的正则
     */
    public static final String REGEX_DATETIME = "^[0-9]{4}\\-((0[1-9])|(1[0-2]))\\-((0[1-9])|([12][0-9])|(3[0-1]))\\s((0[0-9])|(1[0-9])|(2[0-3]))(\\:((0[0-9])|([1-5][0-9]))){2}$";
    /**
     * 字符串格式为完整时间“yyyy-MM-dd HH:mm:ss.SSS”的正则
     */
    public static final String REGEX_DATETIME_FULL = "^[0-9]{4}\\-((0[1-9])|(1[0-2]))\\-((0[1-9])|([12][0-9])|(3[0-1]))\\s((0[0-9])|(1[0-9])|(2[0-3]))(\\:((0[0-9])|([1-5][0-9]))){2}\\.([0-9]){3}$";
    /**
     * 字符串格式为时间“/Date(1585790902913+0800)/”的正则
     */
    public static final String REGEX_DATETIME_JAVA = "^/Date\\((?<timestamp>[0-9]{13})\\+([^\\)]+)\\)/$";

    //********************************** 正则表达式相关 **********************************




    public static final Integer NUM0 = 0;
    public static final Integer NUM1 = 1;
    public static final Integer NUM2 = 2;
    public static final Integer NUM3 = 3;
    public static final Integer NUM4 = 4;
    public static final Integer NUM5 = 5;
    public static final Integer NUM6 = 6;
    public static final Integer NUM7 = 7;
    public static final Integer NUM8 = 8;
    public static final Integer NUM9 = 9;
    public static final Integer NUM10 = 10;
    public static final Integer NUM15 = 15;
    public static final Integer NUM20 = 20;
    public static final Integer NUM30 = 30;
    public static final Integer NUM50 = 50;
    public static final Integer NUM100 = 100;
    public static final Integer NUM200 = 200;
    public static final Integer NUM500 = 500;
    public static final Integer NUM1000 = 1000;
    public static final Integer NUM2000 = 2000;
    public static final Integer NUM5000 = 5000;
    public static final Integer NUM10000 = 10000;
    public static final Integer LENGTH_MAX5000 = 5000;
    public static final Integer LENGTH_MAX2000 = 2000;
    public static final Integer LENGTH_MAX500 = 500;
    public static final Integer LENGTH_MAX200 = 200;
    public static final Integer LENGTH_MAX100 = 100;
    public static final Integer LENGTH_MAX25 =25;
    public static final Integer LENGTH_MAX50 = 50;
    public static final Integer MONEY_MAX_LENGTH = 2;
    public static final Integer MAX_PAGE_SIZE = 999999;
    public static final Integer PAGE_SIZE_DEFAULT = 20;
    public static final Integer DEFAULT_NUM_PRECISION = 2;
    public static final Integer FILE_UNIT = 1024;
    public static final Integer FILE_SIZE_1M = 1024;
    public static final Integer FILE_SIZE_2M = 2 * 1024;
    public static final Integer FILE_SIZE_5M = 5 * 1024;
    public static final Integer FILE_SIZE_10M = 10 * 1024;
    public static final Integer FILE_SIZE_20M = 20 * 1024;
    public static final Integer FILE_SIZE_50M = 50 * 1024;
    public static final Integer FILE_SIZE_100M = 100 * 1024;
    public static final Integer FILE_SIZE_200M = 200 * 1024;
    public static final Integer TIME_LEN_1S = 1000;
    public static final Integer TIME_LEN_10S = 10 * 1000;
    public static final Integer TIME_LEN_1MIN = 60 * 1000;
    public static final Integer TIME_LEN_5MIN = 5 * 60 * 1000;
    public static final Integer TIME_LEN_10MIN = 10 * 60 * 1000;
    public static final Integer TIME_LEN_20MIN = 20 * 60 * 1000;
    public static final Integer TIME_LEN_50MIN = 50 * 60 * 1000;
    public static final Integer TIME_LEN_100MIN = 100 * 60 * 1000;
    public static final Integer TIME_LEN_1H = 60 * 60 * 1000;
    public static final Integer TIME_LEN_2H = 2 * 60 * 60 * 1000;
    public static final Integer TIME_LEN_12H = 12 * 60 * 60 * 1000;
    public static final Integer TIME_LEN_24H = 24 * 60 * 60 * 1000;
}