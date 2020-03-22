package com.ash.randomzy.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampUtil {

    public static String getTimeIn12HourFormat(String timeStamp) {
        Long time = Long.parseLong(timeStamp);
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(date);
    }

    public static String getTimeIn12HourFormat(long timeStamp) {
        Date date = new Date(timeStamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(date);
    }

    public static String getTimeLabel(long timeStamp) {
        Date date = new Date(timeStamp);
        if (checkIfToday(date))
            return getTimeIn12HourFormat(timeStamp);
        else if (checkIfYesterday(date))
            return "Yesterday";
        else if (checkIfYear(date))
            return getDateForThisYear(date);
        else
            return getDateWithYear(date);
    }

    private static String getDateWithYear(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YY");
        return dateFormat.format(date);
    }

    private static String getDateForThisYear(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
        return dateFormat.format(date);
    }


    private static boolean checkIfYear(Date date) {
        Date now = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy");
        return fmt.format(now).equals(fmt.format(date));
    }

    private static boolean checkIfYesterday(Date date) {
        String inp = getDateWithYear(date);
        String today = getDateWithYear(new Date());
        String strInp[] = inp.split("/");
        String strToday[] = today.split("/");
        return (Integer.valueOf(strInp[0]) == Integer.valueOf(strToday[0]) -1)
                && (Integer.valueOf(strInp[1]) == Integer.valueOf(strToday[1]))
                && (Integer.valueOf(strInp[2]) == Integer.valueOf(strToday[2]));
    }

    private static boolean checkIfToday(Date date) {
        Date now = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(now).equals(fmt.format(date));
    }

}
