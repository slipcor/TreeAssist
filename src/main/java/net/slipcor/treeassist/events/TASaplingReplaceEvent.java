package net.slipcor.treeassist.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TASaplingReplaceEvent extends Event implements Cancellable {

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
     * TreeAssist Sapling Replace Event
     *
     * @param block the block that will be replaced
     * @param type the material we will replace
     */
    public TASaplingReplaceEvent(Block block, Material type)
    {
    	super();
    	this.block  = block;
        this.type = type;
    	this.cancelled = false;
    }

    /**
     * @return the block that will be replaced
     */
    public Block getBlock() {
    	return this.block;
    }

    /**
     * @return the material we will replace with
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

    /**
     * Set the replacement material
     * @param value the value to set to
     */
	public void setMaterial(Material value) {
        type = value;
    }

}
