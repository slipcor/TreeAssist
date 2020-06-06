package net.slipcor.treeassist.commands;

import org.bukkit.command.CommandSender;

import java.util.*;

public final  class TabComplete {
    private TabComplete() {}

    public static List<String> getMatches(final CommandSender sender, final List<AbstractCommand> commandList, String[] args) {
        final List<String> matches = new ArrayList<>();
        if (args.length < 1 || args[0] == null || args[0].equals("")) {
            for (AbstractCommand command : commandList) {
                if (command.hasPerms(sender)) {
                    matches.addAll(command.getMain());
                    matches.addAll(command.getShort());
                }
            }

            Collections.sort(matches);
            return matches;
        }

        if (args.length == 1) {
            String typed = args[0].toLowerCase();

            for (AbstractCommand command : commandList) {
                if (command.hasPerms(sender)) {

                    for (String mainCmd : command.getMain()) {
                        if (mainCmd.toLowerCase().startsWith(typed)) {
                            matches.add(mainCmd);
                        }
                    }
                    for (String shortHand : command.getShort()) {
                        if (shortHand.toLowerCase().startsWith(typed)) {
                            matches.add(shortHand);
                        }
                    }
                }
            }

            Collections.sort(matches);
            return matches;
        }

        // more than one argument, first argument should be the general command

        String typed = args[0].toLowerCase();
        for (AbstractCommand command : commandList) {
            if (command.hasPerms(sender)) {
                for (String mainCmd : command.getMain()) {
                    if (mainCmd.toLowerCase().equals(typed)) {
                        return command.completeTab(args);
                    }
                }
                for (String shortHand : command.getShort()) {
                    if (shortHand.toLowerCase().equals(typed)) {
                        return command.completeTab(args);
                    }
                }
            }
        }

        return matches;
    }
}
