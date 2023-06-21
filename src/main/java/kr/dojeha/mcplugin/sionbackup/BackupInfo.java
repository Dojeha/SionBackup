package kr.dojeha.mcplugin.sionbackup;

import java.io.File;
import java.time.LocalDateTime;

public class BackupInfo {
    private String worldName;
    private int minuteInterval;
    private File outputDir;

    private LocalDateTime scheduleStartTime;
    private LocalDateTime lastBackupTime;
    private LocalDateTime nextBackupTime;

    public BackupInfo(String worldName, int minuteInterval, File outputDir) {
        this.worldName = worldName;
        this.minuteInterval = minuteInterval;
        this.outputDir = outputDir;
        scheduleStartTime = LocalDateTime.now();
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinuteInterval() {
        return minuteInterval;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public LocalDateTime getLastBackupTime() {
        return lastBackupTime;
    }

    public LocalDateTime getNextBackupTime() {
        if (nextBackupTime == null) {
            updateNextBackupTime();
        }
        return nextBackupTime;
    }

    protected void updateLastBackupTime() {
        lastBackupTime = LocalDateTime.now();
    }

    protected void updateNextBackupTime() {
        LocalDateTime time = lastBackupTime;
        if (time == null) {
            time = scheduleStartTime;
        }
        nextBackupTime = time.plusMinutes(getMinuteInterval());
    }
}
