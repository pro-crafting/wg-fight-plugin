package me.Postremus.Generator;

import org.bukkit.Location;
import org.bukkit.Material;

public interface IGeneratorJob 
{
	public int getMaximumBlockChange();
	public Location getBlockLocationToChange();
	public Material getType();
	public GeneratorJobState getState();
	public void setState(GeneratorJobState state);
	public Location getMin();
	public Location getMax();
	public String getJobName();
}
