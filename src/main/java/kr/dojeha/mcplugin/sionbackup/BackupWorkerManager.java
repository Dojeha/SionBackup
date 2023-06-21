package kr.dojeha.mcplugin.sionbackup;

import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class BackupWorkerManager {
    private HashMap<String, BackupInfo> infoMap = new LinkedHashMap<>();
    private HashMap<BackupInfo, BackupWorker> workerMap = new LinkedHashMap<>();

    private SionBackup plugin;
    private BukkitScheduler scheduler;

    public BackupWorkerManager(SionBackup plugin) {
        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();
    }

    public synchronized void addInfo(BackupInfo info) {
        String name = info.getWorldName();
        if (infoMap.containsKey(name)) {
            return;
        }
        infoMap.put(name, info);
    }

    protected synchronized void removeWorker(BackupInfo info) {
        workerMap.remove(info);
    }

    private void task(BackupInfo info, BackupExecutor executor, boolean isTimer) {
        if (workerMap.containsKey(info)) {
            executor.warning(info.getWorldName() + " 백업을 시작할 수 없음 (이미 진행중)");
            return;
        }
        info.updateLastBackupTime();
        if (isTimer) {
            info.updateNextBackupTime();
        }
        BackupWorker worker = new BackupWorker(this, info, executor);
        workerMap.put(info, worker);
        executor.info(info.getWorldName() + " 백업 시작");
        worker.start();
    }

    public synchronized void runTask(BackupInfo info, BackupExecutor executor) {
        scheduler.runTask(plugin, () -> {
            task(info, executor, false);
        });
    }

    public synchronized void runTaskTimer(BackupInfo info, BackupExecutor executor) {
        long period = 20L * 60L *  info.getMinuteInterval();
        scheduler.runTaskTimer(plugin, () -> {
            task(info, executor, true);
        }, period, period);
    }

    public synchronized void stopAllTasks() {
        scheduler.cancelTasks(plugin);
        new LinkedList<>(workerMap.values()).forEach((w) -> {
            plugin.getLogger().warning(w.getInfo().getWorldName() + " 백업 종료");
            w.stop();
        });
        workerMap.clear();
    }

    public List<BackupInfo> getInfoList() {
        return new LinkedList<>(infoMap.values());
    }

    public List<BackupWorker> getWorkerList() {
        return new LinkedList<>(workerMap.values());
    }

    public BackupInfo findInfo(String worldName) {
        return infoMap.get(worldName);
    }

    public BackupWorker findWorker(String worldName) {
        BackupInfo info = findInfo(worldName);
        if (info == null) {
            return null;
        }
        return workerMap.get(info);
    }
}
