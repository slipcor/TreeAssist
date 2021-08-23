package net.slipcor.treeassist.utils;

import net.slipcor.core.ConfigEntry;
import net.slipcor.core.CoreDebugger;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandUtils {
    public static CoreDebugger debug;

    public static void commitBlock(Player player, TreeConfig config) {
        commit(player, config, TreeConfig.CFG.COMMANDS_PER_BLOCK);
    }

    public static void commitTree(Player player, TreeConfig config) {
        commit(player, config, TreeConfig.CFG.COMMANDS_PER_TREE);
    }

    private static void commit(Player player, TreeConfig config, ConfigEntry entry) {
        if (player == null || config == null) {
            debug.i("no player or block for command: " + entry);
            return;
        }
        debug.i("reading config: " + config.getConfigName());

        List<String> commands = config.getStringList(entry);
        if (commands.isEmpty()) {
            debug.i("commands empty: " + entry);
            return;
        }

        for (String command : commands) {
            command = command.replace("%player%", player.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
