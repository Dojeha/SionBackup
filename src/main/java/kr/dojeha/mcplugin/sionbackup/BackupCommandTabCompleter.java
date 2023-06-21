package kr.dojeha.mcplugin.sionbackup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.LinkedList;
import java.util.List;

public class BackupCommandTabCompleter implements TabCompleter {

    private SionBackup plugin;

    public BackupCommandTabCompleter(SionBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of(new String[]{"list", "info", "run", "reload"});
        }
        List<String> result = new LinkedList<>();
        if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("run")) {
            if (args.length == 2) {
                BackupWorkerManager manager = plugin.getBackupWorkerManager();
                manager.getInfoList().forEach((i) -> {
                    result.add(i.getWorldName());
                });
            }
        }
        return result;
    }
}
