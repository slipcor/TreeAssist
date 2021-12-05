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

public class CommandReplant extends CoreCommand {
    public CommandReplant(CorePlugin plugin) {
        super(plugin, "treeassist.replantcommand", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_NOREPLANT.parse());
            return;
        }
        int seconds = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_REPLANT_COMMAND_TIME_COOLDOWN, 30);
        TreeAssist.instance.getBlockListener().replant(sender.getName(), seconds);
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_REPLANT.parse(String.valueOf(seconds)));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("replant");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!rp");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist replant - force replanting saplings for some time";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
