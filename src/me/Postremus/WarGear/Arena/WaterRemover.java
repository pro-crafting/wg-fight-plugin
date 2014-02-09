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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class WaterRemover implements Listener
{
	private Arena arena;
	private WarGear plugin;
	private List<SimpleEntry<Location, Integer>> explodedBlocks;
	private List<Location> waterList;
	private int taskId;
	private	List<BlockFace> waterFlowCheckDirections;
	
	public WaterRemover(WarGear plugin, Arena arena)
	{
		this.arena = arena;
		this.plugin = plugin;
		explodedBlocks = new ArrayList<SimpleEntry<Location, Integer>>();
		waterList = new ArrayList<Location>();
		taskId = -1;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
		this.waterFlowCheckDirections = new ArrayList<BlockFace>();
		this.waterFlowCheckDirections.add(BlockFace.NORTH);
		this.waterFlowCheckDirections.add(BlockFace.EAST);
		this.waterFlowCheckDirections.add(BlockFace.SOUTH);
		this.waterFlowCheckDirections.add(BlockFace.WEST);
	}
	
	public void start()
	{
		stop();
		explodedBlocks = new ArrayList<SimpleEntry<Location, Integer>>();
		waterList = new ArrayList<Location>();
		this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable(){
			public void run()
			{
				wateredCheck();
				removeWater();
			}
		}, 0, 2);
	}
	
	public void stop()
	{
		if (taskId != -1)
		{
			this.plugin.getServer().getScheduler().cancelTask(taskId);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
    public void entityExplodeHandler(EntityExplodeEvent event)
	{
		for (Block b : event.blockList())
		{
			
			if (b.getType() != Material.WATER || b.getType() != Material.STATIONARY_WATER)
			{
				BlockVector vec = BukkitUtil.toVector(b);
				if (this.arena.getRepo().getTeam1Region().contains(vec) || this.arena.getRepo().getTeam2Region().contains(vec))
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
			if (this.explodedBlocks.get(i).getValue() == 160)
			{
				Location loc = this.explodedBlocks.get(i).getKey();
				Block b = loc.getBlock();
				if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER)
				{
					this.waterList.add(loc);
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
			for (Block removeBlock : getSourceBlocksOfWater(this.waterList.get(i)))
			{
				if (!removeBlock.getChunk().isLoaded())
				{
					removeBlock.getChunk().load();
					continue;
				}
				removeBlock.setType(Material.AIR);
			}
			Location loc = this.waterList.get(i);
			Block b = loc.getBlock();
			if (!b.getChunk().isLoaded())
			{
				b.getChunk().load();
				continue;
			}
			if (b.getType() == Material.AIR)
			{
				this.waterList.remove(i);
			}
		}
	}
	
	private List<Block> getSourceBlocksOfWater(Location loc)
	{
		List<Block> water = new ArrayList<Block>();
		collectBlocks(loc.getBlock(), water, new ArrayList<Block>());
		return water;
	}
	
	
	/* code by: andf54
	 * https://forums.bukkit.org/threads/get-the-whole-stream-of-water-or-lava.110156/
	 * Einige kleinere änderungen vorgenommen
	 */
	public void collectBlocks(Block anchor, List<Block> collected, List<Block> visitedBlocks)
	{
		if(!(anchor.getType() == Material.WATER || anchor.getType() == Material.STATIONARY_WATER)) return;
		
		if(collected.contains(anchor)) return;
		if(anchor.getType() == Material.STATIONARY_WATER)
		{
		   collected.add(anchor);
		}
		if (visitedBlocks.contains(anchor))return;
		visitedBlocks.add(anchor);
		
		Block b = anchor.getRelative(BlockFace.UP);
		collectBlocks(anchor.getRelative(BlockFace.UP), collected, visitedBlocks);
		
		int currLevel = anchor.getData();
		for (BlockFace face : this.waterFlowCheckDirections)
		{
			b = anchor.getRelative(face);
			collectBlocks(b, collected, visitedBlocks);
		}
	}
}
