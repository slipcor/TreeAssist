package net.slipcor.treeassist.listeners;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeAssistDebugger;
import net.slipcor.treeassist.events.TASaplingPlaceEvent;
import net.slipcor.treeassist.runnables.TreeAssistSaplingSelfPlant;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Random;

public class TreeAssistSpawnListener implements Listener {

	public TreeAssist plugin;
	public static TreeAssistDebugger debug;
	
	public TreeAssistSpawnListener(TreeAssist instance)
	{
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemDropEvent(PlayerDropItemEvent event) {
		if (!plugin.isActive(event.getPlayer().getWorld())) {
			debug.i("not in this world: " + event.getPlayer().getWorld().getName());
			return;
		}
		Item drop = event.getItemDrop();
		for (String name : TreeAssist.treeConfigs.keySet()) {
			TreeConfig config = TreeAssist.treeConfigs.get(name);
			if (config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL) == drop.getItemStack().getType()) {
				debug.i("[PlayerDrop] Someone dropped our sapling! - " + name);
				if (!config.getBoolean(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS)) {
					debug.i("[PlayerDrop] But we do not want to plant this anyway");
					continue;
				}

				drop.setMetadata("dropper", new FixedMetadataValue(plugin, event.getPlayer()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemSpawnEvent(ItemSpawnEvent event) {
		if (!plugin.isActive(event.getEntity().getWorld())) {
			debug.i("not in this world: " + event.getEntity().getWorld().getName());
			return;
		}
		Item drop = event.getEntity();
		Material type = drop.getItemStack().getType();
		if (type.name().contains("BAMBOO") || type.name().equals("LEAVES") || type.name().equals("VINE")) {
			// let's ignore the spammy bits that are known and no saplings anyway
			return;
		}
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

					if (event.getEntity().hasMetadata("dropper")) {

						List<MetadataValue> meta = event.getEntity().getMetadata("dropper");

						if (meta.size() > 0 && meta.get(0).value() instanceof Player) {
							Player player = (Player) meta.get(0).value();

							Block landingBlock = player.getLocation().getBlock();
							while (landingBlock.isEmpty()) {
								landingBlock = landingBlock.getRelative(BlockFace.DOWN);
							}
							PlayerInteractEvent interactEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.BONE_MEAL, 1),
									landingBlock, BlockFace.UP);

							Bukkit.getPluginManager().callEvent(interactEvent);

							if (interactEvent.isCancelled()) {
								return;
							}
						}
					}

					new TreeAssistSaplingSelfPlant(config, drop, newEvent.getType());
					break;
                }
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerPickupItemEvent(EntityPickupItemEvent event) {
		if (!plugin.isActive(event.getItem().getWorld())) {
			debug.i("not in this world: " + event.getItem().getWorld().getName());
			return;
		}

		Item item = event.getItem();
		if (MaterialUtils.isSapling(item.getItemStack().getType())) {
			TreeAssistSaplingSelfPlant.remove(item);
		}
	}
}
