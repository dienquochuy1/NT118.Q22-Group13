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
}
