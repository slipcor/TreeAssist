package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTool extends CoreCommand {
    public CommandTool(CorePlugin plugin) {
        super(plugin, "treeassist.tool", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE_TOOL.parse());
            return;

        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean found = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    if (item.hasItemMeta()) {
                        if (TreeAssist.instance.getBlockListener().isProtectTool(item)) {
                            player.getInventory().removeItem(item);
                            TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_TOOL_OFF.parse());
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                player.getInventory().addItem(TreeAssist.instance.getBlockListener().getProtectionTool());
                TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_TOOL_ON.parse());
            }
            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("commandtool", "tool");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!t");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist tool - toggle the sapling protection tool";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
