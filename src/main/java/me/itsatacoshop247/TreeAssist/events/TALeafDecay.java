package me.itsatacoshop247.TreeAssist.events;

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
	protected Block block;
	
	public TALeafDecay(Block block)
	{
		super();
		this.block = block;
		this.cancelled = false;
	}
	
	public Block getBlock() {
		return this.block;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		cancelled = arg0;
	}

}
