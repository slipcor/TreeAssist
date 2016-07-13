package me.itsatacoshop247.TreeAssist;

import me.itsatacoshop247.TreeAssist.blocklists.*;
import me.itsatacoshop247.TreeAssist.commands.*;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.metrics.MetricsLite;
import me.itsatacoshop247.TreeAssist.timers.CooldownCounter;
import me.itsatacoshop247.TreeAssist.trees.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;


//Running changelog

//- Tree destruction will only destory the main blocks Type ID. E.G. if a Oak Tree branch hits a jungle tree branch, the Oak Tree will not destroy the jungle Branch and vice-versa
//- Fixed Blocks dissapearing when used to destroy a tree.
//- Config autoupdates

public class TreeAssist extends JavaPlugin {
    public List<Location> saplingLocationList = new ArrayList<Location>();
    private final Map<String, List<String>> disabledMap = new HashMap<String, List<String>>();
    final Map<String, AbstractCommand> commandMap = new HashMap<String, AbstractCommand>();
    final List<AbstractCommand> commandList = new ArrayList<AbstractCommand>();
    private Map<String, CooldownCounter> coolDowns = new HashMap<String, CooldownCounter>();
    private Set<String> coolDownOverrides = new HashSet<String>();

    public boolean Enabled = true;
    public boolean mcMMO = false;

    File configFile;
    FileConfiguration config;

    public BlockList blockList;
    public TreeAssistBlockListener listener;

    public int getCoolDown(Player player) {
        if (hasCoolDown(player)) {
            return coolDowns.get(player.getName()).getSeconds();
        }
        return 0;
    }

    public TreeAssistBlockListener getListener() {
        return listener;
    }

    public boolean hasCoolDown(Player player) {
        return !coolDownOverrides.contains(player.getName()) && coolDowns.containsKey(player.getName());
    }

