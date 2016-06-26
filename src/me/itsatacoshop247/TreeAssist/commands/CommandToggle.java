package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandToggle extends AbstractCommand {
    public CommandToggle() {
        super(new String[]{"treeassist.toggle"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE));
            return;
        }

        // toggle PLAYER WORLD
        // toggle PLAYER
        // toggle WORLD

        if (args.length > 1) {

            if (args.length > 2) {
                if (Bukkit.getWorld(args[2]) == null) {
                    sender.sendMessage(Language.parse(Language.MSG.ERROR_NOTFOUND_WORLD, args[2]));
                    return;
                }

                if (!sender.hasPermission("treeassist.toggle.other")) {
                    sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE_OTHER));
                    return;
                }

                if (Utils.plugin.toggleWorld(args[2], args[1])) {
                    sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_OTHER_WORLD_ON, args[1], args[2]));
                } else {
                    sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_OTHER_WORLD_OFF, args[1], args[2]));
                }
                return;
            }
            if (Bukkit.getWorld(args[1]) == null) {
                if (!sender.hasPermission("treeassist.toggle.other")) {
                    sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE_OTHER));
                    return;
                }

                if (Utils.plugin.toggleGlobal(args[1])) {
                    sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_OTHER_ON, args[1]));
                } else {
                    sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_OTHER_OFF, args[1]));
                }
                return;
            }

            if (Utils.plugin.toggleWorld(args[1], sender.getName())) {
                sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_ON, args[1]));
            } else {
                sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_OFF, args[1]));
            }

            return;
        }

        if (Utils.plugin.toggleGlobal(sender.getName())) {
            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_YOU_ON));
        } else {
            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_YOU_OFF));
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("toggle");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tg");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist toggle [player/world] {world} - toggle plugin usage for you/others";
    }

    @Override
    public CommandTree<String> getSubs() {
        final CommandTree<String> result = new CommandTree<>(null);
        for (World world : Bukkit.getServer().getWorlds()) {
            result.define(new String[]{"{Player}", world.getName()});
            result.define(new String[]{world.getName()});
        }
        result.define(new String[]{"{Player}"});
        return result;
    }
}
