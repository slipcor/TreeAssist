package net.slipcor.treeassist.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class TATreeBrokenEvent extends Event implements Cancellable {

private static final HandlerList handlers = new HandlerList();
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
	protected boolean cancelled;
    protected Block   block;
    protected Player  player;
    protected ItemStack tool;

    /**
     * TreeAssist Tree Broken Event
     *
     * @param block the block the player broke
     * @param player the player who broke the tree
     * @param tool the tool the player broke the tree with
     */
    public TATreeBrokenEvent(Block block, Player player, ItemStack tool)
    {
    	super();
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