    public boolean isActive(World world) {
        return (!config.getBoolean("Worlds.Enable Per World")) ||
                config.getList("Worlds.Enabled Worlds").contains(
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

    public boolean isForceAutoDestroy() {
        return getConfig().getBoolean("Main.Automatic Tree Destruction")
                && getConfig().getBoolean("Automatic Tree Destruction.Forced Removal");
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void loadYamls() {
        try {
            this.config.load(this.configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length <= 0) {
            return false;
        }
        AbstractCommand acc = commandMap.get(args[0].toLowerCase());
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

        Utils.plugin = this;

        this.configFile = new File(getDataFolder(), "config.yml");
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.config = new YamlConfiguration();

        this.listener = new TreeAssistBlockListener(this);

        loadYamls();
        config.options().copyDefaults(true);
        //check for defaults to set newly

        this.updateConfig();

        getServer().getPluginManager().registerEvents(listener, this);
        if (config.getBoolean("Main.Auto Plant Dropped Saplings")) {
            getServer().getPluginManager().registerEvents(new TreeAssistSpawnListener(this), this);
        }
        reloadLists();

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        BaseTree.debug = new Debugger(this, 1);
        CustomTree.debugger = new Debugger(this, 2);
        InvalidTree.debugger = new Debugger(this, 3);
        MushroomTree.debugger = new Debugger(this, 4);
        VanillaTree.debugger = new Debugger(this, 5);
        VanillaOneSevenTree.debugger = new Debugger(this, 6);
        Debugger.load(this, Bukkit.getConsoleSender());


        initiateList("Modding.Custom Logs", Utils.validTypes);
        initiateList("Modding.Custom Tree Blocks", Utils.validTypes);

        if (!getConfig().getBoolean("Main.Ignore User Placed Blocks")) {
            String pluginName = getConfig().getString(
                    "Placed Blocks.Handler Plugin Name", "TreeAssist");
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

        Language.init(this, config.getString("Main.Language", "en"));
    }

    private void loadCommands() {
        new CommandAddCustom().load(commandList, commandMap);
        new CommandAddTool().load(commandList, commandMap);
        new CommandDebug().load(commandList, commandMap);
        new CommandForceBreak().load(commandList, commandMap);
        new CommandForceGrow().load(commandList, commandMap);
        new CommandGlobal().load(commandList, commandMap);
        new CommandNoReplace().load(commandList, commandMap);
        new CommandPurge().load(commandList, commandMap);
        new CommandReload().load(commandList, commandMap);
        new CommandRemoveCustom().load(commandList, commandMap);
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

    public void saveConfig() {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCoolDown(Player player, BaseTree tree) {
        int coolDown = getConfig().getInt("Automatic Tree Destruction.Cooldown (seconds)", 0);
        if (coolDown == 0 || tree == null || !tree.isValid() || coolDownOverrides.contains(player.getName())) {
            return;
        } else if (coolDown < 0) {
            coolDown = tree.calculateCooldown(player.getItemInHand());
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
        if (getConfig().getBoolean("Main.Use mcMMO if Available")) {
            this.mcMMO = getServer().getPluginManager().isPluginEnabled("mcMMO");
        } else {
            this.mcMMO = false;
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

    private void initiateList(String string, List<Integer> validTypes) {
        for (Object obj : config.getList(string)) {
            if (obj instanceof Integer) {
                validTypes.add((Integer) obj);
                continue;
            }
            if (obj.equals("LIST ITEMS GO HERE")) {
                List<Object> list = new ArrayList<Object>();
                list.add(-1);
                config.set(string, list);
                saveConfig();
                break;
            }
            validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
        }
    }

    private HashMap<String, String> loadConfigurables(HashMap<String, String> items) {
        //Pre-5.0
        items.put("Main.Automatic Tree Destruction", "true");
        items.put("Main.Use Permissions", "false");
        items.put("Main.Sapling Replant", "true");
        items.put("Main.Apply Full Tool Damage", "true");
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
        items.put("Tools.Tools List", "LIST");
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

        //5.3 additions
        items.put("Modding.Custom Logs", "LIST");
        items.put("Modding.Custom Tree Blocks", "LIST");
        items.put("Modding.Custom Saplings", "LIST");

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
        items.put("Tools.Drop Chance.WOOD_AXE", "100");
        items.put("Tools.Drop Chance.GOLD_AXE", "100");
        items.put("Tools.Drop Chance.IRON_AXE", "100");
        items.put("Tools.Drop Chance.STONE_AXE", "100");

        //5.7.2 additions
        items.put("Automatic Tree Destruction.Cooldown (seconds)", "0");

        //5.7.3 additions
        items.put("Custom Drops.APPLE", "0.1");
        items.put("Custom Drops.GOLDEN_APPLE", "0.0");

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
        items.put("Main.Initial Delay", "false");

        items.put("Sapling Replant.Time to Block Sapling Growth (Seconds)", "0");
        items.put("Main.Language", "en");
        return items;
    }

    public void reloadLists() {
        CustomTree.customTreeBlocks = config.getList("Modding.Custom Tree Blocks");
        CustomTree.customLogs = config.getList("Modding.Custom Logs");
        CustomTree.customSaplings = config.getList("Modding.Custom Saplings");
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
            disabledMap.put(world, new ArrayList<String>());
            disabledMap.get(world).add(player);
        }
        return false;
    }

    private void updateConfig() {
        HashMap<String, String> items = new HashMap<String, String>();

        items = loadConfigurables(items);

        int num = 0;
        for (Map.Entry<String, String> item : items.entrySet()) {
            if (this.config.get(item.getKey()) == null) {
                getLogger().info(item.getKey());
                if (item.getValue().equalsIgnoreCase("LIST")) {
                    List<String> list = Arrays.asList("LIST ITEMS GO HERE");
                    this.config.addDefault(item.getKey(), list);
                } else if (item.getValue().equalsIgnoreCase("true")) {
                    this.config.addDefault(item.getKey(), true);
                } else if (item.getValue().equalsIgnoreCase("false")) {
                    this.config.addDefault(item.getKey(), false);
                } else if (isInteger(item.getValue())) {
                    this.config.addDefault(item.getKey(), Integer.parseInt(item.getValue()));
                } else if (isDouble(item.getValue())) {
                    this.config.addDefault(item.getKey(), Double.parseDouble(item.getValue()));
                } else {
                    this.config.addDefault(item.getKey(), item.getValue());
                }
                num++;
            }
        }
        if (num > 0) {
            getLogger().info(num + " missing items added to config file.");
        }
        this.saveConfig();
    }
}
