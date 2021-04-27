package net.slipcor.treeassist.listeners;

import net.slipcor.core.CoreDebugger;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.events.TASaplingPlaceEvent;
import net.slipcor.treeassist.runnables.TreeAssistSaplingSelfPlant;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.Random;

public class TreeAssistSpawnListener implements Listener {

	public TreeAssist plugin;
	public static CoreDebugger debug;
	
	public TreeAssistSpawnListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemSpawnEvent(ItemSpawnEvent event) {
		Item drop = event.getEntity();
		for (String name : TreeAssist.treeConfigs.keySet()) {
			TreeConfig config = TreeAssist.treeConfigs.get(name);
			if (config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL) == drop.getItemStack().getType() ) {
				debug.i("Someone dropped our sapling! - " + name);
				if (!config.getBoolean(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS)) {
					debug.i("But we do not want to plant this anyway");
					return;
				}

				TASaplingPlaceEvent newEvent = new TASaplingPlaceEvent(
						event.getEntity().getLocation().getBlock(),
						drop.getItemStack().getType());
				TreeAssist.instance.getServer().getPluginManager().callEvent(newEvent);
				if (newEvent.isCancelled()) {
					debug.i("TreeAssistSpawnListener.itemSpawnEvent() Sapling Replant was cancelled!");
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
