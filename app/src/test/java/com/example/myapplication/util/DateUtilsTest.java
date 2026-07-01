package com.example.myapplication.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtilsTest {
    @Test
    public void formatsRecentDateToRelativeTime() {
        // Create an ISO 8601 date string representing 10 minutes ago
        long tenMinutesAgoMillis = System.currentTimeMillis() - (10 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String rawDate = sdf.format(new Date(tenMinutesAgoMillis));

        String relativeTime = DateUtils.formatPublishedDate(rawDate);
        assertEquals("10 phút trước", relativeTime);
    }

    @Test
    public void formatsOlderDateToStandardFormat() {
        // Create an ISO 8601 date string representing 10 days ago
        long tenDaysAgoMillis = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String rawDate = sdf.format(new Date(tenDaysAgoMillis));

        String relativeTime = DateUtils.formatPublishedDate(rawDate);
        // Should contain slash separators for day/month/year e.g. 11/06/2026
        assertTrue(relativeTime.contains("/"));
    }

    @Test
    public void formatsCommentDateUnder20Minutes() {
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String rawDate = sdf.format(new Date(fiveMinutesAgo));

        assertEquals("Vừa xong", DateUtils.formatCommentDate(rawDate));
    }

    @Test
    public void formatsCommentDateToday() {
        // Create a date that is 30 minutes ago, but definitely today
        long thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000);
        java.util.Calendar nowCal = java.util.Calendar.getInstance();
        java.util.Calendar targetCal = java.util.Calendar.getInstance();
        targetCal.setTimeInMillis(thirtyMinutesAgo);
        
        if (nowCal.get(java.util.Calendar.DAY_OF_YEAR) == targetCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String rawDate = sdf.format(new Date(thirtyMinutesAgo));

            assertEquals("Hôm nay", DateUtils.formatCommentDate(rawDate));
        }
    }

    @Test
    public void formatsCommentDateYesterday() {
        java.util.Calendar yesterdayCal = java.util.Calendar.getInstance();
        yesterdayCal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        yesterdayCal.set(java.util.Calendar.HOUR_OF_DAY, 12);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String rawDate = sdf.format(yesterdayCal.getTime());

        assertEquals("1 ngày trước", DateUtils.formatCommentDate(rawDate));
    }

    @Test
    public void formatsCommentDateTwoDaysAgo() {
        java.util.Calendar twoDaysAgoCal = java.util.Calendar.getInstance();
        twoDaysAgoCal.add(java.util.Calendar.DAY_OF_YEAR, -2);
        twoDaysAgoCal.set(java.util.Calendar.HOUR_OF_DAY, 12);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String rawDate = sdf.format(twoDaysAgoCal.getTime());

        assertEquals("2 ngày trước", DateUtils.formatCommentDate(rawDate));
    }

    @Test
    public void formatsCommentDateOlder() {
        java.util.Calendar tenDaysAgoCal = java.util.Calendar.getInstance();
        tenDaysAgoCal.add(java.util.Calendar.DAY_OF_YEAR, -10);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String rawDate = sdf.format(tenDaysAgoCal.getTime());
        
        SimpleDateFormat expectedSdf = new SimpleDateFormat("dd/MM/yyyy");
        String expected = expectedSdf.format(tenDaysAgoCal.getTime());

        assertEquals(expected, DateUtils.formatCommentDate(rawDate));
    }
}
