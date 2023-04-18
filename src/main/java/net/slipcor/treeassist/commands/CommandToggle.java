package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandToggle extends CoreCommand {
    public CommandToggle(CorePlugin plugin) {
        super(plugin, "treeassist.toggle", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE.parse());
            return;
        }

        // toggle PLAYER WORLD
        // toggle PLAYER
        // toggle WORLD

        if (args.length > 1 && !args[1].toLowerCase().equals("check")) {

            if (args.length > 2 && !args[2].toLowerCase().equals("check")) {
                if (Bukkit.getWorld(args[2]) == null) {
                    TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_NOTFOUND_WORLD.parse(args[2]));
                    return;
                }

                if (!sender.hasPermission("treeassist.toggle.other")) {
                    TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE_OTHER.parse());
                    return;
                }

                if (TreeAssist.instance.toggleWorld(args[2], args[1])) {
                    TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_TOGGLE_OTHER_WORLD_ON.parse(args[1], args[2]));
                } else {
                    TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_TOGGLE_OTHER_WORLD_OFF.parse(args[1], args[2]));
                }
                return;
            }
            if (Bukkit.getWorld(args[1]) == null) {
                if (!sender.hasPermission("treeassist.toggle.other")) {
                    TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE_OTHER.parse());
                    return;
                }

                if (TreeAssist.instance.toggleGlobal(args[1])) {
                    sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_OTHER_ON.parse(args[1]));
                } else {
                    sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_OTHER_OFF.parse(args[1]));
                }
                return;
            }

            if (args.length > 2) {
                // we want to check
                if (TreeAssist.instance.isDisabled(args[1], sender.getName())) {
                    sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_OFF.parse(args[1]));
                } else {
                    sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_ON.parse(args[1]));
                }
                return;
            }

            if (TreeAssist.instance.toggleWorld(args[1], sender.getName())) {
                sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_ON.parse(args[1]));
            } else {
                sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_OFF.parse(args[1]));
            }

            return;
        }

        if (args.length > 1) {
            // we want to check
            if (TreeAssist.instance.isDisabled("global", sender.getName())) {
                sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_OFF.parse());
            } else {
                sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_ON.parse());
            }
            return;
        }

        if (TreeAssist.instance.toggleGlobal(sender.getName())) {
            sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_ON.parse());
        } else {
            sender.sendMessage(Language.MSG.SUCCESSFUL_TOGGLE_YOU_OFF.parse());
        }
    }

    @Override
    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities

            List<String> worlds = new ArrayList<>();

            for (World world : Bukkit.getServer().getWorlds()) {
                worlds.add(world.getName());
            }

            List<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
                if (players.size() >= 50) {
                    break; // I think we have enough options.
                }
            }

            Collections.sort(worlds);
            Collections.sort(players);

            results.addAll(worlds);
            results.addAll(players);

            return results;
        }

        if (args.length > 3) {
            return results; // don't go too far!
        }

        if (args.length < 3) {
            // tab complete first argument
            String typed = args[1].toLowerCase();

            List<String> worlds = new ArrayList<>();

            for (World world : Bukkit.getServer().getWorlds()) {
                if (world.getName().toLowerCase().startsWith(typed)) {
                    worlds.add(world.getName());
                }
            }

            List<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(typed)) {
                    players.add(player.getName());

                    if (players.size() >= 50) {
                        break; // I think we have enough options.
                    }
                }
            }

            Collections.sort(worlds);
            Collections.sort(players);

            results.addAll(worlds);
            results.addAll(players);

            return results;
        }
        String typed = args[2].toLowerCase();

        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getName().toLowerCase().startsWith(typed)) {
                results.add(world.getName());
            }
        }

        Collections.sort(results);

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("toggle");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tg");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist toggle [player/world] {world} - toggle plugin usage for you/others";
    }
}
