package de.hrc_gaming.generator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockGenerator 
{
	private JavaPlugin plugin;
	private List<Job> jobs;
	private int taskId;
	private int maxBlockChange;
	
	public BlockGenerator(JavaPlugin plugin, int maxBlockChange)
	{
		this.plugin = plugin;
		this.jobs = new ArrayList<Job>();
		taskId = -1;
		this.maxBlockChange = maxBlockChange;
	}
	
	private void changeBlocks()
	{
		int changedBlocks = 0;
		for (int i=jobs.size()-1;i>-1;i--)
		{
			Job job = jobs.get(i);
			while (changedBlocks<this.maxBlockChange&&job.getState()!=JobState.Finished&&job.getState()!=JobState.Paused)
			{
				Block b = job.getLocationToChange().getBlock();
				b.setType(job.getType());
				changedBlocks++;
			}
			if (job.getState() == JobState.Finished)
			{
				jobs.remove(i);
			}
			if (changedBlocks>=this.maxBlockChange)
			{
				break;
			}
		}
		if (this.jobs.size() == 0)
		{
			this.plugin.getServer().getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}
	
	private void startTask()
	{
		if (taskId == -1)
		{
			this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable(){
				public void run()
				{
					BlockGenerator.this.changeBlocks();
				}
			}, 0, 1);
		}
	}
	
	public void addJob(Job job)
	{
		job.setState(JobState.Started);
		this.jobs.add(job);
		startTask();
	}
}
