package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGlobal extends CoreCommand {
    public CommandGlobal(CorePlugin plugin) {
        super(plugin, "treeassist.toggle.global", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_TOGGLE_GLOBAL.parse());
            return;
        }
        if (!TreeAssist.instance.Enabled) {
            TreeAssist.instance.Enabled = true;
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_TOGGLE_GLOBAL_ON.parse());
        } else {
            TreeAssist.instance.Enabled = false;
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_TOGGLE_GLOBAL_OFF.parse());
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

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
