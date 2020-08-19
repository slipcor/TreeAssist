package net.slipcor.treeassist.listeners;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.MainConfig;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.runnables.TreeAssistAntiGrow;
import net.slipcor.treeassist.runnables.TreeAssistReplant;
import net.slipcor.treeassist.core.*;
import net.slipcor.treeassist.events.TASaplingReplaceEvent;
import net.slipcor.treeassist.externals.mcMMOHook;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.utils.ToolUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Tree;

import java.util.*;

public class TreeAssistBlockListener implements Listener {
    public static Debugger debug;

    public TreeAssist plugin;

    private final TreeAssistAntiGrow antiGrow;
    private final Map<String, Long> noreplant = new HashMap<>();
    private final String protectToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Protect";

    public TreeAssistBlockListener(TreeAssist instance) {
        plugin = instance;
        antiGrow = new TreeAssistAntiGrow();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (plugin.getMainConfig().getBoolean(MainConfig.CFG.DESTRUCTION_FAST_LEAF_DECAY) && plugin.Enabled) {
            Block block = event.getBlock();
            World world = block.getWorld();
            if (plugin.isActive(world)) {
                for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                    if (config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS).contains(block.getType())) {
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
                        event.getPlayer().sendMessage(
                                Language.parse(Language.MSG.SUCCESSFUL_PROTECT_OFF));
                    } else {
                        plugin.saplingLocationList.add(clicked.getLocation());
                        event.getPlayer().sendMessage(
                                Language.parse(Language.MSG.SUCCESSFUL_PROTECT_ON));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getMainConfig().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE) &&
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

        if (!TreeStructure.allTrunks.contains(block.getType())) {
            debug.i("Not a tree block: " + block.getType());
            return;
        }

        if (plugin.getMainConfig().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
            if (plugin.blockList.isPlayerPlaced(block)) {
                debug.i("User placed block. Removing!");
                plugin.blockList.removeBlock(block);
                plugin.blockList.save();
                return;
            }
        }

        for (TreeConfig config : TreeAssist.treeConfigs.values()) {
            List<String> list = config.getStringList(TreeConfig.CFG.TRUNK_MATERIALS, new ArrayList<>());
            for (String matName : list) {
                Material mat = Material.matchMaterial(matName);
                debug.i("checking for material " + mat);
                if (block.getType().equals(mat)) {


                    if (config.getBoolean(TreeConfig.CFG.REPLANTING_WHEN_TREE_BURNS_DOWN) && plugin.Enabled) {
                        if (plugin.isActive(block.getWorld())) {
                            if (block.getState().getData() instanceof Tree) {
                                Material logMat = block.getType();
                                Block oneBelow = block.getRelative(BlockFace.DOWN, 1);
                                Block oneAbove = block.getRelative(BlockFace.UP, 1);
                                List<Material> grounds = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);
                                if (grounds.contains(oneBelow.getType())) {
                                    Material replantMat = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);
                                    if (MaterialUtils.isAir(oneAbove.getType()) || oneAbove.getType() == logMat) {
                                        TASaplingReplaceEvent event = new TASaplingReplaceEvent(block, replantMat);
                                        TreeAssist.instance.getServer().getPluginManager().callEvent(event);
                                        if (event.isCancelled()) {
                                            debug.i("TreeAssistBlockListener.checkFire() Sapling Replant was cancelled!");
                                            return;
                                        }
                                        Runnable b = new TreeAssistReplant(block, event.getType(), config);
                                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
                                    }
                                }
                            }
                        } else {
                            debug.i("not in this world: " + block.getWorld().getName());
                        }
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
        if (!plugin.isActive(event.getBlock().getWorld())) {
            debug.i("not in this world: " + event.getBlock().getWorld().getName());
            return;
        }
        if (plugin.saplingLocationList.contains(event.getBlock().getLocation())) {
            event.getPlayer().sendMessage(Language.parse(Language.MSG.INFO_SAPLING_PROTECTED));
            event.setCancelled(true);
            return;
        }

        if (!TreeStructure.allTrunks.contains(event.getBlock().getType())) {
            debug.i("Not a tree block: " + event.getBlock().getType());

            if (MaterialUtils.isSapling(event.getBlock().getType())) {
                for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                    if (event.getBlock().getType() == config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL)) {
                        if (config.getBoolean(TreeConfig.CFG.REPLANTING_FORCE_PROTECT)) {
                            event.getPlayer().sendMessage(Language.parse(Language.MSG.INFO_NEVER_BREAK_SAPLINGS));
                            event.setCancelled(true);
                        }
                    }
                }
            }

            return;
        }

        if (plugin.getMainConfig().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
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

        configs: for (TreeConfig config : TreeAssist.treeConfigs.values()) {
            List<String> list = config.getStringList(TreeConfig.CFG.TRUNK_MATERIALS, new ArrayList<>());
            for (String matName : list) {
                Material mat = Material.matchMaterial(matName);
                debug.i("checking for material " + mat);
                if (event.getBlock().getType().equals(mat)) {
                    Block block = TreeStructure.findBottomBlock(event.getBlock(), config);
                    if (block != null) {
                        debug.i("Tree found for Material " + matName);

                        TreeStructure trunk = new TreeStructure(config, block, false);

                        if (trunk.isValid()) {
                            debug.i("Tree matches " + matName);

                            if (plugin.getMainConfig().getBoolean(MainConfig.CFG.GENERAL_USE_PERMISSIONS) &&
                                !player.hasPermission(config.getString(TreeConfig.CFG.PERMISSION))) {
                                debug.i("Player does not have permission " + config.getString(TreeConfig.CFG.PERMISSION));
                                matchingTreeConfig = config; // for maybe forcing something later
                                foundTree = trunk;
                                break;
                            }

                            if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_ACTIVE)) {
                                if (TreeAssist.instance.hasCoolDown(player)) {
                                    debug.i("Cooldown!");
                                    player.sendMessage(Language.parse(Language.MSG.INFO_COOLDOWN_STILL));
                                    player.sendMessage(Language.parse(Language.MSG.INFO_COOLDOWN_VALUE, String.valueOf(TreeAssist.instance.getCoolDown(player))));
                                    matchingTreeConfig = config; // for maybe forcing something later
                                    foundTree = trunk;
                                    break configs; // no need to keep checking
                                }

                                if (plugin.isDisabled(player.getWorld().getName(), player.getName())) {
                                    debug.i("Disabled for this player in this world!");
                                    matchingTreeConfig = config; // for maybe forcing something later
                                    foundTree = trunk;
                                    break configs; // no need to keep checking
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
                                    if (!config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_WHEN_SNEAKING)) {
                                        debug.i("Sneaking is bad!");
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                } else {
                                    if (!config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_WHEN_NOT_SNEAKING)) {
                                        debug.i("Not sneaking is bad!");
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                }

                                if (TreeAssist.instance.mcMMO && mcMMOHook.mcMMOTreeFeller(player)) {
                                    debug.i("mcMMO Tree Feller!");
                                    matchingTreeConfig = config; // for maybe forcing something later
                                    foundTree = trunk;
                                    break;
                                }

                                if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRES_TOOLS)) {
                                    if (!ToolUtils.isMatchingTool(player.getInventory().getItemInMainHand(), config)) {
                                        debug.i("Player has not the right tool!");
                                        matchingTreeConfig = config; // for maybe forcing something later
                                        foundTree = trunk;
                                        break;
                                    }
                                }

                                debug.i("success!");
                                ItemStack item = player.getInventory().getItemInMainHand();

                                if (!plugin.getMainConfig().getBoolean(MainConfig.CFG.MODDING_DISABLE_DURABILITY_FIX)) {
                                    if (item.hasItemMeta()) {
                                        short durability = (short)((Damageable)item.getItemMeta()).getDamage();
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

                                event.setCancelled(true);
                                trunk.maybeReplant(player, event.getBlock());
                                if (TreeAssist.instance.getMainConfig().getBoolean(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE)) {
                                    trunk.removeBlocksBelow(event.getBlock());
                                }
                                TreeAssist.instance.treeAdd(trunk);
                                trunk.removeTreeLater(player, item);
                                return;

                            } else if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL) ||
                                        config.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {
                                matchingTreeConfig = config;
                                foundTree = trunk;
                                break;
                            }

                            // else:  we did not find a match or we do not want to force remove it - let's try another!
                        }
                        debug.i("Shape does not match " + matName + (trunk.failReason) );
                        if (trunk.failReason == FailReason.INVALID_BLOCK) {
                            break configs; // do not try to find a different tree!
                        }
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
                TreeAssist.instance.treeAdd(foundTree);
                if (TreeAssist.instance.getMainConfig().getBoolean(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE)) {
                    foundTree.removeBlocksBelow(event.getBlock());
                }
                foundTree.removeTreeLater(null, null);
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
        if (!plugin.getMainConfig().getBoolean(MainConfig.CFG.GENERAL_TOGGLE_DEFAULT)) {
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
        debug.i("onEntityChangeBlock : " + event.getEntityType());
        if (event.getEntity() instanceof FallingBlock) {
            if (BlockUtils.removeIfFallen((FallingBlock) event.getEntity())) {
                debug.i("removing the entity!");
                event.setCancelled(true);
                event.getEntity().remove();
            }
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
