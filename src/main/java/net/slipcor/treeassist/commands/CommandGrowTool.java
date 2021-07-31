package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.TreeType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGrowTool extends CoreCommand {
    public CommandGrowTool(CorePlugin plugin) {
        super(plugin, "treeassist.growtool", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE_GROWTOOL.parse());
            return;

        }
        if (sender instanceof Player) {
            TreeType species;
            try {
                species = TreeType.valueOf(args[1]);

                Player player = (Player) sender;
                boolean found = false;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        if (item.hasItemMeta()) {
                            if (TreeAssist.instance.getPlayerListener().isGrowTool(item)) {
                                player.getInventory().removeItem(item);
                                TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_GROWTOOL_OFF.parse());
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    player.getInventory().addItem(TreeAssist.instance.getPlayerListener().getGrowTool(species));
                    TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_GROWTOOL_ON.parse(species.name()));
                }

            } catch (Exception e) {
                String list = org.apache.commons.lang.StringUtils.join(TreeType.values(), ", ");
                TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_INVALID_TREETYPE.parse(list));
            }
            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("growtool");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!gt");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist growtool - toggle the grow tool";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        List<String> list = new ArrayList<>();
        if (strings.length > 2) {
            return list;
        }
        for(TreeType type : TreeType.values()) {
            if (strings.length < 2 || type.name().contains(strings[1].toUpperCase())) {
                list.add(type.name());
            }
        }
        return list;
    }
}
