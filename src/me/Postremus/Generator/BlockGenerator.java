package me.Postremus.Generator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockGenerator 
{
	private JavaPlugin plugin;
	private int currIdx;
	private List<IGeneratorJob> jobs;
	private int taskId;
	
	public BlockGenerator(JavaPlugin plugin)
	{
		this.plugin = plugin;
		this.jobs = new ArrayList<IGeneratorJob>();
		taskId = -1;
	}
	
	private void changeBlocks()
	{
		if (this.jobs.size() == 0)
		{
			this.plugin.getServer().getScheduler().cancelTask(taskId);
			taskId = -1;
			return;
		}
		if (currIdx == 0)
		{
			currIdx = this.jobs.size()-1;
		}
		IGeneratorJob job = this.jobs.get(currIdx);
		if (job.getState() == GeneratorJobState.Finished)
		{
			this.jobs.remove(currIdx);
			currIdx = 0;
			return;
		}
		if (job.getState() == GeneratorJobState.Paused)
		{
			return;
		}
		for (int i=0;i<job.getMaximumBlockChange();i++)
		{
			Block b = job.getBlockLocationToChange().getBlock();
			//Kisten werden so oder so drops liefern
			//Da die drops nicht gewünscht sind, werden kisten, dropper, etc. zuerst geleert.
			if (b instanceof InventoryHolder)
			{
				((InventoryHolder)b).getInventory().clear();
				((BlockState)b).update(true);
			}
			b.setType(job.getType());
		}
		if (currIdx > 0)
		{
			currIdx--;
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
	
	public void addJob(IGeneratorJob job)
	{
		job.setState(GeneratorJobState.Started);
		this.jobs.add(job);
		startTask();
	}
}
