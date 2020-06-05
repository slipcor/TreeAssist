package net.slipcor.treeassist.listeners;

import java.util.Random;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.Debugger;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.events.TASaplingReplaceEvent;
import net.slipcor.treeassist.runnables.TreeAssistSaplingSelfPlant;
import net.slipcor.treeassist.utils.MaterialUtils;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class TreeAssistSpawnListener implements Listener {

	public TreeAssist plugin;
	public static Debugger debug;
	
	public TreeAssistSpawnListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemSpawnEvent(ItemSpawnEvent event) {
		Item drop = event.getEntity();
		for (TreeConfig config : TreeAssist.treeConfigs.values()) {
			if (config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL) == drop.getItemStack().getType() ) {
				TASaplingReplaceEvent newEvent = new TASaplingReplaceEvent(
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
