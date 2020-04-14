package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandReload extends AbstractCommand {
    public CommandReload() {
        super(new String[]{"treeassist.reload"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_RELOAD));
            return;
        }
        Utils.plugin.blockList.save(true);
        Utils.plugin.reloadConfig();
        Utils.plugin.loadYamls();
        Utils.plugin.reloadLists();
        sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_RELOAD));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("reload");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!rl");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist reload - reload the plugin";
    }

    @Override
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
