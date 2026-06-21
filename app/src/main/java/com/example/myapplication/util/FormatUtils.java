package com.example.myapplication.util;

import java.net.URI;
import java.util.Locale;

public class FormatUtils {

    /**
     * Extracts a clean domain name from a URL (e.g. "https://vnexpress.net/news" -> "vnexpress").
     */
    public static String extractSourceName(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        try {
            URI uri = new URI(url.trim());
            String domain = uri.getHost();
            if (domain != null) {
                if (domain.startsWith("www.")) {
                    domain = domain.substring(4);
                }
                // Extract first part of the domain name (e.g., "vnexpress.net" -> "vnexpress")
                int dotIndex = domain.indexOf('.');
                if (dotIndex > 0) {
                    return domain.substring(0, dotIndex);
                }
                return domain;
            }
        } catch (Exception e) {
            // Fallback
        }
        
        // Basic fallback extraction if URI parsing fails
        String cleaned = url.replace("http://", "").replace("https://", "");
        if (cleaned.startsWith("www.")) {
            cleaned = cleaned.substring(4);
        }
        int slashIndex = cleaned.indexOf('/');
        if (slashIndex > 0) {
            cleaned = cleaned.substring(0, slashIndex);
        }
        int dotIndex = cleaned.indexOf('.');
        if (dotIndex > 0) {
            return cleaned.substring(0, dotIndex);
        }
        return cleaned;
    }

    /**
     * Formats likes count (e.g. 1200 -> "1.2k", 540 -> "540").
     */
    public static String formatLikesCount(int count) {
        if (count >= 1000) {
            return String.format(Locale.US, "%.1fk", count / 1000.0);
        }
        return String.valueOf(count);
    }

    /**
     * Cleans and formats the source name, ensuring common names like "vnexpress" become "VnExpress"
     * and capitalizing others.
     */
    public static String getCleanSourceName(String urlOrSource) {
        if (urlOrSource == null || urlOrSource.trim().isEmpty()) {
            return "TechByte";
        }
        String clean = urlOrSource.trim();
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            clean = extractSourceName(clean);
        }
        if (clean.equalsIgnoreCase("vnexpress")) {
            return "VnExpress";
        }
        if (clean.equalsIgnoreCase("tinhte")) {
            return "Tinh tế";
        }
        // Capitalize first letter
        if (clean.length() > 0) {
            clean = clean.substring(0, 1).toUpperCase() + clean.substring(1);
        }
        return clean;
    }
}
