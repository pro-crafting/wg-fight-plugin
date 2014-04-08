package me.Postremus.Generator;

import org.bukkit.Location;
import org.bukkit.Material;

public interface Job 
{
	public int getMaximumBlockChange();
	public Location getLocationToChange();
	public Material getType();
	public JobState getState();
	public void setState(JobState state);
	public Location getMin();
	public Location getMax();
}
