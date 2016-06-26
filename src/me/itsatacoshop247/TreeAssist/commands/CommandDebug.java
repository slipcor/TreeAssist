package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;

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
        if (args.length < 2 || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("none")) {
            Utils.plugin.getConfig().set("Debug", "none");
            Debugger.load(Utils.plugin, sender);
        } else {
            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("all")) {
                Utils.plugin.getConfig().set("Debug", "all");
            } else {
                Utils.plugin.getConfig().set("Debug", args[1]);
            }
            Debugger.load(Utils.plugin, sender);
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debug");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!d");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist debug - start/stop debug";
    }

    @Override
    public CommandTree<String> getSubs() {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"all"});
        result.define(new String[]{"none"});

        result.define(new String[]{"on"});
        result.define(new String[]{"off"});

        result.define(new String[]{"true"});
        result.define(new String[]{"false"});
        return result;
    }
}
