package me.Postremus.Generator;

import org.bukkit.Location;
import org.bukkit.Material;

public class CuboidJob implements Job
{
	private int currX;
	private int currY;
	private int currZ;
	private Material type;
	private Location currLoc;
	private JobState jobState = JobState.Unstarted;
	private Location min;
	private Location max;
	private JobStateChangedCallback callback;
	
	public CuboidJob(Location min, Location max, Material type, JobStateChangedCallback callback)
	{
		this.type = type;
		currLoc = new Location(min.getWorld(), 0, 0, 0);
		jobState = JobState.Unstarted;
		this.min = min;
		this.max = max;
		this.callback = callback;
	}

	@Override
	public Location getLocationToChange() {
		if (this.currX == max.getBlockX() && this.currY == min.getBlockY() && this.currZ == max.getBlockZ())
		{
			this.setState(JobState.Finished);
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
		this.setState(JobState.Finished);
		return this.currLoc;
	}

	@Override
	public Material getType() {
		return this.type;
	}

	@Override
	public JobState getState() {
		return this.jobState;
	}

	@Override
	public void setState(JobState state) {
		JobState from = this.jobState;
		this.jobState = state;
		this.callback.jobStateChanged(this, from);
	}

	@Override
	public Location getMin() {
		return this.min;
	}

	@Override
	public Location getMax() {
		return this.max;
	}
}
