package com.ash.randomzy.utility;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampUtil {

    public static String getTimeIn12HourFormat(String timpestamp){
        Long time = Long.parseLong(timpestamp);
        Timestamp timestamp = new Timestamp(time);
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(date);
    }

}
