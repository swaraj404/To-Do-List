package com.swaraj.todolist.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural language date/time parser for parsing user input like
 * "tomorrow at 6pm", "next Friday", "in 3 days", etc.
 */
public class NaturalLanguageDateParser {
    
    // Common patterns for natural language dates
    private static final Pattern TOMORROW_PATTERN = Pattern.compile("(?i)tomorrow(\\s+at\\s+(\\d{1,2})(:\\d{2})?(\\s*[ap]m)?)?");
    private static final Pattern TODAY_PATTERN = Pattern.compile("(?i)today(\\s+at\\s+(\\d{1,2})(:\\d{2})?(\\s*[ap]m)?)?");
    private static final Pattern IN_DAYS_PATTERN = Pattern.compile("(?i)in\\s+(\\d+)\\s+days?(\\s+at\\s+(\\d{1,2})(:\\d{2})?(\\s*[ap]m)?)?");
    private static final Pattern NEXT_WEEK_PATTERN = Pattern.compile("(?i)next\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)(\\s+at\\s+(\\d{1,2})(:\\d{2})?(\\s*[ap]m)?)?");
    private static final Pattern TIME_PATTERN = Pattern.compile("(?i)(\\d{1,2})(:\\d{2})?(\\s*[ap]m)?");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
    
    /**
     * Parse natural language date/time input
     */
    public static LocalDateTime parseDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            return LocalDateTime.now().plusDays(1); // Default to tomorrow
        }
        
        input = input.trim().toLowerCase();
        
        // Try to parse "tomorrow" patterns
        Matcher matcher = TOMORROW_PATTERN.matcher(input);
        if (matcher.find()) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalTime time = parseTime(matcher.group(2), matcher.group(3), matcher.group(4));
            return LocalDateTime.of(tomorrow, time);
        }
        
        // Try to parse "today" patterns
        matcher = TODAY_PATTERN.matcher(input);
        if (matcher.find()) {
            LocalDate today = LocalDate.now();
            LocalTime time = parseTime(matcher.group(2), matcher.group(3), matcher.group(4));
            return LocalDateTime.of(today, time);
        }
        
        // Try to parse "in X days" patterns
        matcher = IN_DAYS_PATTERN.matcher(input);
        if (matcher.find()) {
            int days = Integer.parseInt(matcher.group(1));
            LocalDate futureDate = LocalDate.now().plusDays(days);
            LocalTime time = parseTime(matcher.group(3), matcher.group(4), matcher.group(5));
            return LocalDateTime.of(futureDate, time);
        }
        
        // Try to parse "next [day of week]" patterns
        matcher = NEXT_WEEK_PATTERN.matcher(input);
        if (matcher.find()) {
            String dayName = matcher.group(1);
            LocalDate nextDay = getNextDayOfWeek(dayName);
            LocalTime time = parseTime(matcher.group(3), matcher.group(4), matcher.group(5));
            return LocalDateTime.of(nextDay, time);
        }
        
        // Try to parse MM/dd/yyyy format
        matcher = DATE_PATTERN.matcher(input);
        if (matcher.find()) {
            try {
                int month = Integer.parseInt(matcher.group(1));
                int day = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));
                LocalDate date = LocalDate.of(year, month, day);
                
                // Look for time in the rest of the string
                String remaining = input.substring(matcher.end());
                LocalTime time = parseTimeFromString(remaining);
                return LocalDateTime.of(date, time);
            } catch (Exception e) {
                // Fall through to default
            }
        }
        
        // Try to parse just time (assume today)
        matcher = TIME_PATTERN.matcher(input);
        if (matcher.find()) {
            LocalTime time = parseTime(matcher.group(1), matcher.group(2), matcher.group(3));
            LocalDate date = LocalDate.now();
            // If the time has already passed today, use tomorrow
            if (time.isBefore(LocalTime.now())) {
                date = date.plusDays(1);
            }
            return LocalDateTime.of(date, time);
        }
        
        // Try standard date/time formats
        try {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e) {
            // Try other formats
            try {
                return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
            } catch (DateTimeParseException e2) {
                // Default to tomorrow at 9 AM
                return LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
            }
        }
    }
    
    /**
     * Parse time from captured groups
     */
    private static LocalTime parseTime(String hourStr, String minuteStr, String ampmStr) {
        if (hourStr == null) {
            return LocalTime.of(9, 0); // Default to 9 AM
        }
        
        int hour = Integer.parseInt(hourStr);
        int minute = 0;
        
        if (minuteStr != null) {
            minute = Integer.parseInt(minuteStr.substring(1)); // Remove the ':'
        }
        
        // Handle AM/PM
        if (ampmStr != null) {
            ampmStr = ampmStr.trim().toLowerCase();
            if (ampmStr.contains("pm") && hour != 12) {
                hour += 12;
            } else if (ampmStr.contains("am") && hour == 12) {
                hour = 0;
            }
        } else if (hour < 8) {
            // Assume PM for hours less than 8 (e.g., "6" probably means 6 PM)
            hour += 12;
        }
        
        // Ensure valid hour range
        if (hour >= 24) hour = 23;
        if (hour < 0) hour = 0;
        if (minute >= 60) minute = 59;
        if (minute < 0) minute = 0;
        
        return LocalTime.of(hour, minute);
    }
    
    /**
     * Parse time from a string
     */
    private static LocalTime parseTimeFromString(String input) {
        Matcher matcher = TIME_PATTERN.matcher(input);
        if (matcher.find()) {
            return parseTime(matcher.group(1), matcher.group(2), matcher.group(3));
        }
        return LocalTime.of(9, 0); // Default to 9 AM
    }
    
    /**
     * Get the next occurrence of a day of the week
     */
    private static LocalDate getNextDayOfWeek(String dayName) {
        LocalDate today = LocalDate.now();
        int targetDay = switch (dayName.toLowerCase()) {
            case "monday" -> 1;
            case "tuesday" -> 2;
            case "wednesday" -> 3;
            case "thursday" -> 4;
            case "friday" -> 5;
            case "saturday" -> 6;
            case "sunday" -> 7;
            default -> 1; // Default to Monday
        };
        
        int currentDay = today.getDayOfWeek().getValue();
        int daysToAdd = (targetDay - currentDay + 7) % 7;
        if (daysToAdd == 0) {
            daysToAdd = 7; // Next week
        }
        
        return today.plusDays(daysToAdd);
    }
    
    /**
     * Extract task description from input that contains date/time information
     */
    public static String extractTaskDescription(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        // Remove common date/time patterns to extract the main task description
        String cleaned = input.trim();
        
        // Remove patterns like "tomorrow at 6pm", "next friday", etc.
        cleaned = cleaned.replaceAll("(?i)\\s+(tomorrow|today)(\\s+at\\s+\\d{1,2}(:\\d{2})?(\\s*[ap]m)?)?", "");
        cleaned = cleaned.replaceAll("(?i)\\s+in\\s+\\d+\\s+days?(\\s+at\\s+\\d{1,2}(:\\d{2})?(\\s*[ap]m)?)?", "");
        cleaned = cleaned.replaceAll("(?i)\\s+next\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)(\\s+at\\s+\\d{1,2}(:\\d{2})?(\\s*[ap]m)?)?", "");
        cleaned = cleaned.replaceAll("(?i)\\s+at\\s+\\d{1,2}(:\\d{2})?(\\s*[ap]m)?", "");
        cleaned = cleaned.replaceAll("\\s+\\d{1,2}/\\d{1,2}/\\d{4}(\\s+\\d{1,2}:\\d{2})?", "");
        
        return cleaned.trim();
    }
}
