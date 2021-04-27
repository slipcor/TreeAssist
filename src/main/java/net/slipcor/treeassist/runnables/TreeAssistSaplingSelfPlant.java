package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		int radius = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);

		List<TreeStructure> trees = new ArrayList<>(TreeAssist.instance.treesThatQualify(config, item.getLocation().getBlock(), radius * radius));

		for (TreeStructure tree : trees) {
			if (tree.isValid()) {
				tree.addReplantDelay(new TreeAssistReplantDelay(tree, findBlock(item), this, delay));
				return;
			}
		}
		
		Bukkit.getScheduler().runTaskLater(TreeAssist.instance, this, delay);
	}

	private Block findBlock(Item item) {
		Block result = item.getLocation().getBlock();
		while (result.getY() > 1 && !config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS).contains(result.getType()) || !result.getType().isSolid()) {
			result = result.getRelative(BlockFace.DOWN);
		}
		if (result.getY() <= 1) {
			TreeStructure.debug.i("we did not find a valid block");
			return item.getLocation().getBlock();
		}
		TreeStructure.debug.i("we went down to " + BlockUtils.printBlock(result));
		return result.getRelative(BlockFace.UP);
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
