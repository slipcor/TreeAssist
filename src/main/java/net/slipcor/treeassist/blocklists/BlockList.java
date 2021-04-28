package net.slipcor.treeassist.blocklists;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * An interface to deal with player placed blocks
 */
public interface BlockList {
	/**
	 * Prepare the list to be ready for additions
	 */
	void initiate();

	/**
	 * @param block the block to check
	 * @return whether this block has been placed by a player
	 */
	boolean isPlayerPlaced(Block block);

	/**
	 * Add a block to the list
	 * @param block the block to add
	 */
	void addBlock(Block block);

	/**
	 * Remove a block from the list
	 * @param block the block to remove
	 */
	void removeBlock(Block block);

	/**
	 * Save the block list
	 */
	void save();

	/**
	 * Save the block list with optional enforcing
	 * @param force enforce the saving
	 */
	void save(boolean force);

	/**
	 * Handle a block being broken by a player
	 * @param block the block being broken
	 * @param player the player who broke it
	 */
	void logBreak(Block block, Player player);
}
