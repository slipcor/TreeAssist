package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.Debugger;
import net.slipcor.treeassist.core.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebug extends AbstractCommand {
    public CommandDebug() {
        super(new String[]{"treeassist.debug"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_DEBUG));
            return;
        }
        Debugger.destroy();
        if (args.length < 2 || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("none")) {
            TreeAssist.instance.getConfig().set("Debug", "none");
        } else {
            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("all")) {
                TreeAssist.instance.getConfig().set("Debug", "all");
            } else {
                TreeAssist.instance.getConfig().set("Debug", args[1]);
            }
        }
        Debugger.load(TreeAssist.instance, sender);
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
