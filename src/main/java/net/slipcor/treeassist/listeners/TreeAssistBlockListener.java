package net.slipcor.treeassist.listeners;

import net.slipcor.core.CoreDebugger;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.FailReason;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.events.TASaplingBreakEvent;
import net.slipcor.treeassist.events.TASaplingPlaceEvent;
import net.slipcor.treeassist.externals.mcMMOHook;
import net.slipcor.treeassist.runnables.TreeAssistAntiGrow;
import net.slipcor.treeassist.runnables.TreeAssistReplant;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.utils.ToolUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeAssistBlockListener implements Listener {
    public static CoreDebugger debug;

    public TreeAssist plugin;

    private final TreeAssistAntiGrow antiGrow;
    private final Map<String, Long> noreplant = new HashMap<>();
    private final String protectToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Protect";
    private final String growToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Grow";

    public TreeAssistBlockListener(TreeAssist instance) {
        plugin = instance;
        antiGrow = new TreeAssistAntiGrow();
    }

    private final static List<BlockBreakEvent> ignoring = new ArrayList<>();

    public static void ignore(BlockBreakEvent event) {
        ignoring.add(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        debug.i("leaf is decaying: " + BlockUtils.printBlock(event.getBlock()));
        if (plugin.config().getBoolean(MainConfig.CFG.DESTRUCTION_FAST_LEAF_DECAY) && plugin.Enabled) {
            debug.i("we want fast decay!");
            Block block = event.getBlock();
            World world = block.getWorld();
            if (plugin.isActive(world)) {
                debug.i("we are active here!");
                for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                    if (config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS).contains(block.getType())) {
                        debug.i("let's go!");
                        BlockUtils.breakRadiusLeaves(block, config);
                    }
                }
            } else {
                debug.i("not in this world: " + event.getBlock().getWorld().getName());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.hasBlock()) {
            if (this.isProtectTool(event.getPlayer().getInventory().getItemInMainHand())) {
                Block clicked = event.getClickedBlock();

                if (clicked != null && MaterialUtils.isSapling(clicked.getType())) {
                    if (plugin.saplingLocationList.contains(clicked.getLocation())) {
                        plugin.saplingLocationList.remove(clicked.getLocation());
                        TreeAssist.instance.sendPrefixed(event.getPlayer(),
                                Language.MSG.SUCCESSFUL_PROTECT_OFF.parse());
                    } else {
                        plugin.saplingLocationList.add(clicked.getLocation());
                        TreeAssist.instance.sendPrefixed(event.getPlayer(),
                                Language.MSG.SUCCESSFUL_PROTECT_ON.parse());
                    }
                }
            } else if (this.isGrowTool(event.getPlayer().getInventory().getItemInMainHand())) {
                if (event.getBlockFace().equals(BlockFace.UP)) {
                    ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
                    TreeType type = TreeType.valueOf(tool.getItemMeta().getLore().get(0));

                    Block destination = event.getClickedBlock().getRelative(BlockFace.UP);
                    for (int i=0; i< 20; i++) {
                        if (destination.getWorld().generateTree(destination.getLocation(), type)) {
                            return;
                        }
                    }

                    TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.ERROR_GROW.parse());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.config().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE) &&
                (TreeStructure.allTrunks.contains(event.getBlock().getType()))) {
            if (plugin.isActive(event.getBlock().getWorld())) {
                Block block = event.getBlock();
                plugin.blockList.addBlock(block);
                plugin.blockList.save();
            } else {
                debug.i("not in this world: " + event.getBlock().getWorld().getName());
            }
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
        if (!plugin.Enabled) {
            return;
        }

        if (!plugin.isActive(block.getWorld())) {
            debug.i("not in this world: " + block.getWorld().getName());
            return;
        }

        if (!TreeStructure.allTrunks.contains(block.getType())) {
            debug.i("Not a burning tree block: " + block.getType());
            return;
        }

        if (plugin.config().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
            if (plugin.blockList.isPlayerPlaced(block)) {
                debug.i("User placed block. Removing!");
                plugin.blockList.removeBlock(block);
                plugin.blockList.save();
                return;
            }
        }

        for (TreeConfig config : TreeAssist.treeConfigs.values()) {
            List<Material> list = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);
            for (Material mat : list) {
                debug.i("checking for material " + mat + "(" + config.getConfigName() +")");
                if (!block.getType().equals(mat)) {
                    continue;
                }

                if (!config.getBoolean(TreeConfig.CFG.REPLANTING_WHEN_TREE_BURNS_DOWN)) {
                    debug.i("burn replanting disabled in config");
                    continue;
                }

                Block oneBelow = block.getRelative(BlockFace.DOWN, 1);
                List<Material> grounds = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);
                if (!grounds.contains(oneBelow.getType())) {
                    debug.i("not a valid ground: " + BlockUtils.printBlock(oneBelow));
                    continue;
                }
                Block oneAbove = block.getRelative(BlockFace.UP, 1);
                if (!MaterialUtils.isAir(oneAbove.getType()) && !list.contains(oneAbove.getType())) {
                    debug.i("not a valid block above: " + BlockUtils.printBlock(oneAbove));
                    continue;
                }
                Material replantMat = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);
                TASaplingPlaceEvent event = new TASaplingPlaceEvent(block, replantMat);
                TreeAssist.instance.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    debug.i("TreeAssistBlockListener.checkFire() Sapling Replant was cancelled!");
                    return;
                }
                Runnable b = new TreeAssistReplant(block, event.getType(), config);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (ignoring.contains(event)) {
            ignoring.remove(event);
            debug.i("skip this one!");
            return;
        }

        if (!plugin.Enabled) {
            return;
        }

        if (!plugin.isActive(event.getBlock().getWorld())) {
            debug.i("not in this world: " + event.getBlock().getWorld().getName());
            return;
        }

        if (MaterialUtils.isSapling(event.getBlock().getType())) {
            if (plugin.saplingLocationList.contains(event.getBlock().getLocation())) {
                TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.INFO_SAPLING_PROTECTED.parse());
                event.setCancelled(true);
                return;
            }
            TASaplingBreakEvent saplingBreakEvent = new TASaplingBreakEvent(event.getBlock(), event.getBlock().getType());

            TreeAssist.instance.getServer().getPluginManager().callEvent(saplingBreakEvent);

            if (saplingBreakEvent.isCancelled()) {
                debug.i("Another plugin prevented sapling breaking!");
                TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.INFO_SAPLING_PROTECTED.parse());
                event.setCancelled(true);
                return;
            }

            for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                if (event.getBlock().getType() == config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL)) {
                    if (config.getBoolean(TreeConfig.CFG.REPLANTING_FORCE_PROTECT)) {
                        TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.INFO_NEVER_BREAK_SAPLINGS.parse());
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (!TreeStructure.allTrunks.contains(event.getBlock().getType())) {
            debug.i("Not a tree block: " + event.getBlock().getType());
            return;
        }

        if (plugin.config().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
            if (plugin.blockList.isPlayerPlaced(event.getBlock())) {
                debug.i("User placed block. Removing!");
                plugin.blockList.removeBlock(event.getBlock());
                plugin.blockList.save();
                return;
            }
        }

        Player player = event.getPlayer();

        TreeConfig matchingTreeConfig = null;
        TreeStructure matchingTreeStructure = null;

        configs: for (TreeConfig config : TreeAssist.treeConfigs.values()) {
            List<Material> list = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);
            debug.i("--- checking config " + config.getConfigName());
            for (Material mat : list) {
                debug.i("checking for material " + mat);
                if (!event.getBlock().getType().equals(mat)) {
                    continue;
                }
                Block block = TreeStructure.findBottomBlock(event.getBlock(), config);
                if (block == null) {
                    continue;
                }
                debug.i("Tree found for Material " + mat);

                TreeStructure checkTreeStructure = new TreeStructure(config, block, false);

                if (checkTreeStructure.isValid()) {
                    debug.i("Tree matches " + mat);

                    if (TreeAssist.instance.config().getBoolean(MainConfig.CFG.GENERAL_PREVENT_WITHOUT_TOOL)) {
                        if (!ToolUtils.isMatchingTool(player.getInventory().getItemInMainHand(), config)) {
                            debug.i("Player has not the right tool and we want to prevent now!");
                            if ((player.isOp() || (player.getGameMode() == GameMode.CREATIVE))) {
                                debug.i("Player is OP or creative, let them be!");
                            } else {
                                TreeAssist.instance.sendPrefixed(player, Language.MSG.INFO_NEVER_BREAK_LOG_WITHOUT_TOOL.parse());
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }

                    if (plugin.config().getBoolean(MainConfig.CFG.GENERAL_USE_PERMISSIONS) &&
                            !player.hasPermission(config.getString(TreeConfig.CFG.PERMISSION))) {
                        debug.i("Player does not have permission " + config.getString(TreeConfig.CFG.PERMISSION));
                        matchingTreeConfig = config; // for maybe forcing something later
                        matchingTreeStructure = checkTreeStructure;
                        continue configs;
                    }

                    if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_ACTIVE)) {
                        if (TreeAssist.instance.hasCoolDown(player)) {
                            debug.i("Cooldown!");
                            TreeAssist.instance.sendPrefixed(player, Language.MSG.INFO_COOLDOWN_STILL.parse());
                            TreeAssist.instance.sendPrefixed(player, Language.MSG.INFO_COOLDOWN_VALUE.parse(String.valueOf(TreeAssist.instance.getCoolDown(player))));
                            matchingTreeConfig = config; // for maybe forcing something later
                            matchingTreeStructure = checkTreeStructure;

                            break configs;
                        }

                        if (plugin.isDisabled(player.getWorld().getName(), player.getName())) {
                            debug.i("Disabled for this player in this world!");
                            matchingTreeConfig = config; // for maybe forcing something later
                            matchingTreeStructure = checkTreeStructure;

                            break configs;
                        }

                        String lore = config.getString(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRED_LORE);
                        if (!"".equals(lore)) {
                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (!item.hasItemMeta() || !item.getItemMeta().hasLore() || !item.getItemMeta().getLore().contains(lore)) {
                                debug.i("Lore not found: " + lore);
                                matchingTreeConfig = config; // for maybe forcing something later
                                matchingTreeStructure = checkTreeStructure;
                                continue configs;
                            }
                        }

                        if (player.isSneaking()) {
                            if (!config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_WHEN_SNEAKING)) {
                                debug.i("Sneaking is bad!");
                                matchingTreeConfig = config; // for maybe forcing something later
                                matchingTreeStructure = checkTreeStructure;
                                continue configs;
                            }
                        } else {
                            if (!config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_WHEN_NOT_SNEAKING)) {
                                debug.i("Not sneaking is bad!");
                                matchingTreeConfig = config; // for maybe forcing something later
                                matchingTreeStructure = checkTreeStructure;
                                continue configs;
                            }
                        }

                        if (TreeAssist.instance.mcMMO && mcMMOHook.mcMMOTreeFeller(player)) {
                            debug.i("mcMMO Tree Feller!");
                            matchingTreeConfig = config; // for maybe forcing something later
                            matchingTreeStructure = checkTreeStructure;
                            continue configs;
                        }

                        if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRES_TOOLS)) {
                            if (!ToolUtils.isMatchingTool(player.getInventory().getItemInMainHand(), config)) {
                                debug.i("Player has not the right tool!");
                                matchingTreeConfig = config; // for maybe forcing something later
                                matchingTreeStructure = checkTreeStructure;
                                continue configs;
                            }
                        }

                        debug.i("success!");
                        ItemStack item = player.getInventory().getItemInMainHand();

                        if (!plugin.config().getBoolean(MainConfig.CFG.MODDING_DISABLE_DURABILITY_FIX)) {
                            if (item.hasItemMeta() && item.getItemMeta() != null) {
                                if (!item.getItemMeta().isUnbreakable()) {
                                    short durability = (short) ((Damageable) item.getItemMeta()).getDamage();
                                    short maxDurability = item.getType().getMaxDurability();

                                    if (((durability > maxDurability) || durability < 0)
                                            && ToolUtils.isVanillaTool(item)) {
                                        debug.i("removing item: " + item.getType().name() +
                                                " (durability " + durability + ">" + maxDurability);
                                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                        player.updateInventory();
                                    }
                                }
                            }
                        }

                        event.setCancelled(!checkTreeStructure.getConfig().getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY) ||
                                checkTreeStructure.getConfig().getInt(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY_TIME) <= 0);
                        checkTreeStructure.maybeReplant(player, event.getBlock());
                        if (TreeAssist.instance.config().getBoolean(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE)) {
                            checkTreeStructure.removeBlocksBelow(event.getBlock());
                        }
                        TreeAssist.instance.treeAdd(checkTreeStructure);
                        BlockUtils.callExternals(event.getBlock(), player, true);
                        checkTreeStructure.removeTreeLater(player, item);
                        return;

                    } else if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL) ||
                            config.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {
                        matchingTreeConfig = config;
                        matchingTreeStructure = checkTreeStructure;
                        continue configs;
                    }

                    // we did not find a match or we do not want to force remove it - let's try another!
                }
                debug.i("Shape does not match " + mat + " (" + checkTreeStructure.failReason + ")" );
                if (checkTreeStructure.failReason == FailReason.INVALID_BLOCK) {
                    break configs;
                }
            }
        }

        if (matchingTreeConfig != null) {
            debug.i("Fallback to enforcing something!");
            if (matchingTreeConfig.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {
                matchingTreeStructure.maybeReplant(null, event.getBlock());
            }

            if (matchingTreeConfig.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL)) {
                TreeAssist.instance.treeAdd(matchingTreeStructure);
                if (TreeAssist.instance.config().getBoolean(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE)) {
                    matchingTreeStructure.removeBlocksBelow(event.getBlock());
                }
                BlockUtils.callExternals(event.getBlock(), player, true);
                matchingTreeStructure.removeTreeLater(null, null);
            } else {
                // do we maybe need to place saplings still?
                matchingTreeStructure.plantSaplings();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickUpEvent(EntityPickupItemEvent event) {
        debug.i("Picking up : " + event.getItem().getType() + " >> " + event.getItem().getClass());
        if (event.getItem() instanceof FallingBlock) {
            debug.i("Falling block picked up!");
            if (BlockUtils.removeIfFallen((FallingBlock) event.getItem())) {
                event.setCancelled(true);
                debug.i("Event cancelled!");
            } else {
                debug.i("Event not cancelled!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            debug.i("Falling block despawning!");
            BlockUtils.removeIfFallen((FallingBlock) event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.config().getBoolean(MainConfig.CFG.GENERAL_TOGGLE_DEFAULT)) {
            plugin.toggleGlobal(event.getPlayer().getName());
        }
        if (event.getPlayer().isOp() && plugin.getUpdater() != null) {
            plugin.getUpdater().message(event.getPlayer());
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
        debug.i("onEntityChangeBlock : " + event.getEntityType());
        if (event.getEntity() instanceof FallingBlock) {
            if (BlockUtils.removeIfFallen((FallingBlock) event.getEntity())) {
                debug.i("removing the entity!");
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
    }

    public ItemStack getGrowTool(TreeType treeType) {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(growToolDisplayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(treeType.name());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getProtectionTool() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(protectToolDisplayName);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isGrowTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(growToolDisplayName);
    }

    public boolean isProtectTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(protectToolDisplayName);
    }

    public TreeAssistAntiGrow getAntiGrow() {
        return antiGrow;
    }

    public void noReplant(String name, int seconds) {
        noreplant.put(name, (System.currentTimeMillis() / 1000) + seconds);
    }

    public boolean isNoReplant(String name) {
        if (noreplant.containsKey(name)) {
            if (noreplant.get(name) < System.currentTimeMillis() / 1000) {
                noreplant.remove(name);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
