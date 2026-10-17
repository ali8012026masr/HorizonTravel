package com.example.horizontravel.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Formatters {
    private Formatters() {}

    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    public static String capitalizeEnum(String enumValue) {
        if (enumValue == null || enumValue.trim().isEmpty()) {
            return "";
        }
        String[] parts = enumValue.toLowerCase(Locale.US).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return sb.toString();
    }

    public static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        String cleaned = dateStr.trim();
        if (cleaned.contains("T")) {
            cleaned = cleaned.substring(0, cleaned.indexOf('T'));
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(cleaned);
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                return outputFormat.format(date);
            }
        } catch (ParseException ignored) {
        }
        return dateStr;
    }

    public static String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return "";
        }
        String normalized = dateTimeStr.trim();
        // Defensive support for standard ISO strings (e.g. 2026-10-01T14:30:00 or 2026-10-01T14:30)
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.US);
                Date date = inputFormat.parse(normalized);
                if (date != null) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
                    return outputFormat.format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return dateTimeStr;
    }

    public static String formatTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return "";
        }
        String normalized = timeStr.trim();
        String[] patterns = new String[] {"HH:mm:ss", "HH:mm"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.US);
                Date date = inputFormat.parse(normalized);
                if (date != null) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                    return outputFormat.format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return timeStr;
    }
}
