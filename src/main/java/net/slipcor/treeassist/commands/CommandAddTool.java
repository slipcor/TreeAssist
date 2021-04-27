package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.utils.StringUtils;
import net.slipcor.treeassist.utils.ToolUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandAddTool extends CoreCommand {
    public CommandAddTool(CorePlugin plugin) {
        super(plugin, "treeassist.addtool", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_ADDTOOL.parse());
            return;
        }

        if (args.length < 1) {
            TreeAssist.instance.sendPrefixed(sender, ChatColor.DARK_RED + this.getShortInfo());
            return;
        }

        if (args.length < 2) {
            Player player = (Player) sender;
            for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                if (config.getConfigName().contains("default")) {
                    ToolUtils.toolAdd(player, config);
                }
            }
            return;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                if (StringUtils.matchContains(config.getStringList(TreeConfig.CFG.TRUNK_MATERIALS, new ArrayList<>()), args[1], true)) {
                    ToolUtils.toolAdd(player, config);
                }
            }
            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("addtool");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!at");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist addtool {trunk block type} - add a required tool";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
