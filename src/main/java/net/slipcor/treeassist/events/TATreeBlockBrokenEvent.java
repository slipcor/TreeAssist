package net.slipcor.treeassist.events;

import net.slipcor.treeassist.discovery.TreeStructure;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class TATreeBlockBrokenEvent extends Event implements Cancellable {

private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

	protected boolean cancelled;
    protected TreeStructure tree; // the TreeStructure we are dealing with
    protected Block block; // the current Block being broken
    protected Player player; // the Player initiating the breaking
    protected ItemStack tool; // the item the Player held when initiating the breaking

    /**
     * TreeAssist Tree Broken Event
     *
     * @param tree the TreeStructure we discovered
     * @param block the block the player broke
     * @param player the player who broke the tree
     * @param tool the tool the player broke the tree with
     */
    public TATreeBlockBrokenEvent(TreeStructure tree, Block block, Player player, ItemStack tool)
    {
    	super();
    	this.tree = tree;
    	this.block  = block;
    	this.player = player;
    	this.tool   = tool;
    	this.cancelled = false;
    }

    /**
     * @return the block the player broke
     */
    public Block getBlock() {
    	return this.block;
    }

    /**
     * @return the tool the player broke the tree with
     */
    public ItemStack getTool() {
    	return this.tool;
    }

    /**
     * @return the player who broke the tree
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return the TreeStructure we discovered
     */
    public TreeStructure getTree() {
        return this.tree;
    }

    /**
     * @return the TreeStructure config name, e.g. "tall_ungle"
     */
    public String getTreeName() {
        return this.tree.getConfig().getConfigName();
    }

    /**
     * @return whether the event is cancelled
     */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

    /**
     * Set the cancelled state
     * @param value the value to set to
     */
	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}
}
