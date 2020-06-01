package me.itsatacoshop247.TreeAssist;

import me.itsatacoshop247.TreeAssist.core.*;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.events.TALeafDecay;
import me.itsatacoshop247.TreeAssist.events.TASaplingReplaceEvent;
import me.itsatacoshop247.TreeAssist.externals.mcMMOHook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import java.util.*;

public class TreeAssistBlockListener implements Listener {
    public static Debugger debug;

    public TreeAssist plugin;

    private final TreeAssistAntiGrow antiGrow;
    private final Map<String, Long> noreplace = new HashMap<String, Long>();
    private final String protectToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Protect";

    public TreeAssistBlockListener(TreeAssist instance) {
        plugin = instance;
        antiGrow = new TreeAssistAntiGrow(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (plugin.getTreeAssistConfig().getBoolean(Config.CFG.LEAF_DECAY_FAST_LEAF_DECAY) && plugin.Enabled) {
            Block block = event.getBlock();
            World world = block.getWorld();
            if (!plugin.isActive(world)) {
                return;
            }
            breakRadiusLeaves(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.hasBlock()) {
            if (this.isProtectTool(event.getPlayer().getItemInHand())) {
                Block clicked = event.getClickedBlock();

                if (Utils.isSapling(clicked.getType())) {
                    if (plugin.saplingLocationList.contains(clicked.getLocation())) {
                        plugin.saplingLocationList.remove(clicked.getLocation());
                        event.getPlayer().sendMessage(
                                Language.parse(MSG.SUCCESSFUL_PROTECT_OFF));
                    } else {
                        plugin.saplingLocationList.add(clicked.getLocation());
                        event.getPlayer().sendMessage(
                                Language.parse(MSG.SUCCESSFUL_PROTECT_ON));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_IGNORE_USER_PLACED_BLOCKS) &&
                (TreeStructure.allTrunks.contains(event.getBlock().getType()))) {
            if (plugin.getTreeAssistConfig().getBoolean(Config.CFG.WORLDS_ENABLE_PER_WORLD)) {
                if (!plugin.getTreeAssistConfig().getStringList(Config.CFG.WORLD_ENABLED_WORLDS, new ArrayList<>()).contains(event.getBlock().getWorld().getName())) {
                    return;
                }
            }
            Block block = event.getBlock();
            plugin.blockList.addBlock(block);
            plugin.blockList.save();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        checkFire(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        checkFire(event.getBlock());
    }

    private void checkFire(Block block) {

        if (plugin.getTreeAssistConfig().getBoolean(Config.CFG.SAPLING_REPLANT_REPLANT_WHEN_TREE_BURNS_DOWN) && plugin.Enabled) {
            if (plugin.getTreeAssistConfig().getBoolean(Config.CFG.WORLDS_ENABLE_PER_WORLD)) {
                if (!plugin.getTreeAssistConfig().getStringList(Config.CFG.WORLD_ENABLED_WORLDS, new ArrayList<>()).contains(block.getWorld().getName())) {
                    return;
                }
            }
            MaterialData data = block.getState().getData();
            if (data instanceof Tree) {
                Material logMat = block.getType();
                Tree tree = (Tree) data;
                Block onebelow = block.getRelative(BlockFace.DOWN, 1);
                Block oneabove = block.getRelative(BlockFace.UP, 1);
                if (onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS_BLOCK || onebelow.getType() == Material.PODZOL) {
                    if (Utils.isAir(oneabove.getType()) || oneabove.getType() == logMat) {
                        TASaplingReplaceEvent event = new TASaplingReplaceEvent(block, tree.getSpecies().name());
                        Utils.plugin.getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            debug.i("TreeAssistBlockListener.checkFire() Sapling Replant was cancelled!");
                            return;
                        }
                        Runnable b = new TreeAssistReplant(plugin, block, tree.getSpecies());
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.Enabled) {
            return;
        }

        if (!TreeStructure.allTrunks.contains(event.getBlock().getType())) {
            return;
        }

        if (!plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_IGNORE_USER_PLACED_BLOCKS)) {
            if (plugin.blockList.isPlayerPlaced(event.getBlock())) {
                debug.i("User placed block. Removing!");
                plugin.blockList.removeBlock(event.getBlock());
                plugin.blockList.save();
                return;
            }
        }

        Player player = event.getPlayer();

        TreeConfig matchingTreeConfig = null;
        TreeStructure foundTree = null;

        configs: for (TreeConfig config : Utils.treeConfigs.values()) {
            List<String> list = config.getStringList(TreeConfig.CFG.TRUNK_MATERIALS, new ArrayList<>());
            for (String matName : list) {
                Material mat = Material.matchMaterial(matName);
                debug.i("checking for material " + mat);
                if (event.getBlock().getType().equals(mat)) {
                    Block block = TreeCalculator.validate(event.getBlock(), config);
                    if (block != null) {
                        debug.i("Tree found for Material " + matName);

                        TreeStructure trunk = new TreeStructure(config, block, false);

                        if (trunk.isValid()) {
                            debug.i("Tree matches " + matName);

                            if (plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_USE_PERMISSIONS) &&
                                !player.hasPermission(config.getString(TreeConfig.CFG.PERMISSION))) {
                                debug.i("Player does not have permission " + config.getString(TreeConfig.CFG.PERMISSION));
                                matchingTreeConfig = config; // for maybe forcing something later
                                foundTree = trunk;
                                break;
                            }

                            if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_ACTIVE)) {
                                if (Utils.plugin.hasCoolDown(player)) {
                                    debug.i("Cooldown!");
                                    player.sendMessage(Language.parse(MSG.INFO_COOLDOWN_STILL));
                                    player.sendMessage(Language.parse(MSG.INFO_COOLDOWN_VALUE, String.valueOf(Utils.plugin.getCoolDown(player))));
                                    matchingTreeConfig = config; // for maybe forcing something later
                                    foundTree = trunk;
                                    break configs;
                                }

                                if (plugin.isDisabled(player.getWorld().getName(), player.getName())) {
                                    debug.i("Disabled for this player in this world!");
                                    matchingTreeConfig = config; // for maybe forcing something later
                                    foundTree = trunk;
                                    break configs;
                                }

                                String lore = config.getString(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRED_LORE);
                                if (!"".equals(lore)) {
                                    ItemStack item = player.getInventory().getItemInMainHand();
                                    if (!item.hasItemMeta() || !item.getItemMeta().hasLore() || !item.getItemMeta().getLore().contains(lore)) {
                                        debug.i("Lore not found: " + lore);
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                }

                                if (player.isSneaking()) {
                                    if (!plugin.getTreeAssistConfig().getBoolean(Config.CFG.AUTOMATIC_TREE_DESTRUCTION_WHEN_SNEAKING)) {
                                        debug.i("Sneaking is bad!");
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                } else {
                                    if (!plugin.getTreeAssistConfig().getBoolean(Config.CFG.AUTOMATIC_TREE_DESTRUCTION_WHEN_NOT_SNEAKING)) {
                                        debug.i("Not sneaking is bad!");
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                }

                                if (Utils.plugin.mcMMO && mcMMOHook.mcMMOTreeFeller(player)) {
                                    debug.i("mcMMO Tree Feller!");
                                    matchingTreeConfig = config; // for maybe forcing something later
                                    foundTree = trunk;
                                    break;
                                }

                                if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRES_TOOLS)) {
                                    if (!Utils.isRequiredTool(player.getInventory().getItemInMainHand(), config)) {
                                        debug.i("Player has not the right tool!");
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                }

                                debug.i("success!");
                                ItemStack item = player.getInventory().getItemInMainHand();

                                if (!plugin.getTreeAssistConfig().getBoolean(Config.CFG.MODDING_DISABLE_DURABILITY_FIX)) {
                                    if (item.hasItemMeta()) {
                                        short durability = (short)((Damageable)item.getItemMeta()).getDamage();
                                        short maxDurability = item.getType().getMaxDurability();

                                        if (((durability > maxDurability) || durability < 0)
                                                && Utils.isVanillaTool(item)) {
                                            debug.i("removing item: " + item.getType().name() +
                                                    " (durability " + durability + ">" + maxDurability);
                                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                            player.updateInventory();
                                        }
                                    }
                                }

                                trunk.maybeReplant(player, event.getBlock());
                                if (Utils.plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_DESTROY_ONLY_BLOCKS_ABOVE)) {
                                    trunk.clearUpTo(event.getBlock());
                                }
                                trunk.removeLater(player, item);
                                return;

                            } else if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL) ||
                                    matchingTreeConfig.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {
                                matchingTreeConfig = config;
                                foundTree = trunk;
                                break;
                            }

                            // else:  we did not find a match or we do not want to force remove it - let's try another!
                        }
                        debug.i("Shape does not match " + matName);
                    }
                }
            }
        }

        if (matchingTreeConfig != null) {
            debug.i("Fallback to enforcing something!");
            if (matchingTreeConfig.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {
                foundTree.maybeReplant(null, event.getBlock());
            }

            if (matchingTreeConfig.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL)) {
                if (Utils.plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_DESTROY_ONLY_BLOCKS_ABOVE)) {
                    foundTree.clearUpTo(event.getBlock());
                }
                foundTree.removeLater(null, null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickUpEvent(EntityPickupItemEvent event) {
        if (event.getItem() instanceof FallingBlock) {
            if (Utils.removeIfFallen(event.getItem())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            Utils.removeIfFallen(event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_TOGGLE_DEFAULT)) {
            plugin.toggleGlobal(event.getPlayer().getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (antiGrow.contains(event.getLocation())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            if (Utils.removeIfFallen(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks if the block is a leaf block and drops it
     * if no log is in 2 block radius around
     *
     * @param blockAt the block to check
     */
    private void breakIfLonelyLeaf(Block blockAt) {
        if (!Utils.isLeaf(blockAt.getType())) {
            return;
        }
        World world = blockAt.getWorld();

        int fail = -1; // because we will fail once, when finding blockAt

        for (int x = blockAt.getX() - 2; x <= blockAt.getX() + 2; x++) {
            for (int y = blockAt.getY() - 2; y <= blockAt.getY() + 2; y++) {
                for (int z = blockAt.getZ() - 2; z <= blockAt.getZ() + 2; z++) {
                    fail += calcAir(world.getBlockAt(x, y, z));
                    if (fail > 4) {
                        return; // fail threshold -> out!
                    }
                }
            }
        }

        TALeafDecay event = new TALeafDecay(blockAt);
        Utils.plugin.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
        	Utils.plugin.blockList.logBreak(blockAt, null);
        	Utils.breakBlock(blockAt);
        }
    }



    /**
     * enforces an 8 block radius FloatingLeaf removal
     *
     * @param blockAt the block to check
     */
    public void breakRadiusLeaves(Block blockAt) {
        TALeafDecay event = new TALeafDecay(blockAt);
        Utils.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Utils.plugin.blockList.logBreak(blockAt, null);
        Utils.breakBlock(blockAt);
        World world = blockAt.getWorld();
        int x = blockAt.getX();
        int y = blockAt.getY();
        int z = blockAt.getZ();
        for (int x2 = -8; x2 < 9; x2++) {
            for (int z2 = -8; z2 < 9; z2++) {
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 2, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 1, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 1, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 2, z + z2));
            }
        }
    }

    private int calcAir(Block blockAt) {
        if (Utils.isAir(blockAt.getType()) || blockAt.getType() == Material.VINE || Utils.isLeaf(blockAt.getType())) {
            return 0;
        } else if (Utils.isLog(blockAt.getType())) {
            return 5;
        } else {
            return 1;
        }
    }

    public ItemStack getProtectionTool() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(protectToolDisplayName);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isProtectTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(protectToolDisplayName);
    }

    public TreeAssistAntiGrow getAntiGrow() {
        return antiGrow;
    }

    public void noReplace(String name, int seconds) {
        noreplace.put(name, (Long) (System.currentTimeMillis() / 1000) + seconds);
    }

    public boolean isNoReplace(String name) {
        if (noreplace.containsKey(name)) {
            if (noreplace.get(name) < System.currentTimeMillis() / 1000) {
                noreplace.remove(name);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
