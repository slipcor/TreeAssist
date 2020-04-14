package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandGlobal extends AbstractCommand {
    public CommandGlobal() {
        super(new String[]{"treeassist.toggle.global"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE_GLOBAL));
            return;
        }
        if (!Utils.plugin.Enabled) {
            Utils.plugin.Enabled = true;
            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_GLOBAL_ON));
        } else {
            Utils.plugin.Enabled = false;
            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_GLOBAL_OFF));
        }
        return;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("global");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!g");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist global - toggle global plugin availability";
    }

    @Override
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }

}
