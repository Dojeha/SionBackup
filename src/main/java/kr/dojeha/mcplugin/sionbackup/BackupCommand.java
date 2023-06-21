package kr.dojeha.mcplugin.sionbackup;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class BackupCommand implements CommandExecutor {

    private SionBackup plugin;

    public BackupCommand(SionBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/backup list");
            sender.sendMessage("/backup info <worldname>");
            sender.sendMessage("/backup run <worldname>");
            sender.sendMessage("/backup reload");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            return listCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase("info")) {
            return infoCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase("run")) {
            return runCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase("reload")) {
            return reloadCommand(sender, args);
        }
        return false;
    }

    private boolean listCommand(CommandSender sender, String[] args) {
        BackupWorkerManager manager = plugin.getBackupWorkerManager();
        manager.getInfoList().forEach((i) -> {
            String name = i.getWorldName();
            long minuteInterval = i.getMinuteInterval();
            sender.sendMessage(name + " (" + ChatColor.GREEN + minuteInterval + ChatColor.RESET + "분)");
        });
        return true;
    }

    private boolean infoCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "/backup info <name>");
            return true;
        }
        BackupWorkerManager manager = plugin.getBackupWorkerManager();
        BackupInfo info = manager.findInfo(args[1]);
        if (info == null) {
            sender.sendMessage(ChatColor.RED + args[1] + "(은)는 없습니다.");
            return true;
        }
        sender.sendMessage("이름: " + ChatColor.GREEN + info.getWorldName() + ChatColor.RESET);
        sender.sendMessage("백업주기: " + ChatColor.GREEN + info.getMinuteInterval() + ChatColor.RESET + "분");
        sender.sendMessage("다음 백업: " + DateTimeFormat.LOG.format(info.getNextBackupTime()));
        return true;
    }

    private boolean runCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "/backup run <worldname>");
            return true;
        }
        BackupExecutor executor;
        if (sender instanceof ConsoleCommandSender) {
            executor = new BackupExecutor(plugin);
        }
        else if (sender instanceof Player) {
            executor = new BackupExecutor(plugin, sender);
        }
        else {
            return false;
        }
        BackupWorkerManager manager = plugin.getBackupWorkerManager();
        manager.runTask(manager.findInfo(args[1]), executor);
        return true;
    }

    private boolean reloadCommand(CommandSender sender, String[] args) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "백업 리로드 완료");
        return true;
    }
}
