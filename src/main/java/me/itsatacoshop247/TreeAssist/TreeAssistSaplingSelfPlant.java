package me.itsatacoshop247.TreeAssist;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.itsatacoshop247.TreeAssist.core.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;

import me.itsatacoshop247.TreeAssist.core.Utils;

public class TreeAssistSaplingSelfPlant implements Runnable {
	private final TreeAssist plugin;
	private final TreeConfig config;
	private Item drop;
	private final static Set<Item> items = new HashSet<>();
	
	public TreeAssistSaplingSelfPlant(TreeAssist instance, TreeConfig config, Item item)
	{
		this.plugin = instance;
		this.config = config;
		drop = item;
		items.add(drop);
		
		int delay = config.getInt(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS_DELAY, 5);
		if (delay < 1) {
			delay = 1;
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, this, 20L * delay);
	}

	@Override
	public void run() 
	{
		if (!items.contains(drop)) {
			return;
		}
		Block block = drop.getLocation().getBlock();

		List<Material> grounds = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);

		if ((Utils.isAir(block.getType()) || block.getType() == Material.SNOW) &&
				(grounds.contains(block.getRelative(BlockFace.DOWN).getType()))) {

			block.setType(config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL));
			drop.remove();
		}
	}
	
	public static void remove(Item item) {
		items.remove(item);
	}
}
