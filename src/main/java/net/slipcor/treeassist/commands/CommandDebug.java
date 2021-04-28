package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebug extends CoreCommand {
    public CommandDebug(CorePlugin plugin) {
        super(plugin, "treeassist.debug", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_DEBUG.parse());
            return;
        }
        TreeAssist.instance.destroyDebugger();
        if (args.length < 2 || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("none")) {
            TreeAssist.instance.getConfig().set("Debug", "none");
        } else {
            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("all")) {
                TreeAssist.instance.getConfig().set("Debug", "all");
            } else {
                TreeAssist.instance.getConfig().set("Debug", args[1]);
            }
        }
        TreeAssist.instance.loadDebugger("Debug", sender);
    }

    @Override
    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("on");
            results.add("off");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        if ("on".startsWith(args[1].toLowerCase())) {
            results.add("on");
        }
        if ("off".startsWith(args[1].toLowerCase())) {
            results.add("off");
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debug");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!d");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist debug - start/stop debug";
    }
}
