package net.slipcor.treeassist.runnables;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.utils.MaterialUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;

public class TreeAssistSaplingSelfPlant implements Runnable {
	private final TreeConfig config;
	private final Material overrideMaterial;
	private Item drop;
	private final static Set<Item> items = new HashSet<>();

	/**
	 * A Runnable to place a sapling item on the ground properly
	 *
	 * @param config    the TreeConfig to check for settings
	 * @param item      the dropped item
	 * @param material  an optionally different override material
	 */
	public TreeAssistSaplingSelfPlant(TreeConfig config, Item item, Material material) {
		this.config = config;
		drop = item;
		items.add(drop);
		overrideMaterial = (drop.getItemStack().getType() == material) ? null : material;
		
		int delay = config.getInt(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS_DELAY, 5);
		if (delay < 1) {
			delay = 1;
		}
		
		Bukkit.getScheduler().runTaskLater(TreeAssist.instance, this, delay);
	}

	@Override
	public void run() {
		if (!items.contains(drop)) {
			return;
		}
		Block block = drop.getLocation().getBlock();

		List<Material> grounds = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);

		if (MaterialUtils.isAir(block.getType()) &&
				(grounds.contains(block.getRelative(BlockFace.DOWN).getType()))) {

			block.setType(overrideMaterial == null ? config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL) : overrideMaterial);
			drop.remove();
		}
	}

	/**
	 * Remove an item from our sapling list
	 *
	 * @param item the item to remove
	 */
	public static void remove(Item item) {
		items.remove(item);
	}
}
