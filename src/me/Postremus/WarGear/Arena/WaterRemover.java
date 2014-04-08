package me.Postremus.WarGear.Arena;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import me.Postremus.WarGear.WarGear;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitTask;

public class WaterRemover implements Listener
{
	private Arena arena;
	private WarGear plugin;
	private List<SimpleEntry<Location, Integer>> explodedBlocks;
	private List<Block> waterList;
	private BukkitTask task;
	
	public WaterRemover(WarGear plugin, Arena arena)
	{
		this.arena = arena;
		this.plugin = plugin;
		explodedBlocks = new ArrayList<SimpleEntry<Location, Integer>>();
		waterList = new ArrayList<Block>();
	}
	
	public void start()
	{
		stop();
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		explodedBlocks = new ArrayList<SimpleEntry<Location, Integer>>();
		waterList = new ArrayList<Block>();
		task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable(){
			public void run()
			{
				wateredCheck();
				removeWater();
			}
		}, 0, 20);
	}
	
	public void stop()
	{
		if (task != null)
		{
			task.cancel();
		}
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
    public void entityExplodeHandler(EntityExplodeEvent event)
	{
		for (Block b : event.blockList())
		{
			if (b.getType() != Material.WATER || b.getType() != Material.STATIONARY_WATER)
			{
				if (this.arena.contains(b.getLocation()))
				{
					this.explodedBlocks.add(new SimpleEntry<Location, Integer>(b.getLocation(), 0));
				}
			}
		}
	}
	
	private void wateredCheck()
	{
		for (int i= this.explodedBlocks.size()-1;i>-1;i--)
		{
			if (this.explodedBlocks.get(i).getValue() >= 15)
			{
				Block b = this.explodedBlocks.get(i).getKey().getBlock();
				if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER)
				{
					this.waterList.add(b);
				}
				this.explodedBlocks.remove(i);
			}
			else
			{
				this.explodedBlocks.get(i).setValue(this.explodedBlocks.get(i).getValue()+1);
			}
		}
	}
	
	private void removeWater()
	{
		for (int i=this.waterList.size()-1;i>-1;i--)
		{
			Block current = this.waterList.get(i);
			for (Block removeBlock : getSourceBlocksOfWater(current))
			{
				removeBlock.setType(Material.AIR);
			}
			if (current.getType() == Material.AIR)
			{
				this.waterList.remove(i);
			}
		}
	}
	
	private List<Block> getSourceBlocksOfWater(Block startBlock)
	{
		List<Block> water = new ArrayList<Block>();
		collectBlocks(startBlock, water, new ArrayList<Block>());
		return water;
	}
	
	
	/* code by: andf54
	 * https://forums.bukkit.org/threads/get-the-whole-stream-of-water-or-lava.110156/
	 * Einige kleinere änderungen vorgenommen
	 */
	public void collectBlocks(Block anchor, List<Block> collected, List<Block> visitedBlocks)
	{
		if(!(anchor.getType() == Material.WATER || anchor.getType() == Material.STATIONARY_WATER)) return;
		
		if (visitedBlocks.contains(anchor))return;
		visitedBlocks.add(anchor);
		if(anchor.getType() == Material.STATIONARY_WATER)
		{
		   collected.add(anchor);
		}
		
		collectBlocks(anchor.getRelative(BlockFace.UP), collected, visitedBlocks);
		collectBlocks(anchor.getRelative(BlockFace.NORTH), collected, visitedBlocks);
		collectBlocks(anchor.getRelative(BlockFace.EAST), collected, visitedBlocks);
		collectBlocks(anchor.getRelative(BlockFace.SOUTH), collected, visitedBlocks);
		collectBlocks(anchor.getRelative(BlockFace.WEST), collected, visitedBlocks);
	}
}
