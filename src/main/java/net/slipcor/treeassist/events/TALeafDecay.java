package net.slipcor.treeassist.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TALeafDecay extends Event implements Cancellable {
	
    private static final HandlerList handlers = new HandlerList();
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
	protected boolean cancelled;
	final protected Block block;

	/**
	 * TreeAssist Leaf Decay Event
	 *
	 * @param block the block that is decaying
	 */
	public TALeafDecay(Block block)
	{
		super();
		this.block = block;
		this.cancelled = false;
	}

	/**
	 * @return the block that is decaying
	 */
	public Block getBlock() {
		return this.block;
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
