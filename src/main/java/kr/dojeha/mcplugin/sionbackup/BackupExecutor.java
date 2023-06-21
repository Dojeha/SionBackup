package kr.dojeha.mcplugin.sionbackup;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

public class BackupExecutor {
    private Consumer<String> infoAction;
    private Consumer<String> warningAction;
    private Consumer<String> sendWebhookAction;

    public BackupExecutor(SionBackup plugin) {
        infoAction = plugin.getLogger()::info;
        warningAction = plugin.getLogger()::warning;
        sendWebhookAction = plugin::sendWebhook;
    }

    public BackupExecutor(SionBackup plugin, CommandSender sender) {
        this(plugin);
        infoAction = infoAction.andThen(sender::sendMessage);
        warningAction = warningAction.andThen((msg) -> {
            sender.sendMessage(ChatColor.YELLOW + msg);
        });
    }

    public void info(String msg) {
        infoAction.accept(msg);
    }

    public void warning(String msg) {
        warningAction.accept(msg);
    }

    public void sendWebhook(String msg) {
        sendWebhookAction.accept(msg);
    }
}
