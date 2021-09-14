package com.jeequan.jeepay.core.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 常用日期函数工具类
 * @author 罗季晖
 * @email  bigtiger02@gmail.com
 * @date   2013-8-21
 */
public class DateUtil1 {

    public final static String REG_YYMMDD = "yyyy-MM-dd";
    public final static String REG_YYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public final static String REG_YYMMDDMM = "yyyy-MM-dd HH:mm";
    /**
     * 日期格式化
     * @param reg   日期格式化参数
     * @param date  日期
     * @return
     */
    public static String dateFormat(String reg,Date date){
        DateFormat sdf = getDateFormat(reg);
        return sdf.format(date);
    }
    /**
     * 默认日期格式化  格式为 yyyy-MM-dd
     * @param date
     * @return
     */
    public static String dateFormat(Date date){
        if(date == null){
            return "";
        }
        return dateFormat(REG_YYMMDD,date);
    }


    public static String dateFormatmm(Date date){
        if(date == null){
            return "";
        }
        return dateFormat(REG_YYMMDDMM,date);
    }

    public static Date firstDayOfMonth(int year, int month)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.DAY_OF_MONTH,cal.getMinimum(Calendar.DATE));
        return cal.getTime();
    }

    public static Date lastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.DAY_OF_MONTH,cal.getMinimum(Calendar.DATE));
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }


    public  static List<Date> getDates(int year,int month){
        List<Date> dates = new ArrayList<Date>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH,  month-1);
        cal.set(Calendar.DATE, 1);


        while(cal.get(Calendar.YEAR) == year &&
                cal.get(Calendar.MONTH) < month){
            //int day = cal.get(Calendar.DAY_OF_WEEK);


            dates.add((Date)cal.getTime().clone());

            cal.add(Calendar.DATE, 1);
        }
        return dates;

    }

    public static String dateFormatForTime(Date date){
        return dateFormat(REG_YYMMDD_HHMMSS, date);
    }
    /**
     * 以友好的方式显示时间
     * @param time
     * @return
     */
    public static String friendlyTime(Date time) {
        if(time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = getDateFormat();
        //判断是否是同一天
        String curDate = dateFormat.format(cal.getTime());
        String paramDate = dateFormat.format(time);
        if(curDate.equals(paramDate)){
            int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
            if(hour == 0){
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"分钟前";
            }else{
                ftime = hour+"小时前";
            }
            return ftime;
        }

        long lt = time.getTime()/86400000;
        long ct = cal.getTimeInMillis()/86400000;
        int days = (int)(ct - lt);
        if(days == 0){
            int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
            if(hour == 0){
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"分钟前";
            }else{
                ftime = hour+"小时前";
            }
        }
        else if(days == 1){
            ftime = "昨天";
        }
        else if(days == 2){
            ftime = "前天";
        }
        else if(days > 2 && days <= 10){
            ftime = days+"天前";
        }
        else if(days > 10){
            ftime = dateFormat.format(time);
        }
        return ftime;
    }

    /**
     * 判断给定字符串时间是否为今日
     * @param time
     * @return boolean
     */
    public static boolean isToday(Date time){
        boolean b = false;
        Date today = new Date();
        DateFormat dateFormat = getDateFormat();
        if(time != null){
            String nowDate = dateFormat.format(today);
            String timeDate = dateFormat.format(time);
            if(nowDate.equals(timeDate)){
                b = true;
            }
        }
        return b;
    }

    /**
     * 获取日期格式化对象
     * @return
     */
    public static DateFormat getDateFormat(){
        return getDateFormat(REG_YYMMDD);
    }
    /**
     * 获取日期格式化对象
     * @param reg
     * @return
     */
    public static DateFormat getDateFormat(String reg){
        return new SimpleDateFormat(reg);
    }

    /**
     * 获取毫秒
     * @param reg
     * @param date
     * @return
     */
    public static long getMilliseconds(String reg,String date){
        long milliseconds = 0;
        try {
            DateFormat df = getDateFormat(reg);
            Date time = df.parse(date);
            milliseconds = time.getTime();
        } catch (ParseException e) {
            milliseconds = 0;
        }
        return milliseconds;
    }
    /**
     * 获取秒
     * @param reg
     * @param date
     * @return
     */
    public static int getSeconds(String reg,String date){
        int seconds = 0;
        if(date == null || "".equals(date.trim())){
            return seconds;
        }
        try {
            DateFormat df = getDateFormat(reg);
            Date time = df.parse(date);
            seconds = new Long(time.getTime()/1000).intValue();
        } catch (ParseException e) {
            seconds = 0;
        }
        return seconds;
    }
    public static int getSeconds(String reg,Date date){
        int seconds = 0;
        if(date == null){
            return seconds;
        }
        seconds = new Long(date.getTime()/1000).intValue();
        return seconds;
    }
    /**
     * 获取当前日期
     * @param reg
     * @return
     */
    public static String getNowTimeFormat(String reg){
        return dateFormat(reg, new Date());
    }
    public static int getNowSeconds(){
        return getSeconds(REG_YYMMDD_HHMMSS, new Date());
    }
    public static Object secondsToDate(int seconds,String reg) {
        return dateFormat(reg, new Date(seconds*1000L));
    }
//
//    /**
//     * 获取两个时间相差的天数
//     * @param start
//     * @param end
//     * @return
//     */
//    public static int getIntervalDays(Date start,Date end){
//        JDateTime jStart = new JDateTime(start);
//        JDateTime jEnd = new JDateTime(end);
//        Period period = new Period(jStart, jEnd);
//        int intervalDay = Long.valueOf(period.getDays()).intValue();
//        return intervalDay + 1;
//    }
//    /**
//     * 获取两个日期之间相差的月数
//     * @param start
//     * @param end
//     * @return
//     */
//    public static int getIntervalMonths(Date start,Date end){
//        JDateTime jStart = new JDateTime(start);
//        JDateTime jEnd = new JDateTime(end);
//        int intervalMonth = (jEnd.getYear() - jStart.getYear())*12 + (jEnd.getMonth() - jStart.getMonth()) ;
//        return intervalMonth;
//    }
//    public static int compare(Date first, Date second) {
//        if(first == null && second == null){
//            return 0;
//        }
//        if(first == null && second != null){
//            return -1;
//        }
//        if(first != null && second == null){
//            return 1;
//        }
//        return first.compareTo(second);
//    }
//
//    public static Integer get(Date date , int calendarField)
//    {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        Integer value = Convert.toInteger(calendar.get(calendarField));
//        return value;
//    }
}