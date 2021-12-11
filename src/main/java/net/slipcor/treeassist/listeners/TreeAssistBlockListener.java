package net.slipcor.treeassist.listeners;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeAssistDebugger;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.events.TASaplingPlaceEvent;
import net.slipcor.treeassist.runnables.TreeAssistAntiGrow;
import net.slipcor.treeassist.runnables.TreeAssistReplant;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.MainConfig;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeAssistBlockListener implements Listener {
    public static TreeAssistDebugger debug;

    public TreeAssist plugin;

    private final TreeAssistAntiGrow antiGrow;
    private final Map<String, Long> noreplant = new HashMap<>();
    private final Map<String, Long> replant = new HashMap<>();

    public TreeAssistBlockListener(TreeAssist instance) {
        plugin = instance;
        antiGrow = new TreeAssistAntiGrow();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        if (!plugin.isActive(block.getWorld())) {
            debug.i("not in this world: " + block.getWorld().getName());
            debug.explain(TreeAssistDebugger.ErrorType.DECAY, block, "Block is not decaying because it is disabled in this world!");
            return;
        }
        int decaying = 0;
        debug.i("leaf is decaying: " + BlockUtils.printBlock(event.getBlock()));
        if (plugin.config().getBoolean(MainConfig.CFG.DESTRUCTION_FAST_LEAF_DECAY) && plugin.Enabled) {
            decaying++;
            debug.i("we want fast decay!");
            for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                if (config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS).contains(block.getType())) {
                    debug.i("let's go!");
                    debug.explain(TreeAssistDebugger.ErrorType.DECAY, block, ChatColor.GREEN + "We should be decaying!");
                    BlockUtils.breakRadiusLeaves(block, config);
                    return;
                }
            }
        } else {
            debug.explain(TreeAssistDebugger.ErrorType.DECAY, block, "Block is not decaying the main config setting is not enabled!");
            return;
        }
        if (decaying > 0) {
            debug.explain(TreeAssistDebugger.ErrorType.DECAY, block, "Block is not decaying because of the " + decaying + " decaying configs, none matches!");
        } else {
            debug.explain(TreeAssistDebugger.ErrorType.DECAY, block, "Block is not decaying because no matching tree was found!");
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
            debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block is not handled in this world!");
            return;
        }

        if (!TreeStructure.allTrunks.contains(block.getType())) {
            debug.i("Not a burning tree block: " + block.getType());
            debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block is not a recognized log block!");
            return;
        }

        if (plugin.config().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
            if (plugin.blockList.isPlayerPlaced(block)) {
                debug.i("User placed block. Removing!");
                debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block is placed by player!");
                plugin.blockList.removeBlock(block);
                plugin.blockList.save();
                return;
            }
        }

        int burning = 0;

        for (TreeConfig config : TreeAssist.treeConfigs.values()) {
            List<Material> list = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);
            for (Material mat : list) {
                debug.i("checking for material " + mat + "(" + config.getConfigName() +")");
                if (!block.getType().equals(mat)) {
                    continue;
                }
                burning++;

                if (!config.getBoolean(TreeConfig.CFG.REPLANTING_WHEN_TREE_BURNS_DOWN)) {
                    debug.i("burn replanting disabled in config");
                    debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block does not replant config " + config.getConfigName() + "!");
                    continue;
                }

                Block oneBelow = block.getRelative(BlockFace.DOWN, 1);
                List<Material> grounds = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);
                if (!grounds.contains(oneBelow.getType())) {
                    debug.i("not a valid ground: " + BlockUtils.printBlock(oneBelow));
                    debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block does not have valid ground for config " + config.getConfigName() + "!");
                    continue;
                }
                Block oneAbove = block.getRelative(BlockFace.UP, 1);
                if (!MaterialUtils.isAir(oneAbove.getType()) && !list.contains(oneAbove.getType())) {
                    debug.i("not a valid block above: " + BlockUtils.printBlock(oneAbove));
                    debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block does not have valid log block config " + config.getConfigName() + "!");
                    continue;
                }
                Material replantMat = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);
                TASaplingPlaceEvent event = new TASaplingPlaceEvent(block, replantMat);
                TreeAssist.instance.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    debug.i("TreeAssistBlockListener.checkFire() Sapling Replant was cancelled!");
                    debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block sapling replacement was cancelled by other plugin!");
                    return;
                }
                Runnable b = new TreeAssistReplant(block, event.getType(), config);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
                debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, ChatColor.GREEN + "Burning block sapling replacement should have worked!");
                return;
            }
        }
        if (burning > 0) {
            debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block sapling replacement did not trigger because of the " + burning + " matching configs, none has the setting activated!");
        } else {
            debug.explain(TreeAssistDebugger.ErrorType.SAPLING, block, "Burning block sapling replacement did not trigger because no config is set to decay!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!plugin.isActive(event.getEntity().getWorld())) {
            debug.i("not in this world: " + event.getEntity().getWorld().getName());
            return;
        }
        if (event.getEntity() instanceof FallingBlock) {
            debug.i("Falling block despawning!");
            BlockUtils.removeIfFallen((FallingBlock) event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (!plugin.isActive(event.getWorld())) {
            debug.i("not in this world: " + event.getWorld().getName());
            return;
        }
        if (antiGrow.contains(event.getLocation())) {
            debug.explain(TreeAssistDebugger.ErrorType.GROW, event.getLocation().getBlock(), "We are preventing growth for a limited time!");
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent event) {
        if (!plugin.isActive(event.getBlock().getWorld())) {
            debug.i("not in this world: " + event.getBlock().getWorld().getName());
            return;
        }
        debug.i("onEntityChangeBlock : " + event.getEntityType());
        if (event.getEntity() instanceof FallingBlock) {
            if (BlockUtils.removeIfFallen((FallingBlock) event.getEntity())) {
                debug.i("removing the entity!");
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
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

    public void replant(String name, int seconds) {
        replant.put(name, (System.currentTimeMillis() / 1000) + seconds);
    }

    public boolean isReplant(String name) {
        if (replant.containsKey(name)) {
            if (replant.get(name) < System.currentTimeMillis() / 1000) {
                replant.remove(name);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
