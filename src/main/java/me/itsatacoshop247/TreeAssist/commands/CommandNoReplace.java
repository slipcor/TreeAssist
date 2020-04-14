package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandNoReplace extends AbstractCommand {
    public CommandNoReplace() {
        super(new String[]{"treeassist.noreplace"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_NOREPLACE));
            return;
        }
        int seconds = Utils.plugin.getConfig().getInt("Sapling Replant.Command Time Delay (Seconds)", 30);
        Utils.plugin.listener.noReplace(sender.getName(), seconds);
        sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_NOREPLACE, String.valueOf(seconds)));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("noreplace");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!nr");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist noreplace - stop replacing saplings for some time";
    }

    @Override
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
