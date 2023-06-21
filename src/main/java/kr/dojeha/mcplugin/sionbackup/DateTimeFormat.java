package kr.dojeha.mcplugin.sionbackup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum DateTimeFormat {
    FILE("yyyy-MM-dd HH_mm_ss"),
    LOG("yyyy-mm-dd HH:mm:ss");

    private DateTimeFormatter formatter;

    DateTimeFormat(String pattern) {
        formatter = DateTimeFormatter.ofPattern(pattern);
    }

    public String format(LocalDateTime dateTime) {
        return formatter.format(dateTime);
    }
}

