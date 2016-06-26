package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandRemoveCustom extends AbstractCommand {
    public CommandRemoveCustom() {
        super(new String[]{"treeassist.removecustom"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_REMOVECUSTOM));
            return;

        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Utils.removeCustomGroup(player);
            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("removecustom");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!rc");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist removecustom - remove a custom block group";
    }

    @Override
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
