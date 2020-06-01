package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.TreeConfig;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandAddTool extends AbstractCommand {
    public CommandAddTool() {
        super(new String[]{"treeassist.addtool"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_ADDTOOL));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.DARK_RED + this.getShortInfo());
            return;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (TreeConfig config : Utils.treeConfigs.values()) {
                if (Utils.matchContains(config.getStringList(TreeConfig.CFG.TRUNK_MATERIALS, new ArrayList<>()), args[1], true)) {
                    Utils.addRequiredTool(player, config);
                }
            }
            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
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
}
