package net.slipcor.treeassist.blocklists;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.MainConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * This class basically does nothing apart from preparing the lookup time. It remains empty and can be overridden
 */
public class EmptyBlockList implements BlockList {
	protected int lookupTime;

	public EmptyBlockList() {
		lookupTime = TreeAssist.instance.config().getInt(MainConfig.CFG.PLACED_BLOCKS_LOOKUP_TIME, 60*60*24);
	}

	@Override
	public void initiate() {}

	@Override
	public boolean isPlayerPlaced(Block block) {
		return false;
	}

	@Override
	public void addBlock(Block block) {
	}

	@Override
	public void removeBlock(Block block) {
	}

	@Override
	public void save() {
	}

	@Override
	public void save(boolean force) {
	}

	@Override
	public void logBreak(Block block, Player player) {
	}

}
