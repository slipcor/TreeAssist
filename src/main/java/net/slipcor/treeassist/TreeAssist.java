package net.slipcor.treeassist;

import net.slipcor.core.*;
import net.slipcor.treeassist.blocklists.*;
import net.slipcor.treeassist.commands.*;
import net.slipcor.treeassist.discovery.TreeBlock;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.externals.WorldGuardListener;
import net.slipcor.treeassist.listeners.TreeAssistBlockListener;
import net.slipcor.treeassist.listeners.TreeAssistPlayerListener;
import net.slipcor.treeassist.listeners.TreeAssistSpawnListener;
import net.slipcor.treeassist.metrics.MetricsLite;
import net.slipcor.treeassist.metrics.MetricsMain;
import net.slipcor.treeassist.runnables.CleanRunner;
import net.slipcor.treeassist.runnables.CoolDownCounter;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.CommandUtils;
import net.slipcor.treeassist.utils.StringUtils;
import net.slipcor.treeassist.utils.ToolUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class TreeAssist extends CorePlugin {
    public static Map<String, TreeConfig> treeConfigs = new LinkedHashMap<>();        // The TreeConfigs which define what constitutes a tree
    public static TreeAssist instance;                                          // Static plugin instance

    public List<Location> saplingLocationList = new ArrayList<>();              // List of protected saplings
    private final Map<String, List<String>> disabledMap = new HashMap<>();      // List of player names who disabled TreeAssist
    final Map<String, CoreCommand> commandMap = new HashMap<>();            // Map of commands
    final List<CoreCommand> commandList = new ArrayList<>();                // List of commands
    private final Map<String, CoolDownCounter> coolDowns = new HashMap<>();     // Maps of player names to active CoolDown Runnables
    private final Set<String> coolDownOverrides = new HashSet<>();              // List of player names who override cooldowns

    private final Set<TreeStructure> validTrees = new HashSet<>();

    public boolean Enabled = true;  // Whether the plugin as a whole is enabled
    public boolean mcMMO = false;   // Whether mcMMO has been found and hooked into
    public boolean jobs = false;    // Whether Jobs has been found and hooked into
    public boolean makeEvents = false;

    private File configFile;
    private MainConfig config;

    public BlockList blockList;                     // Placed Block List instance

    private WorldGuardListener worldGuard = null;   // WorldGuard Listener instance
    private TreeAssistBlockListener listener;       // Block Listener instance
    private TreeAssistPlayerListener playerListener; // Player Listener instance
    private TreeAssistSpawnListener spawnListener;  // Item Spawn Listener instance

    private CoreUpdater updater = null;
    private CoreTabCompleter completer;
    private CoreLanguage language;

    /**
     * Check for mcMMO if requested
     */
    private void checkMcMMO() {
        if (config.getBoolean(MainConfig.CFG.PLUGINS_USE_MCMMO) || config.getBoolean(MainConfig.CFG.PLUGINS_USE_TREEMCMMO)) {
            try {
                if (getServer().getPluginManager().isPluginEnabled("mcMMO")) {
                    Class.forName("com.gmail.nossr50.datatypes.skills.PrimarySkillType");
                    this.mcMMO = true;
                }
            } catch (ClassNotFoundException e) {
                instance.getLogger().warning("mcMMO classic is not supported, please update to mcMMO 2.X!");
                this.mcMMO = false;
            }
        } else {
            this.mcMMO = false;
        }
    }

    /**
     * Check for Jobs if requested
     */
    private void checkJobs() {
        if (config.getBoolean(MainConfig.CFG.PLUGINS_USE_JOBS) || config.getBoolean(MainConfig.CFG.PLUGINS_USE_TREEJOBS)) {
            this.jobs = getServer().getPluginManager().isPluginEnabled("Jobs");
        } else {
            this.jobs = false;
        }
    }

    /**
     * Check for WorldGuard if requested
     */
    private void checkWorldGuard() {
        if (config.getBoolean(MainConfig.CFG.PLUGINS_USE_WORLDGUARD) && getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuard = new WorldGuardListener();
        }
    }

    /**
     * If running for the first time, save the default config
     */
    private void firstRun() {
        if (!this.configFile.exists()) {
            this.configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
    }

    /**
     * @return the BlockListener instance
     */
    public TreeAssistBlockListener getBlockListener() {
        return listener;
    }

    /**
     * @return the PlayerListener instance
     */
    public TreeAssistPlayerListener getPlayerListener() {
        return playerListener;
    }

    /**
     * Get the CoolDown seconds of a player
     *
     * @param player the player to look for
     * @return the seconds they have to wait
     */
    public int getCoolDown(Player player) {
        if (hasCoolDown(player)) {
            return coolDowns.get(player.getName()).getSeconds();
        }
        return 0;
    }

    /**
     * @return the MainConfig instance
     */
    public MainConfig config() {
        return config;
    }

    public CoreUpdater getUpdater() {
        return updater;
    }

    /**
     * Check whether a player has a CoolDown
     *
     * @param player the player to look for
     * @return whether they do have a CoolDown
     */
    public boolean hasCoolDown(Player player) {
        if (player.hasPermission("treeassist.bypass.cooldown")) {
            TreeStructure.debug.i("cooldown bypass permission!");
            return false;
        }
        return !coolDownOverrides.contains(player.getName()) && coolDowns.containsKey(player.getName());
    }

    /**
     * Check whether the plugin is active in a world
     *
     * @param world the world to check
     * @return whether the plugin is active in that world
     */
    public boolean isActive(World world) {
        return (!config.getBoolean(MainConfig.CFG.WORLDS_RESTRICT)) ||
                config.getStringList(MainConfig.CFG.WORLDS_ENABLED_WORLDS, new ArrayList<>()).contains(
                        world.getName());
    }

    /**
     * Check whether the plugin is disabled for a player in a specific world
     *
     * @param world the world to check
     * @param player the player to check
     * @return whether the player disabled the plugin in this world
     */
    public boolean isDisabled(String world, String player) {
        if (disabledMap.containsKey("global")) {
            if (disabledMap.get("global").contains(player)) {
                return true;
            }
        }
        if (disabledMap.containsKey(world)) {
            return disabledMap.get(world).contains(player);
        }
        return false;
    }

    /**
     * Load all commands
     */
    public void loadCommands() {
        commandList.clear();
        commandMap.clear();
        completer = null;
        new CommandAddTool(this).load(commandList, commandMap);
        new CommandConfig(this).load(commandList, commandMap);
        new CommandDebug(this).load(commandList, commandMap);
        new CommandFindForest(this).load(commandList, commandMap);
        new CommandForceBreak(this).load(commandList, commandMap);
        new CommandForceGrow(this).load(commandList, commandMap);
        new CommandGlobal(this).load(commandList, commandMap);
        new CommandNoReplant(this).load(commandList, commandMap);
        new CommandPurge(this).load(commandList, commandMap);
        new CommandReload(this).load(commandList, commandMap);
        new CommandRemoveTool(this).load(commandList, commandMap);
        new CommandToggle(this).load(commandList, commandMap);
        new CommandTool(this).load(commandList, commandMap);
        new CommandTreeConfig(this).load(commandList, commandMap);
        new CommandGrowTool(this).load(commandList, commandMap);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final CoreCommand acc = (args.length > 0) ? commandMap.get(args[0].toLowerCase()) : null;

        String[] newArgs = StringUtils.compress(args);

        if (newArgs == null || newArgs.length == 0) {
            return false;
        }

        if (acc != null) {
            acc.commit(sender, newArgs);
            return true;
        }

        for (CoreCommand cc : commandMap.values()) {
            if (cc.getShort().contains(newArgs[0].toLowerCase())) {
                cc.commit(sender, newArgs);
                return true;
            }
        }
        boolean found = false;
        for (CoreCommand command : commandList) {
            if (command.hasPerms(sender)) {
                sendPrefixed(sender, ChatColor.YELLOW + command.getShortInfo());
                found = true;
            }
        }
        return found;
    }

    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (this.blockList instanceof FlatFileBlockList) {
            blockList.save(true);
        }
        destroyDebugger();
    }

    public void onEnable() {

        checkMcMMO();
        checkJobs();
        this.makeEvents = config().getBoolean(MainConfig.CFG.PLUGINS_USE_CUSTOM_EVENTS);

        if (worldGuard != null) {
            getServer().getPluginManager().registerEvents(worldGuard, this);
        }

        getServer().getPluginManager().registerEvents(listener, this);

        this.playerListener = new TreeAssistPlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        this.spawnListener = new TreeAssistSpawnListener(this);
        getServer().getPluginManager().registerEvents(spawnListener, this);

        if (config.getBoolean(MainConfig.CFG.BSTATS_ACTIVE)) {
            if (config.getBoolean(MainConfig.CFG.BSTATS_FULL)) {
                MetricsMain fullMetrics = new MetricsMain(this);
            } else {
                MetricsLite liteMetrics = new MetricsLite(this);
            }
        }

        TreeStructure.debug = new CoreDebugger(this, 1);
        CleanRunner.debug = new CoreDebugger(this, 2);
        TreeAssistBlockListener.debug = new CoreDebugger(this, 6);
        TreeAssistSpawnListener.debug = new CoreDebugger(this, 7);
        BlockUtils.debug = new CoreDebugger(this, 8);
        TreeAssistPlayerListener.debug = new CoreDebugger(this, 9);
        CommandUtils.debug = new CoreDebugger(this, 10);
        loadDebugger("Debug", Bukkit.getConsoleSender());

        if (config.getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
            String pluginName = config.getString(
                    MainConfig.CFG.PLACED_BLOCKS_PLUGIN_NAME, "TreeAssist");
            if ("TreeAssist".equalsIgnoreCase(pluginName)) {
                blockList = new FlatFileBlockList();
            } else if ("Prism".equalsIgnoreCase(pluginName)) {
                blockList = new Prism2BlockList();
            } else if ("LogBlock".equalsIgnoreCase(pluginName)) {
                blockList = new LogBlockBlockList();
            } else if ("CoreProtect".equalsIgnoreCase(pluginName)) {
                blockList = new CoreProtectBlockList();
            } else {
                blockList = new EmptyBlockList();
            }
        } else {
            blockList = new EmptyBlockList();
        }
        blockList.initiate();

        loadCommands();
        if (loadLanguage() != null) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        this.updater = new CoreUpdater(this, getFile(), "treeassist",
                "https://www.spigotmc.org/resources/treeassist.67436/", MainConfig.CFG.UPDATE_MODE, MainConfig.CFG.UPDATE_TYPE);
    }

    public String loadLanguage() {
        return language.load("lang_en");
    }

    @Override
    public void onLoad() {
        instance = this;
        language = new Language(this);
        ConfigurationSerialization.registerClass(TreeBlock.class);

        this.configFile = new File(getDataFolder(), "config.yml");
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.config = new MainConfig(this, configFile);
        this.config.load();
        reloadLists();

        this.listener = new TreeAssistBlockListener(this);

        checkWorldGuard();
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        if (completer == null) {
            completer = new CoreTabCompleter(true);
        }
        return completer.getMatches(sender, commandList, args);
    }

    /**
     * Reload all tree definitions
     */
    public void reloadLists() {
        if (!new File(getDataFolder().getPath(), "trees").exists()) {
            saveResource("trees/default.yml", false);
            saveResource("trees/overworld/acacia.yml", false);
            saveResource("trees/overworld/birch.yml", false);
            saveResource("trees/overworld/dark_oak.yml", false);
            saveResource("trees/overworld/jungle.yml", false);
            saveResource("trees/overworld/oak.yml", false);
            saveResource("trees/overworld/spruce.yml", false);
            saveResource("trees/overworld/tall_jungle.yml", false);
            saveResource("trees/overworld/tall_oak.yml", false);
            saveResource("trees/overworld/tall_spruce.yml", false);

            saveResource("trees/nether.yml", false);
            saveResource("trees/nether/crimson_fungus.yml", false);
            saveResource("trees/nether/thick_crimson_fungus.yml", false);
            saveResource("trees/nether/warped_fungus.yml", false);
            saveResource("trees/nether/thick_warped_fungus.yml", false);

            saveResource("trees/mushroom.yml", false);
            saveResource("trees/mushroom/mushroom-brown.yml", false);
            saveResource("trees/mushroom/mushroom-red.yml", false);
        }
        TreeStructure.reloadTreeDefinitions();
    }

    /**
     * Remove a player's CountDown
     *
     * @param playerName the player's name
     */
    public void removeCountDown(String playerName) {
        try {
            coolDowns.get(playerName).cancel();
        } catch (Exception e) {
        }
        coolDowns.remove(playerName);
    }

    /**
     * Set a player's CoolDown
     *
     * @param player the player to set
     * @param config the TreeConfig to check
     * @param logs   the amount of logs to account for
     */
    public void setCoolDown(Player player, TreeConfig config, List<Block> logs) {
        if (player.hasPermission("treeassist.bypass.cooldown")) {
            TreeStructure.debug.i("cooldown bypass permission!");
            return;
        }
        int coolDown = config.getInt(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_COOLDOWN, 0);
        if (coolDown == 0 || coolDownOverrides.contains(player.getName())) {
            return;
        } else if (coolDown < 0) {
            coolDown = ToolUtils.calculateCoolDown(player.getInventory().getItemInMainHand(), logs);
            sendPrefixed(player, Language.MSG.INFO_COOLDOWN_WAIT.parse(
                    String.valueOf(coolDown)));
        }
        CoolDownCounter cc = new CoolDownCounter(player, coolDown);
        cc.runTaskTimer(this, 20L, 20L);
        coolDowns.put(player.getName(), cc);
    }

    /**
     * Set a CoolDown override for a player
     *
     * @param player the player to set
     * @param value  the override status to set
     */
    public synchronized void setCoolDownOverride(String player, boolean value) {
        if (value) {
            coolDownOverrides.add(player);
        } else {
            coolDownOverrides.remove(player);
        }
    }

    @Override
    public String getDebugPrefix() {
        return "";
    }

    @Override
    public String getMessagePrefix() {
        return Language.MSG.INFO_PLUGIN_PREFIX.parse();
    }

    /**
     * Toggle global activity of the plugin for a player
     *
     * @param player the player to update
     * @return whether the player globally is able to use the plugin now
     */
    public boolean toggleGlobal(String player) {
        return toggleWorld("global", player);
    }

    /**
     * Toggle activity of the plugin for a player in a specific world
     *
     * @param world  the world to toggle
     * @param player the player to update
     * @return whether the player is able to use the plugin in that world now
     */
    public boolean toggleWorld(String world, String player) {
        if (disabledMap.containsKey(world)) {
            if (disabledMap.get(world).contains(player)) {
                disabledMap.get(world).remove(player);
                return true;
            } else {
                disabledMap.get(world).add(player);
            }
        } else {
            disabledMap.put(world, new ArrayList<>());
            disabledMap.get(world).add(player);
        }
        return false;
    }

    /**
     * Add a valid tree structure to a list of known trees
     *
     * @param tree the tree
     */
    public void treeAdd(TreeStructure tree) {
        validTrees.add(tree);
    }

    public void treeRemove(TreeStructure tree) {
        validTrees.remove(tree);
    }

    /**
     * Look for all trees that could be close and match the config
     *
     * @param config the config to compare
     * @param block the block to check against
     * @return a set of matching trees that match
     */
    public Set<TreeStructure> treesThatQualify(TreeConfig config, Block block) {
        return treesThatQualify(config, block, 2500);
    }

    /**
     * Look for all trees that could be close and match the config
     *
     * @param config the config to compare
     * @param block the block to check against
     * @return a set of matching trees that match
     */
    public Set<TreeStructure> treesThatQualify(TreeConfig config, Block block, int distanceSquared) {
        Set<TreeStructure> checkTrees = new HashSet<>(validTrees);
        Set<TreeStructure> result = new HashSet<>();
        for (TreeStructure tree : checkTrees) {
            if (config.equals(tree.getConfig())) {
                Location myBlock = block.getLocation();
                Location otherLocation = tree.bottom.getLocation();
                if (myBlock.getWorld().equals(otherLocation.getWorld()) && myBlock.distanceSquared(otherLocation) < distanceSquared) {
                    result.add(tree);
                }
            }
        }
        return result;
    }
}
