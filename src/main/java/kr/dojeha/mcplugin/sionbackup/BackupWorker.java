package kr.dojeha.mcplugin.sionbackup;

import com.google.common.io.Files;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class BackupWorker {
    private BackupWorkerManager manager;
    private BackupInfo info;
    private BackupExecutor executor;

    private boolean isRunning;
    private Thread thread = null;
    private Runnable runnable = () -> {
        File zipFile = tryPackWorld();

        String fileSize = convFileSize(zipFile.length());
        float elapsedSec = getElapsedSec(info.getLastBackupTime(), LocalDateTime.now());
        String detail = " (" + fileSize + ", " + elapsedSec + "s)";

        try {
            String worldName = info.getWorldName();
            String logMessage = worldName + " ==> " + zipFile.getCanonicalPath() + detail;
            executor.info(logMessage);
            executor.sendWebhook(logMessage);
        } catch (IOException e) {
            executor.warning(e.getLocalizedMessage());
        }

        manager.removeWorker(info);
        isRunning = false;
        thread = null;
    };

    public BackupWorker(BackupWorkerManager manager, BackupInfo info, BackupExecutor executor) {
        this.manager = manager;
        this.info = info;
        this.executor = executor;
    }

    protected synchronized void start() {
        if (isAlive()) return;
        isRunning = true;
        thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("BackupThread-" + info.getWorldName());
        thread.start();
    }

    protected synchronized void stop() {
        if (isAlive() == false) return;
        manager.removeWorker(info);
        isRunning = false;
        thread.interrupt();
        thread = null;
    }

    public boolean isAlive() {
        return (isRunning || thread != null);
    }

    public BackupInfo getInfo() {
        return info;
    }

    private File tryPackWorld() {
        String worldName = info.getWorldName();
        File outputDir = info.getOutputDir();

        if (outputDir.exists() == false) {
            outputDir.mkdir();
        }

        String fileName = DateTimeFormat.FILE.format(info.getLastBackupTime()) + " " + worldName;
        File tempFile = new File(outputDir, fileName + ".bt");
        File zipFile = new File(outputDir, fileName + ".zip");
        try {
            ZipUtil.pack(new File("./" + worldName), tempFile);
        } catch (Exception e) {
            executor.warning(e.getLocalizedMessage());
        }
        try {
            Files.move(tempFile, zipFile);
        } catch (IOException e) {
            executor.warning(e.getLocalizedMessage());
        }
        return zipFile;
    }

    private float getElapsedSec(LocalDateTime start, LocalDateTime end) {
        Duration d = Duration.between(start, end);
        return d.getSeconds() + (long) (d.getNano() * 1e-8) * 0.1f;
    }

    private String convFileSize(long bytes) {
        String[] unitSuffixes = {"B", "KB", "MB", "GB", "TB"};
        long[] unitSizes = {1, 1_000, 1_000_000, 1_000_000_000, 1_000_000_000_000L};
        int uIndex = 0;
        for (int i = 0; i < unitSizes.length; i++) {
            if (bytes > unitSizes[i]) {
                uIndex = i;
            }
        }
        // 나누면서 소수점 1자리 남기기
        return ((bytes * 10) / unitSizes[uIndex]) * 0.1f + unitSuffixes[uIndex];
    }
}
