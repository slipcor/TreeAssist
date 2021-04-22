package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.Language;
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
            TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.ERROR_PERMISSION_TOGGLE_GLOBAL));
            return;
        }
        if (!TreeAssist.instance.Enabled) {
            TreeAssist.instance.Enabled = true;
            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_GLOBAL_ON));
        } else {
            TreeAssist.instance.Enabled = false;
            sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_TOGGLE_GLOBAL_OFF));
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("global");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!g");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist global - toggle global plugin availability";
    }
}
