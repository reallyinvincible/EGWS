package com.exuberant.egws;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

    public static final int NAMEBOARD_REQUEST = 101;
    public static final int BUILDING_REQUEST = 102;

    public static List<String> getCurrentTime(){
        List<String> timeList = new ArrayList<>();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
        timeList.add(simpleDateFormat.format(date));
        timeList.add(simpleTimeFormat.format(date));
        return timeList;
    }

    public static double calculateDistance(double lat1, double lat2, double lon1,
                                           double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = 0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

}
