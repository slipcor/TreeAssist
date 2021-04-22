package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.MainConfig;
import net.slipcor.treeassist.core.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandNoReplant extends AbstractCommand {
    public CommandNoReplant() {
        super(new String[]{"treeassist.noreplant"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.ERROR_PERMISSION_NOREPLANT));
            return;
        }
        int seconds = TreeAssist.instance.getMainConfig().getInt(MainConfig.CFG.COMMANDS_NOREPLANT_COMMAND_TIME_COOLDOWN, 30);
        TreeAssist.instance.getBlockListener().noReplant(sender.getName(), seconds);
        TreeAssist.instance.sendPrefixed(sender, Language.parse(Language.MSG.SUCCESSFUL_NOREPLANT, String.valueOf(seconds)));
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
}
