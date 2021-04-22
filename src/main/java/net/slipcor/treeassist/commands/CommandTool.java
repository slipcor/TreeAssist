package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.Language;
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
            TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE_TOOL));
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
                            TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.SUCCESSFUL_TOOL_OFF));
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                player.getInventory().addItem(TreeAssist.instance.getBlockListener().getProtectionTool());
                TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.SUCCESSFUL_TOOL_ON));
            }
            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
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
}
