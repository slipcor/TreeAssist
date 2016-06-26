package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTool extends AbstractCommand {
    public CommandTool() {
        super(new String[]{"treeassist.tool"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE_TOOL));
            return;

        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean found = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    if (item.hasItemMeta()) {
                        if (Utils.plugin.listener.isProtectTool(item)) {
                            player.getInventory().removeItem(item);
                            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOOL_OFF));
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                player.getInventory().addItem(Utils.plugin.listener.getProtectionTool());
                sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOOL_ON));
            }
            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("commandtool", "tool");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
