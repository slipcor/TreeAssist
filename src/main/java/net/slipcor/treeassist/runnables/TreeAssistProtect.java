package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.Location;

public class TreeAssistProtect implements Runnable {
	public Location location;

	/**
	 * A Runnable temporarily preventing the breaking of a sapling
	 *
	 * @param location the location to protect
	 */
	public TreeAssistProtect(Location location) {
		this.location = location;
	}

	@Override
	public void run() {
		if (TreeAssist.instance.isEnabled() && TreeAssist.instance.saplingLocationList.contains(this.location)) {
			TreeAssist.instance.saplingLocationList.remove(this.location);
		}
	}
}
