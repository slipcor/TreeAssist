package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.core.Language;
import net.slipcor.treeassist.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommand {
    private final String[] perms;

    AbstractCommand(final String[] permissions) {
        perms = permissions.clone();
    }

    /**
     * Check argument count for valid numbers
     *
     * @param sender the CommandSender to reply to in case of failure
     * @param args the arguments
     * @param validCounts an array of valid values
     * @return whether we found a valid number
     */
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
                        StringUtils.joinArray(validCounts, "|")));
        return false;
    }

    /**
     * Commit the command
     *
     * @param sender the issuing CommandSender
     * @param args the arguments (raw)
     */
    public abstract void commit(CommandSender sender, String[] args);

    /**
     * @return a list of names for the command
     */
    public abstract List<String> getMain();

    /**
     * @return a list of abbreviations for the command
     */
    public abstract List<String> getShort();

    /**
     * @return info about how to use the command
     */
    public abstract String getShortInfo();

    /**
     * Check whether someone has the required permissions
     *
     * @param sender the CommandSender to check
     * @return whether the sender has the permissions
     */
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

    /**
     * Load a command and add it to the respective Collections
     *
     * @param list the global command list, we add ourselves to it
     * @param map the command map, mapping names and shorthands to ourselves
     */
    public void load(final List<AbstractCommand> list, final Map<String, AbstractCommand> map) {
        for (String sShort : getShort()) {
            map.put(sShort, this);
        }
        for (String sMain : getMain()) {
            map.put(sMain, this);
        }
        list.add(this);
    }

    /**
     * Look for tab completion results
     *
     * @param args the currently written arguments
     * @return matches to offer for tab-complete
     */
    public List<String> completeTab(String[] args) {
        return new ArrayList<>(); // we have no arguments
    }
}
