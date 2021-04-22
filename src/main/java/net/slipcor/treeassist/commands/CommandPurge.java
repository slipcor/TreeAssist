package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.blocklist.FlatFileBlockList;
import net.slipcor.treeassist.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandPurge extends AbstractCommand {
    public CommandPurge() {
        super(new String[]{"treeassist.purge"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.ERROR_PERMISSION_PURGE));
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{2})) {
            return;
        }
        if (TreeAssist.instance.blockList instanceof FlatFileBlockList) {
            FlatFileBlockList bl = (FlatFileBlockList) TreeAssist.instance.blockList;
            try {
                int days = Integer.parseInt(args[1]);
                int done = bl.purge(days);

                TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.SUCCESSFUL_PURGE_DAYS, String.valueOf(done), args[1]));
            } catch (NumberFormatException e) {
                if (args[1].equalsIgnoreCase("global")) {
                    int done = bl.purge();
                    TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.SUCCESSFUL_PURGE_GLOBAL, String.valueOf(done)));
                } else {
                    int done = bl.purge(args[1]);
                    TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.SUCCESSFUL_PURGE_WORLD, String.valueOf(done), args[1]));
                }
            }
        } else {
            TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.ERROR_ONLY_TREEASSIST_BLOCKLIST));
        }
    }

    @Override
    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("global");

            List<String> worlds = new ArrayList<>();

            for (World world : Bukkit.getServer().getWorlds()) {
                worlds.add(world.getName());
            }

            Collections.sort(worlds);

            results.addAll(worlds);
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing a world, probably

        if ("global".startsWith(args[1].toLowerCase())) {
            results.add("global");
        }
        List<String> worlds = new ArrayList<>();

        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                worlds.add(world.getName());
            }
        }

        Collections.sort(worlds);
        results.addAll(worlds);

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("purge");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!p");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist purge - [global/world/days] {days} - purge entries for worlds/days";
    }
}
