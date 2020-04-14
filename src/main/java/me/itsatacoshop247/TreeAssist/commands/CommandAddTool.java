package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandAddTool extends AbstractCommand {
    public CommandAddTool() {
        super(new String[]{"treeassist.addtool"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_ADDTOOL));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Utils.addRequiredTool(player);
            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("addtool");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!at");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist addtool - add a required tool";
    }

    @Override
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
