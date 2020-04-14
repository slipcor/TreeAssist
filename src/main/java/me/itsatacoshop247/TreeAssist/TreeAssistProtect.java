package me.itsatacoshop247.TreeAssist;

import org.bukkit.Location;

public class TreeAssistProtect implements Runnable {
	public final TreeAssist plugin;
	public Location location;
	
	public TreeAssistProtect(TreeAssist instance, Location importLocation)
	{
		this.plugin = instance;
		this.location = importLocation;
	}

	@Override
	public void run() 
	{
		if(plugin.isEnabled() && plugin.saplingLocationList.contains(this.location))
		{
			plugin.saplingLocationList.remove(this.location);
		}
	}
}
