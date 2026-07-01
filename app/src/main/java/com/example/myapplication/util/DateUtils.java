package com.example.myapplication.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            }
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

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

    public static String formatCommentDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Vừa xong";
        try {
            SimpleDateFormat inputFormat;
            if (rawDate.contains("T")) {
                if (rawDate.contains(".")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                }
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            }
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(rawDate.trim());
            if (date == null) return rawDate;

            long diff = System.currentTimeMillis() - date.getTime();
            if (diff < 0) {
                return "Vừa xong";
            }
            long diffMinutes = diff / (60 * 1000);
            if (diffMinutes < 20) {
                return "Vừa xong";
            }

            Calendar nowCal = Calendar.getInstance();
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date);

            boolean isToday = nowCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)
                    && nowCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR);

            if (isToday) {
                return "Hôm nay";
            }

            // Truncate both to midnight to find exact calendar day difference
            Calendar nowMidnight = Calendar.getInstance();
            nowMidnight.set(Calendar.HOUR_OF_DAY, 0);
            nowMidnight.set(Calendar.MINUTE, 0);
            nowMidnight.set(Calendar.SECOND, 0);
            nowMidnight.set(Calendar.MILLISECOND, 0);

            Calendar dateMidnight = Calendar.getInstance();
            dateMidnight.setTime(date);
            dateMidnight.set(Calendar.HOUR_OF_DAY, 0);
            dateMidnight.set(Calendar.MINUTE, 0);
            dateMidnight.set(Calendar.SECOND, 0);
            dateMidnight.set(Calendar.MILLISECOND, 0);

            long diffMs = nowMidnight.getTimeInMillis() - dateMidnight.getTimeInMillis();
            long diffDays = diffMs / (24 * 60 * 60 * 1000);

            if (diffDays == 1) {
                return "1 ngày trước";
            } else if (diffDays == 2) {
                return "2 ngày trước";
            } else if (diffDays > 2 && diffDays < 7) {
                return diffDays + " ngày trước";
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            return rawDate;
        }
    }
}
