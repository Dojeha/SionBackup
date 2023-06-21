package kr.dojeha.mcplugin.sionbackup;

import club.minnced.discord.webhook.WebhookClient;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SionBackup extends JavaPlugin {

    private BackupWorkerManager manager;
    private List<WebhookClient> webhooks = new LinkedList<>();

    @Override
    public void onEnable() {
        this.getCommand("backup").setExecutor(new BackupCommand(this));
        this.getCommand("backup").setTabCompleter(new BackupCommandTabCompleter(this));
        this.getCommand("backup").setPermission("backup");

        loadSettings();
        startScheduler();
    }

    @Override
    public void onDisable() {
        clearWebhook();
        manager.stopAllTasks();
    }

    public void reload() {
        clearWebhook();
        manager.stopAllTasks();
        this.reloadConfig();
        loadSettings();
        startScheduler();
    }

    private void loadSettings() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults();
        manager = new BackupWorkerManager(this);

        if (this.getConfig().getBoolean("allow_backup") == false) return;

        List<String> webhookUrls = this.getConfig().getStringList("send_log_discord_webhooks");
        for (String url : webhookUrls) {
            webhooks.add(WebhookClient.withUrl(url));
        }

        File backupOutputDir = new File(this.getConfig().getString("backup_output_dir"));
        for (Object obj : this.getConfig().getList("backup_worlds")) {
            Map<String, Object> map = (Map<String, Object>) obj;
            String name = (String) map.get("name");
            int minuteInterval = (int) map.get("minute_interval");

            manager.addInfo(new BackupInfo(name, minuteInterval, backupOutputDir));
        }
    }

    private void startScheduler() {
        BackupExecutor executor = new BackupExecutor(this);
        for (BackupInfo info : manager.getInfoList()) {
            manager.runTaskTimer(info, executor);
        }
    }

    public void clearWebhook() {
        for (WebhookClient client : webhooks) {
            client.close();
        }
        Iterator<WebhookClient> iter = webhooks.iterator();
        while(iter.hasNext()) {
            iter.next().close();
            iter.remove();
        }
    }

    public void sendWebhook(String msg) {
        for (WebhookClient client : webhooks) {
            client.send(msg);
        }
    }

    public BackupWorkerManager getBackupWorkerManager() {
        return manager;
    }
}
