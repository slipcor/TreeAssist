package net.slipcor.treeassist.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TASaplingBreakEvent extends Event implements Cancellable {

private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    final protected Block block;
	protected boolean cancelled;
    protected Material type;

    /**
     * TreeAssist Sapling Break Event
     *
     * @param block the block that will be placed
     * @param type the material we will place
     */
    public TASaplingBreakEvent(Block block, Material type)
    {
    	super();
    	this.block  = block;
        this.type = type;
    	this.cancelled = false;
    }

    /**
     * @return the block that will be placed
     */
    public Block getBlock() {
    	return this.block;
    }

    /**
     * @return the material we will break
     */
    public Material getType() {
        return this.type;
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
