package me.itsatacoshop247.TreeAssist.events;

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
	
    public TATreeBrokenEvent(Block block, Player player, ItemStack tool)
    {
    	super();
    	this.block  = block;
    	this.player = player;
    	this.tool   = tool;
    	this.cancelled = false;
    }
    
    public Block getBlock() {
    	return this.block;
    }
    
    public ItemStack getTool() {
    	return this.tool;
    }
    
    public Player getPlayer() {
    	return this.player;
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
