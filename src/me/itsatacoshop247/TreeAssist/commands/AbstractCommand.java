package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public abstract class AbstractCommand {
    private final String[] perms;

    AbstractCommand(final String[] permissions) {
        perms = permissions.clone();
    }

    static boolean argCountValid(final CommandSender sender, final String[] args,
                                 final Integer[] validCounts) {

        for (final int i : validCounts) {
            if (i == args.length) {
                return true;
            }
        }

        sender.sendMessage(
                Language.parse(Language.MSG.ERROR_INVALID_ARGUMENT_COUNT,
                        String.valueOf(args.length),
                        Utils.joinArray(validCounts, "|")));
        return false;
    }

    public abstract void commit(CommandSender sender, String[] args);

    public abstract List<String> getMain();

    public abstract String getName();

    public abstract List<String> getShort();

    public abstract String getShortInfo();

    public abstract CommandTree<String> getSubs();

    public boolean hasPerms(final CommandSender sender) {
        if (sender.hasPermission("treeassist.commands")) {
            return true;
        }

        for (final String perm : perms) {
            if (sender.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    public void load(final List<AbstractCommand> list, final Map<String, AbstractCommand> map) {
        for (String sShort : getShort()) {
            map.put(sShort, this);
        }
        for (String sMain : getMain()) {
            map.put(sMain, this);
        }
        list.add(this);
    }
}
