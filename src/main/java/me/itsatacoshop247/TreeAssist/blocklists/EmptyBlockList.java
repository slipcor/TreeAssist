package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class EmptyBlockList implements BlockList {
	protected int lookupTime;

	public EmptyBlockList() {
		lookupTime = Utils.plugin.getConfig().getInt("Placed Blocks.Handler Lookup Time", 60*60*24);
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
