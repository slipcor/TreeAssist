package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.blocklists.FlatFileBlockList;
import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandPurge extends AbstractCommand {
    public CommandPurge() {
        super(new String[]{"treeassist.purge"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_PURGE));
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{2})) {
            return;
        }
        if (Utils.plugin.blockList instanceof FlatFileBlockList) {
            FlatFileBlockList bl = (FlatFileBlockList) Utils.plugin.blockList;
            try {
                int days = Integer.parseInt(args[1]);
                int done = bl.purge(days);

                sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_PURGE_DAYS, String.valueOf(done), args[1]));
            } catch (NumberFormatException e) {
                if (args[1].equalsIgnoreCase("global")) {
                    int done = bl.purge(sender);
                    sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_PURGE_GLOBAL, String.valueOf(done)));
                } else {
                    int done = bl.purge(args[1]);
                    sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_PURGE_WORLD, String.valueOf(done), args[1]));
                }
            }
        } else {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_TREEASSIST_BLOCKLIST));
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("purge");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!p");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist purge - [global/world/days] {days} - purge entries for worlds/days";
    }

    @Override
    public CommandTree<String> getSubs() {
        final CommandTree<String> result = new CommandTree<>(null);
        for (World world : Bukkit.getServer().getWorlds()) {
            result.define(new String[]{world.getName()});
        }
        result.define(new String[]{"global"});
        return result;
    }
}
