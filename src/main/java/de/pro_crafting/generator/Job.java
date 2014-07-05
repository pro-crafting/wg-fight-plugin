package de.pro_crafting.generator;

import org.bukkit.Location;
import org.bukkit.Material;

public interface Job 
{
	public Location getLocationToChange();
	public Material getType();
	public JobState getState();
	public void setState(JobState state);
}
