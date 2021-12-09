package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebugTool extends CoreCommand {

    public CommandDebugTool(CorePlugin plugin) {
        super(plugin, "treeassist.debugtool", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE_DEBUGTOOL.parse());
            return;

        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean found = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    if (item.hasItemMeta()) {
                        if (TreeAssist.instance.getPlayerListener().isDebugTool(item)) {
                            player.getInventory().removeItem(item);
                            TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_DEBUGTOOL_OFF.parse());
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                player.getInventory().addItem(TreeAssist.instance.getPlayerListener().getDebugTool());
                TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_DEBUGTOOL_ON.parse());
            }
            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debugtool");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!dt");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist debugtool - toggle the debug tool";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
