package net.slipcor.treeassist;

import net.slipcor.treeassist.blocklist.*;
import net.slipcor.treeassist.commands.*;
import net.slipcor.treeassist.configs.MainConfig;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.externals.WorldGuardListener;
import net.slipcor.treeassist.listeners.TreeAssistBlockListener;
import net.slipcor.treeassist.listeners.TreeAssistSpawnListener;
import net.slipcor.treeassist.metrics.MetricsLite;
import net.slipcor.treeassist.runnables.CleanRunner;
import net.slipcor.treeassist.runnables.CoolDownCounter;
import net.slipcor.treeassist.core.*;
import net.slipcor.treeassist.utils.ToolUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class TreeAssist extends JavaPlugin {
    public static Map<String, TreeConfig> treeConfigs = new LinkedHashMap<>();        // The TreeConfigs which define what constitutes a tree
    public static TreeAssist instance;                                          // Static plugin instance

    public List<Location> saplingLocationList = new ArrayList<>();              // List of protected saplings
    private final Map<String, List<String>> disabledMap = new HashMap<>();      // List of player names who disabled TreeAssist
    final Map<String, AbstractCommand> commandMap = new HashMap<>();            // Map of commands
    final List<AbstractCommand> commandList = new ArrayList<>();                // List of commands
    private final Map<String, CoolDownCounter> coolDowns = new HashMap<>();     // Maps of player names to active CoolDown Runnables
    private final Set<String> coolDownOverrides = new HashSet<>();              // List of player names who override cooldowns

    private final Set<TreeStructure> validTrees = new HashSet<>();

    public boolean Enabled = true;  // Whether the plugin as a whole is enabled
    public boolean mcMMO = false;   // Whether mcMMO has been found and hooked into
    public boolean jobs = false;    // Whether Jobs has been found and hooked into

    private File configFile;
    private MainConfig config;

    public BlockList blockList;                     // Placed Block List instance

    private WorldGuardListener worldGuard = null;   // WorldGuard Listener instance
    private TreeAssistBlockListener listener;       // Block Listener instance
    private TreeAssistSpawnListener spawnListener;  // Item Spawn Listener instance


    /**
     * Check for mcMMO if requested
     */
    private void checkMcMMO() {
        if (config.getBoolean(MainConfig.CFG.PLUGINS_USE_MCMMO)) {
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
        if (config.getBoolean(MainConfig.CFG.PLUGINS_USE_JOBS)) {
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
    public MainConfig getMainConfig() {
        return config;
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
    private void loadCommands() {
        new CommandAddTool().load(commandList, commandMap);
        new CommandDebug().load(commandList, commandMap);
        new CommandFindForest().load(commandList, commandMap);
        new CommandForceBreak().load(commandList, commandMap);
        new CommandForceGrow().load(commandList, commandMap);
        new CommandGlobal().load(commandList, commandMap);
        new CommandNoReplant().load(commandList, commandMap);
        new CommandPurge().load(commandList, commandMap);
        new CommandReload().load(commandList, commandMap);
        new CommandRemoveTool().load(commandList, commandMap);
        new CommandToggle().load(commandList, commandMap);
        new CommandTool().load(commandList, commandMap);
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final AbstractCommand acc = (args.length > 0) ? commandMap.get(args[0].toLowerCase()) : null;
        if (acc != null) {
            acc.commit(sender, args);
            return true;
        }
        boolean found = false;
        for (AbstractCommand command : commandList) {
            if (command.hasPerms(sender)) {
                sender.sendMessage(ChatColor.YELLOW + command.getShortInfo());
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
        Debugger.destroy();
    }

    public void onEnable() {

        checkMcMMO();
        checkJobs();

        if (worldGuard != null) {
            getServer().getPluginManager().registerEvents(worldGuard, this);
        }

        getServer().getPluginManager().registerEvents(listener, this);

        this.spawnListener = new TreeAssistSpawnListener(this);
        getServer().getPluginManager().registerEvents(spawnListener, this);

        MetricsLite metrics = new MetricsLite(this);

        TreeStructure.debug = new Debugger(this, 1);
        CleanRunner.debug = new Debugger(this, 2);
        TreeAssistBlockListener.debug = new Debugger(this, 6);
        TreeAssistSpawnListener.debug = new Debugger(this, 7);
        Debugger.load(this, Bukkit.getConsoleSender());

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

        Language.init(this, config.getString(MainConfig.CFG.GENERAL_LANGUAGE, "lang_en"));
    }

    @Override
    public void onLoad() {
        instance = this;
        ConfigurationSerialization.registerClass(TreeBlock.class);

        this.configFile = new File(getDataFolder(), "config.yml");
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.config = new MainConfig(configFile);
        reloadLists();
        this.config.load();

        this.listener = new TreeAssistBlockListener(this);

        checkWorldGuard();
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        return TabComplete.getMatches(sender, commandList, args);
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
            player.sendMessage(Language.parse(
                    Language.MSG.INFO_COOLDOWN_WAIT, String.valueOf(coolDown)));
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
