package com.example.myapplication.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    public static String formatPublishedDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Vừa xong";
        try {
            SimpleDateFormat inputFormat;
            if (rawDate.contains("T")) {
                if (rawDate.contains(".")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                }
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            }

            Date date = inputFormat.parse(rawDate.trim());
            if (date == null) return rawDate;

            long diff = System.currentTimeMillis() - date.getTime();
            long diffSeconds = diff / 1000;
            long diffMinutes = diff / (60 * 1000);
            long diffHours = diff / (60 * 60 * 1000);
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffSeconds < 60) {
                return "Vừa xong";
            } else if (diffMinutes < 60) {
                return diffMinutes + " phút trước";
            } else if (diffHours < 24) {
                return diffHours + " giờ trước";
            } else if (diffDays < 7) {
                return diffDays + " ngày trước";
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi"));
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            return rawDate;
        }
    }
}
