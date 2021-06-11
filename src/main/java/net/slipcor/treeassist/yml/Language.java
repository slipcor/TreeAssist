package net.slipcor.treeassist.yml;

import net.slipcor.core.CoreLanguage;
import net.slipcor.core.CorePlugin;
import net.slipcor.core.LanguageEntry;
import net.slipcor.treeassist.TreeAssist;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class Language extends CoreLanguage {
    public Language(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected LanguageEntry[] getAllNodes() {
        return MSG.values();
    }

    public enum MSG implements LanguageEntry {
        ERROR_ADDTOOL_ALREADY("error.addtool.already", "&cYou have already added this as required tool!"),
        ERROR_ADDTOOL_OTHER("error.addtool.other", "&cSomething went wrong trying to add the required tool: %1%"),
        ERROR_CUSTOM_LISTS("error.custom.lists", "&cSomething is wrong with your custom lists. Please fix them! They need have to same item count!"),
        ERROR_CUSTOM_EXISTS("error.custom.exists", "&cThis custom block group definition already exists!"),
        ERROR_CUSTOM_NOT_FOUND("error.custom.notfound", "&cThis custom block group definition does not exists"),
        ERROR_CUSTOM_EXPLANATION("error.custom.explanation", "&cYou need to have three items in you first hotbar slots. &e1) SAPLING - 2) LOG - 3) LEAVES"),
        ERROR_DATA_YML("error.data_yml", "&cYou have a messed up data.yml - fix or remove it!"),
        ERROR_EMPTY_HAND("error.emptyhand", "&cYou don't have an item in your hand"),
        ERROR_FINDFOREST("error.findforest", "&cForest not found in 500 block radius: &r%1%"),
        ERROR_GROW("error.grow", "&cCould not grow a tree here. Is there enough space?"),
        ERROR_INVALID_ARGUMENT_COUNT("error.invalid_argumentcount", "&cInvalid number of arguments&r (%1% instead of %2%)!"),
        ERROR_INVALID_ARGUMENT_LIST("error.invalid_argumentlist", "&cInvalid arguments! Valid:&r %1%"),
        ERROR_INVALID_TREETYPE("error.invalid_treetype", "&cInvalid TreeType! Valid:&r %1%"),
        ERROR_NOT_TOOL("error.nottool", "&cYou don't have the required tool to do that!"),
        ERROR_OUT_OF_RANGE("error.outofrange", "&cThe max range for this command is: %1%"),
        ERROR_PERMISSION_ADDTOOL("error.permission.addtool", "&cYou don't have 'treeassist.addtool'"),
        ERROR_PERMISSION_DEBUG("error.permission.debug", "&cYou don't have 'treeassist.debug'"),
        ERROR_PERMISSION_FINDFOREST("error.permission.findforest", "&cYou don't have 'treeassist.findforest'"),
        ERROR_PERMISSION_FORCEBREAK("error.permission.forcebreak", "&cYou don't have 'treeassist.forcebreak'"),
        ERROR_PERMISSION_FORCEGROW("error.permission.forcegrow", "&cYou don't have 'treeassist.forcegrow'"),
        ERROR_PERMISSION_NOREPLANT("error.permission.noreplant", "&cYou don't have 'treeassist.noreplant'"),
        ERROR_PERMISSION_PURGE("error.permission.purge", "&cYou don't have 'treeassist.purge'"),
        ERROR_PERMISSION_RELOAD("error.permission.reload", "&cYou don't have 'treeassist.reload'"),
        ERROR_PERMISSION_REMOVETOOL("error.permission.removetool", "&cYou don't have 'treeassist.removetool'"),
        ERROR_PERMISSION_TOGGLE("error.permission.toggle", "&cYou don't have 'treeassist.toggle'"),
        ERROR_PERMISSION_TOGGLE_OTHER("error.permission.toggle_other", "&cYou don't have 'treeassist.toggle.other'"),
        ERROR_PERMISSION_TOGGLE_GLOBAL("error.permission.toggle_global", "&cYou don't have 'treeassist.toggle.global'"),
        ERROR_PERMISSION_TOGGLE_TOOL("error.permission.toggle_tool", "&cYou don't have 'treeassist.tool'"),
        ERROR_PERMISSION_TOGGLE_GROWTOOL("error.permission.toggle_growtool", "&cYou don't have 'treeassist.growtool'"),
        ERROR_REMOVETOOL_NOTDONE("error.removetool.not_done", "&cTool is no required tool!"),

        ERROR_NOTFOUND_WORLD("error.notfound.world", "&cWorld not found: %1%'"),

        ERROR_ONLY_PLAYERS("error.only.players", "Only for players!"),
        ERROR_ONLY_TREEASSIST_BLOCKLIST("error.only.treeassist_blocklist", "&cThis command only is available for the TreeAssist BlockList!"),

        INFO_COOLDOWN_DONE("info.cooldown.done", "&aCooldown reset!"),
        INFO_COOLDOWN_STILL("info.cooldown.still", "&aYou are still cooling down!"),
        INFO_COOLDOWN_VALUE("info.cooldown.value", "&a%1% seconds remaining!"),
        INFO_COOLDOWN_WAIT("info.cooldown.wait", "&aWait for the %1% second cooldown!"),

        INFO_CUSTOM_ADDED("info.custom.added", "&aCustom block group definition added!"),
        INFO_CUSTOM_REMOVED("info.custom.removed", "&aCustom block group definition removed!"),

        INFO_NEVER_BREAK_SAPLINGS("info.never_break_saplings", "&aYou cannot break saplings on this server!"),
        INFO_SAPLING_PROTECTED("info.sapling_protected", "&aThis sapling is protected!"),
        INFO_PLUGIN_PREFIX("info.plugin_prefix", "&8[&2TreeAssist&8]&r "),

        WARNING_ADDTOOL_ONLYONE("warning.sapling_protected", "&6You can only use one enchantment. Using: %1%"),

        SUCCESSFUL_ADDTOOL("successful.addtool", "&aRequired tool added: %1%"),
        SUCCESSFUL_DEBUG_ALL("successful.debug_all", "debugging EVERYTHING"),
        SUCCESSFUL_DEBUG_X("successful.debug", "debugging %1%"),

        SUCCESSFUL_FINDFOREST("successful.findforest", "&aForest found at &r%1%"),

        SUCCESSFUL_NOREPLANT("successful.noreplant", "&aYou now stop replanting trees for %1% seconds."),

        SUCCESSFUL_PROTECT_OFF("successful.protect_off", "&aSapling is no longer protected!"),
        SUCCESSFUL_PROTECT_ON("successful.protect_on", "&aSapling now is protected!"),

        SUCCESSFUL_PURGE_DAYS("successful.purge.days", "&a%1% entries have been purged for the last %2% days!"),
        SUCCESSFUL_PURGE_GLOBAL("successful.purge.global", "&a%1% global entries have been purged!"),
        SUCCESSFUL_PURGE_WORLD("successful.purge.world", "&a%1% entries have been purged for the world %2%!"),

        SUCCESSFUL_RELOAD("successful.reload", "&aTreeAssist has been reloaded."),

        SUCCESSFUL_REMOVETOOL("successful.removetool", "&aRequired tool removed: %1%"),

        SUCCESSFUL_TOGGLE_GLOBAL_OFF("successful.toggle.global_off", "&aTreeAssist functions are turned off globally!"),
        SUCCESSFUL_TOGGLE_GLOBAL_ON("successful.toggle.global_on", "&aTreeAssist functions are now turned on globally!"),

        SUCCESSFUL_TOGGLE_OTHER_OFF("successful.toggle.other_global_off", "&aTreeAssist functions are turned off for %1%!"),
        SUCCESSFUL_TOGGLE_OTHER_ON("successful.toggle.other_global_on", "&aTreeAssist functions are now turned on for %1%!"),
        SUCCESSFUL_TOGGLE_OTHER_WORLD_OFF("successful.toggle.other_world_off", "&aTreeAssist functions are turned off for %1% in world %2%!"),
        SUCCESSFUL_TOGGLE_OTHER_WORLD_ON("successful.toggle.other_world_on", "&aTreeAssist functions are now turned on for %1% in world %2%!"),

        SUCCESSFUL_TOGGLE_YOU_OFF("successful.toggle.you_global_off", "&aTreeAssist functions are turned off for you!"),
        SUCCESSFUL_TOGGLE_YOU_ON("successful.toggle.you_global_on", "&aTreeAssist functions are now turned on for you!"),
        SUCCESSFUL_TOGGLE_YOU_WORLD_OFF("successful.toggle.you_world_off", "&aTreeAssist functions are turned off for you in world %1%!"),
        SUCCESSFUL_TOGGLE_YOU_WORLD_ON("successful.toggle.you_world_on", "&aTreeAssist functions are now turned on for you in world %1%!"),

        SUCCESSFUL_GROWTOOL_OFF("successful.grow_tool_off", "&aGrow Tool removed!"),
        SUCCESSFUL_GROWTOOL_ON("successful.grow_tool_on", "&aYou have been given the Grow Tool! It will try to grow a tree of type &e%1%&a!"),

        SUCCESSFUL_TOOL_OFF("successful.tool_off", "&aProtection Tool removed!"),
        SUCCESSFUL_TOOL_ON("successful.tool_on", "&aYou have been given the Protection Tool!");


        private final String node;
        private String value;

        MSG(final String node, final String value) {
            this.node = node;
            this.value = value;
        }

        /**
         * read a node from the config and return its value after replacing
         *
         * @param args    strings to replace
         * @return the replaced node string
         */
        public String parse(final String... args) {
            String result = toString();
            int i = 0;
            for (final String word : args) {
                result = result.replace("%" + ++i + '%', word);
            }
            return ChatColor.translateAlternateColorCodes('&', result);
        }

        /**
         * read a node from the config and return its value
         *
         * @return the node string
         */
        public String parse() {
            return ChatColor.translateAlternateColorCodes('&', toString());
        }

        public String getNode() {
            return node;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(final String sValue) {
            value = sValue;
        }

        @Override
        public String toString() {
            return value;
        }
    }


    /**
     * Create a language manager instance
     */
    public static void init(final TreeAssist instance, final String langString) {
        instance.getDataFolder().mkdir();
        final File configFile = new File(instance.getDataFolder().getPath()
                + "/" + langString + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (final Exception e) {
                instance.getLogger().severe(
                        "Error when creating language file.");
            }
        }
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        for (final MSG m : MSG.values()) {
            config.addDefault(m.getNode(), m.toString());
        }

        config.options().copyDefaults(true);
        try {
            config.save(configFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        for (final MSG m : MSG.values()) {
            m.setValue(config.getString(m.getNode()));
        }
    }

}
