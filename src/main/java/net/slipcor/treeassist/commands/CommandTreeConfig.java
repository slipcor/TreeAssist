package net.slipcor.treeassist.commands;

import net.slipcor.core.ConfigEntry;
import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTreeConfig extends CoreCommand {
    final List<TreeConfig.CFG> accessibleLists = new ArrayList<>();

    public CommandTreeConfig(CorePlugin plugin) {
        super(plugin, "treeassist.treeconfig", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);

        accessibleLists.add(TreeConfig.CFG.GROUND_BLOCKS);
        accessibleLists.add(TreeConfig.CFG.NATURAL_BLOCKS);
        accessibleLists.add(TreeConfig.CFG.TOOL_LIST);
        accessibleLists.add(TreeConfig.CFG.COMMANDS_PER_BLOCK);
        accessibleLists.add(TreeConfig.CFG.COMMANDS_PER_TREE);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_CONFIG.parse());
            return;
        }

        if (args.length >= 3 && args[1].toLowerCase().equals("set")) {
            if (!argCountValid(sender, args, new Integer[]{5})) {
                return;
            }

            //           0         1      2      3      4
            // /ta       config    set    [type] [node] [value]
            set(sender, args[2], args[3], args[4]);
            return;
        }

        if (args.length >= 3 && args[1].toLowerCase().equals("get")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2       3
            // /ta       config    get    [type]  [node]
            get(sender, args[2], args[3]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("info")) {
            if (!argCountValid(sender, args, new Integer[]{3, 4})) {
                return;
            }

            //           0         1      2       3
            // /ta       config    info   [type]  [node]
            if (args.length >= 3) {
                info(sender, args[2]);
            } else {
                info(sender, args[3]);
            }
            return;
        }

        if (args.length >= 3 && args[1].toLowerCase().equals("add")) {
            if (!argCountValid(sender, args, new Integer[]{5})) {
                return;
            }

            //           0         1      2      3      4
            // /ta       config    add    [type] [node] [value]
            add(sender, args[2], args[3], args[4]);
            return;
        }

        if (args.length >= 3 && args[1].toLowerCase().equals("remove")) {
            if (!argCountValid(sender, args, new Integer[]{5})) {
                return;
            }

            //           0         2      3      4
            // /ta       config    [type] [node] [value]
            remove(sender, args[2], args[3], args[4]);
            return;
        }

        TreeAssist.instance.sendPrefixed(sender, getShortInfo());
    }

    private TreeConfig.CFG getFullNode(String part) {

        boolean foundEntry = false;
        TreeConfig.CFG completedEntry = null;

        for (TreeConfig.CFG entry : TreeConfig.CFG.values()) {
            if (entry.getNode().replaceAll("\\s+", "").toLowerCase().contains(part.toLowerCase()) && entry.getNode().length() != part.length()) {
                if (foundEntry) {
                    // found a second match, let us not autocomplete this!
                    foundEntry = false;
                    completedEntry = null;
                    break;
                }
                foundEntry = true;
                completedEntry = entry;
            }
        }

        return completedEntry;
    }

    private void add(final CommandSender sender, final String configName, final String node, final String value) {

        TreeConfig.CFG completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            add(sender, configName, completedEntry.getNode(), value);
            return;
        }

        final TreeConfig.CFG entry = TreeConfig.CFG.getByNode(node);

        TreeAssist treeAssist = TreeAssist.instance;

        if (entry == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN_NODE.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        TreeConfig config = TreeAssist.treeConfigs.get(configName);
        if (config == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_TREECONFIG.parse(configName));
            return;
        }

        if (entryType == ConfigEntry.Type.COMMENT) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_GROUP_IMPOSSIBLE.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.LIST) {
            List<String> newList = new ArrayList<>(config.getStringList(entry, new ArrayList<String>()));
            if (newList.contains(value)) {
                treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_ADD_SKIPPED.parse(node, value));
                return;
            }
            newList.add(value);
            config.setValue(entry, newList);
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_ADD_SUCCESS.parse(node, value));
        } else {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_LIST.parse(node));
            return;
        }
        config.save();
        TreeStructure.reloadTreeDefinitions();
    }

    private void get(final CommandSender sender, final String configName, final String node) {

        TreeConfig.CFG completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            get(sender, configName, completedEntry.getNode());
            return;
        }

        final TreeConfig.CFG entry = TreeConfig.CFG.getByNode(node);

        TreeAssist treeAssist = TreeAssist.instance;

        if (entry == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN_NODE.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        TreeConfig config = TreeAssist.treeConfigs.get(configName);
        if (config == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_TREECONFIG.parse(configName));
            return;
        }

        if (entryType == ConfigEntry.Type.COMMENT) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_GET_GROUP_IMPOSSIBLE.parse(node));
        } else if (entryType == ConfigEntry.Type.LIST) {
            StringBuffer value = new StringBuffer();
            List<String> list = config.getStringList(entry, new ArrayList<String>());
            for (String item : list) {
                value.append("\n");
                value.append(item);
            }
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_GET_SUCCESS.parse(node, value.toString()));
        } else if (entryType == ConfigEntry.Type.BOOLEAN) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_GET_SUCCESS.parse(node, String.valueOf(config.getBoolean(entry))));
        } else if (entryType == ConfigEntry.Type.STRING) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_GET_SUCCESS.parse(node, config.getString(entry)));
        } else if (entryType == ConfigEntry.Type.INT) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_GET_SUCCESS.parse(node, String.valueOf(config.getInt(entry))));
        } else if (entryType == ConfigEntry.Type.DOUBLE) {
            treeAssist.sendPrefixed(sender,
                    Language.MSG.ERROR_CONFIG_GET_SUCCESS.parse(node, String.format("%.2f", config.getDouble(entry))));
        } else {
            treeAssist.sendPrefixed(sender,
                    Language.MSG.ERROR_CONFIG_UNKNOWN_TYPE.parse(entryType.name()));
        }
    }

    private void info(final CommandSender sender, final String node) {

        TreeConfig.CFG completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            info(sender, completedEntry.getNode());
            return;
        }

        final TreeConfig.CFG entry = TreeConfig.CFG.getByNode(node);

        TreeAssist treeAssist = TreeAssist.instance;

        if (entry == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN_NODE.parse(node));
            return;
        }

        if (entry.getComment() == null || entry.getComment().isEmpty()) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_INFO_EMPTY.parse(node));
        } else {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_INFO_SUCCESS.parse(node, "\n" + entry.getComment()));
        }
    }

    private void remove(final CommandSender sender, final String configName, final String node, final String value) {

        TreeConfig.CFG completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            remove(sender, configName, completedEntry.getNode(), value);
            return;
        }

        final TreeConfig.CFG entry = TreeConfig.CFG.getByNode(node);

        TreeAssist treeAssist = TreeAssist.instance;

        if (entry == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN_NODE.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        TreeConfig config = TreeAssist.treeConfigs.get(configName);
        if (config == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_TREECONFIG.parse(configName));
            return;
        }

        if (entryType == ConfigEntry.Type.COMMENT) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_GROUP_IMPOSSIBLE.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.LIST) {
            List<String> newList = new ArrayList<>(config.getStringList(entry, new ArrayList<String>()));
            if (!newList.contains(value)) {
                treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_REMOVE_SKIPPED.parse(node, value));
                return;
            }
            newList.remove(value);
            config.setValue(entry, newList);
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_REMOVE_SUCCESS.parse(node, value));
        } else {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_LIST.parse(node));
            return;
        }
        config.save();
        TreeStructure.reloadTreeDefinitions();
    }

    private void set(final CommandSender sender, final String configName, final String node, final String value) {

        TreeConfig.CFG completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            set(sender, configName, completedEntry.getNode(), value);
            return;
        }

        final TreeConfig.CFG entry = TreeConfig.CFG.getByNode(node);

        TreeAssist treeAssist = TreeAssist.instance;

        if (entry == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN_NODE.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        TreeConfig config = TreeAssist.treeConfigs.get(configName);
        if (config == null) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_TREECONFIG.parse(configName));
            return;
        }

        if (entryType == ConfigEntry.Type.COMMENT) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_GROUP_IMPOSSIBLE.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.LIST) {
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_LIST_IMPOSSIBLE.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.BOOLEAN) {
            if ("true".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.TRUE);
                treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_SUCCESS.parse(node, "true"));
            } else if ("false".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.FALSE);
                treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_SUCCESS.parse(node, "false"));
            } else {
                treeAssist.sendPrefixed(sender,
                        Language.MSG.ERROR_INVALID_ARGUMENT_TYPE.parse(value, "boolean (true|false)"));
                return;
            }
        } else if (entryType == ConfigEntry.Type.STRING) {
            config.setValue(entry, String.valueOf(value));
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_SUCCESS.parse(node, value));
        } else if (entryType == ConfigEntry.Type.INT) {
            final int iValue;

            try {
                iValue = Integer.parseInt(value);
            } catch (final Exception e) {
                treeAssist.sendPrefixed(sender, Language.MSG.ERROR_INVALID_NUMBER.parse(value));
                return;
            }
            config.setValue(entry, iValue);
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_SUCCESS.parse(node, String.valueOf(iValue)));
        } else if (entryType == ConfigEntry.Type.DOUBLE) {
            final double dValue;

            try {
                dValue = Double.parseDouble(value);
            } catch (final Exception e) {
                treeAssist.sendPrefixed(sender,
                        Language.MSG.ERROR_INVALID_ARGUMENT_TYPE.parse(value, "double (e.g. 12.00)"));
                return;
            }
            config.setValue(entry, dValue);
            treeAssist.sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_SUCCESS.parse(node,
                            String.valueOf(dValue)));
        } else {
            treeAssist.sendPrefixed(sender,
                    Language.MSG.ERROR_CONFIG_UNKNOWN_TYPE.parse(entryType.name()));
            return;
        }
        config.save();
        TreeStructure.reloadTreeDefinitions();
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();
        //               0         1      2      3      4
        // /pvpstats treeconfig    add    [type] [node] [value]

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("get");
            results.add("set");
            results.add("add");
            results.add("info");
            results.add("remove");
        } else if (args.length == 2) {
            // second argument!
            addIfMatches(results, "get", args[1]);
            addIfMatches(results, "set", args[1]);
            addIfMatches(results, "add", args[1]);
            addIfMatches(results, "info", args[1]);
            addIfMatches(results, "remove", args[1]);
        } else if (args.length == 3 && !args[1].equals("info")) {

            for (String configName : TreeAssist.treeConfigs.keySet()) {
                addIfMatches(results, configName, args[2]);
            }
        } else {
            // args is >= 4

            if (
                    !args[1].equalsIgnoreCase("get") &&
                            !args[1].equalsIgnoreCase("set") &&
                            !args[1].equalsIgnoreCase("add") &&
                            !args[1].equalsIgnoreCase("info") &&
                            !args[1].equalsIgnoreCase("remove")
            ) {
                return results;
            }

            int checkPos = args[1].equals("info") ? 2 : 3;

            if (args[checkPos].equals("")) {
                // list actual argument possibilities
                for (TreeConfig.CFG entry : TreeConfig.CFG.values()) {

                    if (args[1].equalsIgnoreCase("info")) {
                        if (entry.getComment() == null || entry.getComment().isEmpty()) {
                            continue;
                        }
                    } else if (args[1].equalsIgnoreCase("get")) {
                        if (entry.getType() == ConfigEntry.Type.COMMENT) {
                            continue;
                        }
                    } else if (args[1].equalsIgnoreCase("set")) {
                        if (entry.getType() == ConfigEntry.Type.COMMENT || entry.getType() == ConfigEntry.Type.LIST) {
                            continue;
                        }
                    } else {
                        if (entry.getType() == ConfigEntry.Type.COMMENT || !accessibleLists.contains(entry)) {
                            continue;
                        }
                    }
                    results.add(entry.getNode().replaceAll("\\s+", ""));
                }
                return results;
            }

            if (args.length > checkPos+1) {
                return results; // don't go too far!
            }

            for (TreeConfig.CFG entry : TreeConfig.CFG.values()) {
                if (args[1].equalsIgnoreCase("info")) {
                    if (entry.getComment() == null || entry.getComment().isEmpty()) {
                        continue;
                    }
                } else if (args[1].equalsIgnoreCase("get")) {
                    if (entry.getType() == ConfigEntry.Type.COMMENT) {
                        continue;
                    }
                } else if (args[1].equalsIgnoreCase("set")) {
                    if (entry.getType() == ConfigEntry.Type.COMMENT || entry.getType() == ConfigEntry.Type.LIST) {
                        continue;
                    }
                } else {
                    if (entry.getType() == ConfigEntry.Type.COMMENT || !accessibleLists.contains(entry)) {
                        continue;
                    }
                }

                addIfMatches(results, entry.getNode().replaceAll("\\s+", ""), args[checkPos]);
            }
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("treeconfig");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tc");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist treeconfig get [type] [node] - get a config value\n" +
               "/treeassist treeconfig info [node] - get information about a config node\n" +
               "/treeassist treeconfig set [type] [node] [value] - set a config value\n" +
               "/treeassist treeconfig add [type] [node] [value] - add a value to a config list\n" +
               "/treeassist treeconfig remove [type] [node] [value] - remove a value from a config list";
    }
}
