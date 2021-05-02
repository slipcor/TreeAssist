package net.slipcor.treeassist.listeners;

import net.slipcor.core.CoreDebugger;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.events.TASaplingPlaceEvent;
import net.slipcor.treeassist.runnables.TreeAssistSaplingSelfPlant;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class TreeAssistSpawnListener implements Listener {

	public TreeAssist plugin;
	public static CoreDebugger debug;
	
	public TreeAssistSpawnListener(TreeAssist instance)
	{
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemDropEvent(PlayerDropItemEvent event) {
		Item drop = event.getItemDrop();
		for (String name : TreeAssist.treeConfigs.keySet()) {
			TreeConfig config = TreeAssist.treeConfigs.get(name);
			if (config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL) == drop.getItemStack().getType()) {
				debug.i("[PlayerDrop] Someone dropped our sapling! - " + name);
				if (!config.getBoolean(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS)) {
					debug.i("[PlayerDrop] But we do not want to plant this anyway");
					continue;
				}

				Block landingBlock = event.getPlayer().getLocation().getBlock();
				while (landingBlock.isEmpty()) {
					landingBlock = landingBlock.getRelative(BlockFace.DOWN);
				}
				PlayerInteractEvent interactEvent = new PlayerInteractEvent(event.getPlayer(), Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.BONE_MEAL, 1),
						landingBlock, BlockFace.UP);

				Bukkit.getPluginManager().callEvent(interactEvent);

				if (interactEvent.isCancelled()) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemSpawnEvent(ItemSpawnEvent event) {
		Item drop = event.getEntity();
		for (String name : TreeAssist.treeConfigs.keySet()) {
			TreeConfig config = TreeAssist.treeConfigs.get(name);
			if (config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL) == drop.getItemStack().getType() ) {
				debug.i("[ItemSpawn] Someone dropped our sapling! - " + name);
				if (!config.getBoolean(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS)) {
					debug.i("[ItemSpawn] But we do not want to plant this anyway");
					continue;
				}

				TASaplingPlaceEvent newEvent = new TASaplingPlaceEvent(
						event.getEntity().getLocation().getBlock(),
						drop.getItemStack().getType());
				TreeAssist.instance.getServer().getPluginManager().callEvent(newEvent);
				if (newEvent.isCancelled()) {
					debug.i("[ItemSpawn] Sapling Replant was cancelled!");
					continue;
				}

				if ((new Random()).nextDouble() <
						config.getDouble(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS_PROBABILITY)) {
					new TreeAssistSaplingSelfPlant(config, drop, newEvent.getType());
                }
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerPickupItemEvent(EntityPickupItemEvent event) {
		Item item = event.getItem();
		if (MaterialUtils.isSapling(item.getItemStack().getType())) {
			TreeAssistSaplingSelfPlant.remove(item);
		}
	}
}
