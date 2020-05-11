package me.itsatacoshop247.TreeAssist;

import me.itsatacoshop247.TreeAssist.blocklists.*;
import me.itsatacoshop247.TreeAssist.commands.*;
import me.itsatacoshop247.TreeAssist.core.*;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.externals.WorldGuardListener;
import me.itsatacoshop247.TreeAssist.metrics.MetricsLite;
import me.itsatacoshop247.TreeAssist.timers.CooldownCounter;
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
    public List<Location> saplingLocationList = new ArrayList<Location>();
    private final Map<String, List<String>> disabledMap = new HashMap<String, List<String>>();
    final Map<String, AbstractCommand> commandMap = new HashMap<String, AbstractCommand>();
    final List<AbstractCommand> commandList = new ArrayList<AbstractCommand>();
    private Map<String, CooldownCounter> coolDowns = new HashMap<String, CooldownCounter>();
    private Set<String> coolDownOverrides = new HashSet<String>();

    public boolean Enabled = true;
    public boolean mcMMO = false;
    public boolean jobs = false;

    File configFile;
    private Config config;

    public BlockList blockList;
    public TreeAssistBlockListener listener;
    public WorldGuardListener worldGuard = null;
    private TreeAssistSpawnListener spawnListener;

    public int getCoolDown(Player player) {
        if (hasCoolDown(player)) {
            return coolDowns.get(player.getName()).getSeconds();
        }
        return 0;
    }

    public Config getTreeAssistConfig() {
        return config;
    }

    public TreeAssistBlockListener getListener() {
        return listener;
    }

    public boolean hasCoolDown(Player player) {
        return !coolDownOverrides.contains(player.getName()) && coolDowns.containsKey(player.getName());
    }

    public boolean isActive(World world) {
        return (!config.getBoolean(Config.CFG.WORLDS_ENABLE_PER_WORLD)) ||
                config.getStringList(Config.CFG.WORLD_ENABLED_WORLDS, new ArrayList<>()).contains(
                        world.getName());
    }

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

    public boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isForceAutoDestroy(TreeConfig treeConfig) {
        return treeConfig.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION)
                && config.getBoolean(Config.CFG.AUTOMATIC_TREE_DESTRUCTION_FORCED_REMOVAL);
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
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
        if (config.getBoolean(Config.CFG.MAIN_AUTO_PLANT_DROPPED_SAPLINGS)) {
            this.spawnListener = new TreeAssistSpawnListener(this);
            getServer().getPluginManager().registerEvents(spawnListener, this);
        }
        reloadLists();

        MetricsLite metrics = new MetricsLite(this);

        TreeStructure.debug = new Debugger(this, 1);
        TreeAssistBlockListener.debug = new Debugger(this, 6);
        TreeAssistSpawnListener.debug = new Debugger(this, 7);
        Debugger.load(this, Bukkit.getConsoleSender());

        if (!config.getBoolean(Config.CFG.MAIN_IGNORE_USER_PLACED_BLOCKS)) {
            String pluginName = config.getString(
                    Config.CFG.PLACED_BLOCKS_HANDLER_PLUGIN_NAME, "TreeAssist");
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
        //getCommand("treeassist").setTabCompleter(this);

        Language.init(this, config.getString(Config.CFG.MAIN_LANGUAGE, "en"));
    }

    @Override
    public void onLoad() {
        Utils.plugin = this;
        ConfigurationSerialization.registerClass(TreeBlock.class);

        this.configFile = new File(getDataFolder(), "config.yml");
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.config = new Config(configFile);
        this.config.load();

        this.listener = new TreeAssistBlockListener(this);

        checkWorldGuard();
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        return TabComplete.getMatches(sender, commandList, args);
    }

    private void loadCommands() {
        new CommandAddTool().load(commandList, commandMap);
        new CommandDebug().load(commandList, commandMap);
        new CommandFindForest().load(commandList, commandMap);
        new CommandForceBreak().load(commandList, commandMap);
        new CommandForceGrow().load(commandList, commandMap);
        new CommandGlobal().load(commandList, commandMap);
        new CommandNoReplace().load(commandList, commandMap);
        new CommandPurge().load(commandList, commandMap);
        new CommandReload().load(commandList, commandMap);
        new CommandRemoveTool().load(commandList, commandMap);
        new CommandToggle().load(commandList, commandMap);
        new CommandTool().load(commandList, commandMap);
    }

    public void removeCountDown(String playerName) {
        try {
            coolDowns.get(playerName).cancel();
        } catch (Exception e) {

        }
        coolDowns.remove(playerName);
    }

    public void setCoolDown(Player player, List<Block> logs) {
        int coolDown = config.getInt(Config.CFG.AUTOMATIC_TREE_DESTRUCTION_COOLDOWN, 0);
        if (coolDown == 0 || coolDownOverrides.contains(player.getName())) {
            return;
        } else if (coolDown < 0) {
            coolDown = Utils.calculateCooldown(player.getInventory().getItemInMainHand(), logs);
            player.sendMessage(Language.parse(
                    MSG.INFO_COOLDOWN_WAIT, String.valueOf(coolDown)));
        }
        CooldownCounter cc = new CooldownCounter(player, coolDown);
        cc.runTaskTimer(this, 20L, 20L);
        coolDowns.put(player.getName(), cc);
    }

    public synchronized void setCoolDownOverride(String player, boolean value) {
        if (value) {
            coolDownOverrides.add(player);
        } else {
            coolDownOverrides.remove(player);
        }
    }

    /**
     * @return true if the result is "player may use plugin"
     */
    public boolean toggleGlobal(String player) {
        return toggleWorld("global", player);
    }

    private void checkMcMMO() {
        if (config.getBoolean(Config.CFG.MAIN_USE_MCMMO_IF_AVAILABLE)) {
            try {
                if (getServer().getPluginManager().isPluginEnabled("mcMMO")) {
                    Class.forName("com.gmail.nossr50.datatypes.skills.PrimarySkillType");
                    this.mcMMO = true;
                }
            } catch (ClassNotFoundException e) {
                Utils.plugin.getLogger().warning("mcMMO classic is not supported, please update to mcMMO 2.X!");
                this.mcMMO = false;
            }
        } else {
            this.mcMMO = false;
        }
    }

    private void checkJobs() {
        if (config.getBoolean(Config.CFG.MAIN_USE_JOBS_IF_AVAILABLE)) {
            this.jobs = getServer().getPluginManager().isPluginEnabled("Jobs");
        } else {
            this.jobs = false;
        }
    }

    private void checkWorldGuard() {
        if (config.getBoolean(Config.CFG.MAIN_USE_WORLDGUARD_IF_AVAILABLE) && getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuard = new WorldGuardListener();
        }
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void firstRun() throws Exception {
        if (!this.configFile.exists()) {
            this.configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), this.configFile);
        }
    }

    private HashMap<String, String> loadConfigurables(HashMap<String, String> items) {
        //Pre-5.0
        items.put("Main.Automatic Tree Destruction", "true");
        items.put("Main.Use Permissions", "false");
        items.put("Main.Sapling Replant", "true");
        items.put("Automatic Tree Destruction.Apply Full Tool Damage", "true");
        items.put("Main.Ignore User Placed Blocks", "false");
        items.put("Main.Use mcMMO if Available", "true");
        items.put("Automatic Tree Destruction.Tree Types.Birch", "true");
        items.put("Automatic Tree Destruction.Tree Types.Jungle", "true");
        items.put("Automatic Tree Destruction.Tree Types.Oak", "true");
        items.put("Automatic Tree Destruction.Tree Types.Spruce", "true");
        items.put("Leaf Decay.Fast Leaf Decay", "true");
        items.put("Sapling Replant.Bottom Block has to be Broken First", "true");
        items.put("Sapling Replant.Time to Protect Sapling (Seconds)", "0");
        items.put("Sapling Replant.Replant When Tree Burns Down", "true");
        items.put("Sapling Replant.Block all breaking of Saplings", "false");
        items.put("Sapling Replant.Delay until Sapling is replanted (seconds) (minimum 1 second)", "1");
        items.put("Tools.Sapling Replant Require Tools", "true");
        items.put("Tools.Tree Destruction Require Tools", "true");

        //items.put("Tools.Tools List", "LIST"); //TODO: needs to go into tree definitions

        items.put("Worlds.Enable Per World", "false");
        items.put("Worlds.Enabled Worlds", "LIST");
        items.put("Config Help", "dev.bukkit.org/server-mods/tree-assist/pages/config-walkthrough/");

        //5.0 additions
        items.put("Sapling Replant.Tree Types to Replant.Birch", "true");
        items.put("Sapling Replant.Tree Types to Replant.Jungle", "true");
        items.put("Sapling Replant.Tree Types to Replant.Oak", "true");
        items.put("Sapling Replant.Tree Types to Replant.Spruce", "true");

        //5.2 additions
        items.put("Main.Destroy Only Blocks Above", "false");

        //5.4 additions
        items.put("Automatic Tree Destruction.Tree Types.BigJungle", "true");
        items.put("Sapling Replant.Tree Types to Replant.BigJungle", "true");

        //5.5 additions
        items.put("Automatic Tree Destruction.Delay (ticks)", "0");
        items.put("Automatic Tree Destruction.Forced Removal", "false");
        items.put("Automatic Tree Destruction.Initial Delay (seconds)", "10");

        //5.6 additions
        items.put("Main.Auto Plant Dropped Saplings", "false");
        items.put("Auto Plant Dropped Saplings.Chance (percent)", "10");
        items.put("Auto Plant Dropped Saplings.Delay (seconds)", "5");

        //5.7 additions
        items.put("Automatic Tree Destruction.Tree Types.Brown Shroom", "true");
        items.put("Automatic Tree Destruction.Tree Types.Red Shroom", "true");

        //5.7.1 additions
        items.put("Tools.Drop Chance.DIAMOND_AXE", "100");
        items.put("Tools.Drop Chance.WOODEN_AXE", "100");
        items.put("Tools.Drop Chance.GOLDEN_AXE", "100");
        items.put("Tools.Drop Chance.IRON_AXE", "100");
        items.put("Tools.Drop Chance.STONE_AXE", "100");

        //5.7.2 additions
        items.put("Automatic Tree Destruction.Cooldown (seconds)", "0");

        //5.7.3 additions
        // items.put("Custom Drops.APPLE", "0.1");        //TODO needs to go into tree definitions
        // items.put("Custom Drops.GOLDEN_APPLE", "0.0"); //TODO needs to go into tree definitions

        //5.8 additions
        items.put("Placed Blocks.Handler Plugin Name", "TreeAssist");
        items.put("Automatic Tree Destruction.Tree Types.Acacia", "true");
        items.put("Automatic Tree Destruction.Tree Types.Dark Oak", "true");
        items.put("Sapling Replant.Tree Types to Replant.Acacia", "true");
        items.put("Sapling Replant.Tree Types to Replant.Dark Oak", "true");
        items.put("Automatic Tree Destruction.Tree Types.BigSpruce", "true");
        items.put("Sapling Replant.Tree Types to Replant.BigSpruce", "true");

        items.put("Sapling Replant.Enforce", "true");
        items.put("Automatic Tree Destruction.Remove Leaves", "true");
        items.put("Main.Toggle Default", "true");
        items.put("Sapling Replant.Command Time Delay (Seconds)", "30");

        items.put("Automatic Tree Destruction.When Sneaking", "true");
        items.put("Automatic Tree Destruction.Required Lore", "");
        items.put("Automatic Tree Destruction.Initial Delay", "false");

        items.put("Sapling Replant.Time to Block Sapling Growth (Seconds)", "0");
        items.put("Main.Language", "en");

        items.put("Automatic Tree Destruction.Auto Add To Inventory", "false");
        items.put("Automatic Tree Destruction.When Not Sneaking", "true");

        //6.0 additions
        items.put("Modding.Custom Tree Definitions", "CustomTreeDefinitions");

        items.put("Block Statistics.Pickup", "false");
        items.put("Block Statistics.Mine Block", "false");

        items.put("Placed Blocks.Handler Lookup Time", "86400");

        items.put("Main.Use Falling Blocks", "false");

        items.put("Main.Use Jobs if Available", "true");

        items.put("Main.Use WorldGuard if Available", "false");

        items.put("Version", "6.0");

        return items;
    }

    public void reloadLists() {
        if (!new File(getDataFolder().getPath(), "trees").exists()) {
            saveResource("trees/overworld.yml", false);
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
        Utils.reloadTreeDefinitions(config);
    }

    /**
     * @return true if the result is "player may use plugin"
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
}
