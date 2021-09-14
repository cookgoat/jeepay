package com.jeequan.jeepay.core.utils;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class DateCalcUtil {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
    private static Calendar startDate = Calendar.getInstance();
    private static Calendar endDate = Calendar.getInstance();
    private static DateFormat df = DateFormat.getDateInstance();
    private static Date earlydate = new Date();
    private static Date latedate = new Date();

    /**
     * 计算两个时间相差多少个年
     *
     * @param early
     * @param late
     * @return
     * @throws ParseException
     */
    public static int yearsBetween(String start, String end) throws ParseException {
        startDate.setTime(sdf.parse(start));
        endDate.setTime(sdf.parse(end));
        return (endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR));
    }

    /**
     * 计算两个时间相差多少个月
     *
     * @param date1
     *            <String>
     * @param date2
     *            <String>
     * @return int
     * @throws ParseException
     */
    public static int monthsBetween(String start, String end) throws ParseException {
        int result=0;
        Calendar cal1 = new GregorianCalendar();
        cal1.setTime(sdf1.parse(start));
        Calendar cal2 = new GregorianCalendar();
        cal2.setTime(sdf1.parse(end));
        result =(cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR)) * 12 + cal1.get(Calendar.MONTH)- cal2.get(Calendar.MONTH);
        return result==0?1 : Math.abs(result);
    }


    /**
     * 计算两个时间相差多少个天
     *
     * @param early
     * @param late
     * @return
     * @throws ParseException
     */
    public static int weeksBetween(String start, String end) throws ParseException {
        // 得到两个日期相差多少天
        return daysBetween(start, end) /7;
    }


    /**
     * 计算两个时间相差多少个天
     *
     * @param early
     * @param late
     * @return
     * @throws ParseException
     */
    public static int daysBetween(String start, String end) throws ParseException {
        // 得到两个日期相差多少天
        return hoursBetween(start, end) / 24;
    }

    /**
     * 计算两个时间相差多少小时
     *
     * @param early
     * @param late
     * @return
     * @throws ParseException
     */
    public static int hoursBetween(String start, String end) throws ParseException {
        // 得到两个日期相差多少小时
        return minutesBetween(start, end) / 60;
    }

    /**
     * 计算两个时间相差多少分
     *
     * @param early
     * @param late
     * @return
     * @throws ParseException
     */
    public static int minutesBetween(String start, String end) throws ParseException {
        // 得到两个日期相差多少分
        return secondesBetween(start, end) / 60;
    }

    /**
     * 计算两个时间相差多少秒
     *
     * @param early
     * @param late
     * @return
     * @throws ParseException
     */
    public static int secondesBetween(String start, String end) throws ParseException {
        earlydate = df.parse(start);
        latedate = df.parse(end);
        startDate.setTime(earlydate);
        endDate.setTime(latedate);
        // 设置时间为0时
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        // 得到两个日期相差多少秒
        return ((int) (endDate.getTime().getTime() / 1000) - (int) (startDate.getTime().getTime() / 1000));
    }


    /**
     * 获取两个时间的相差天数
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return Long
     */
    public static Long getBetweenDays(Temporal begin, Temporal end) {
        return Math.abs(ChronoUnit.DAYS.between(begin, end));
    }
    /**
     * 计算两日期相差的周数 （日历逻辑）
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return Integer
     */
    public static Long getBetweenWeeks(Temporal begin, Temporal end) {
        if (end.getLong(ChronoField.EPOCH_DAY) - begin.getLong(ChronoField.EPOCH_DAY) < 0) {
            Temporal temp = begin;
            begin = end;
            end = temp;
        }
        int beginWeekDay = begin.get(ChronoField.DAY_OF_WEEK);
        long daysBetween = getBetweenDays(begin, end);
        long weeksBetween = daysBetween / 7;
        int offset = (daysBetween % 7 + beginWeekDay) > 7 ? 1 : 0;
        return offset + weeksBetween;
    }


}
