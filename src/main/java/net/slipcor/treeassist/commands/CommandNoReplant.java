package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandNoReplant extends CoreCommand {
    public CommandNoReplant(CorePlugin plugin) {
        super(plugin, "treeassist.noreplant", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_NOREPLANT.parse());
            return;
        }
        int seconds = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_NOREPLANT_COMMAND_TIME_COOLDOWN, 30);
        TreeAssist.instance.getBlockListener().noReplant(sender.getName(), seconds);
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_NOREPLANT.parse(String.valueOf(seconds)));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("noreplant");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!nr");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist noreplant - stop replanting saplings for some time";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
