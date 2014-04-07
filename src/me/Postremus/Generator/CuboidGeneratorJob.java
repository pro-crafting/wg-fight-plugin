package me.Postremus.Generator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class CuboidGeneratorJob implements GeneratorJob
{
	private int maximumBlockChange;
	private int currX;
	private int currY;
	private int currZ;
	private Material type;
	private Location currLoc;
	private GeneratorJobState jobState = GeneratorJobState.Unstarted;
	private Location min;
	private Location max;
	private String jobName;
	
	public CuboidGeneratorJob(Location min, Location max, Material type, String jobName)
	{
		this.maximumBlockChange = 3000;
		this.type = type;
		currLoc = new Location(min.getWorld(), 0, 0, 0);
		jobState = GeneratorJobState.Unstarted;
		this.min = min;
		this.max = max;
		this.jobName = jobName;
	}
	
	@Override
	public int getMaximumBlockChange() {
		return this.maximumBlockChange;
	}

	@Override
	public Location getBlockLocationToChange() {
		if (this.currX == max.getBlockX() && this.currY == min.getBlockY() && this.currZ == max.getBlockZ())
		{
			this.setState(GeneratorJobState.Finished);
		}
		if (this.currX == 0 && this.currY == 0 && this.currZ == 0)
		{
			this.currX = min.getBlockX();
			this.currY = max.getBlockY();
			this.currZ = min.getBlockZ();
			return this.currLoc;
		}
		for (;currX<max.getBlockX()+1;)
		{
			for (;currY>min.getBlockY()-1;)
			{
				for (;currZ<max.getBlockZ()+1;)
				{
					currLoc.setX(currX);
					currLoc.setY(currY);
					currLoc.setZ(currZ);
					currZ++;
					return this.currLoc;
				}
				currY--;
				currZ=min.getBlockZ();
				return this.currLoc;
			}
			currX++;
			currY=max.getBlockY();
			return this.currLoc;
		}
		this.setState(GeneratorJobState.Finished);
		return this.currLoc;
	}

	@Override
	public Material getType() {
		return this.type;
	}

	@Override
	public GeneratorJobState getState() {
		return this.jobState;
	}

	@Override
	public void setState(GeneratorJobState state) {
		Bukkit.getPluginManager().callEvent(new JobStateChangedEvent(this, this.jobState, state));
		this.jobState = state;
	}

	@Override
	public Location getMin() {
		return this.min;
	}

	@Override
	public Location getMax() {
		return this.max;
	}

	@Override
	public String getJobName() {
		return this.jobName;
	}

}
