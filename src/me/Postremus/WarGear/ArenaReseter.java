package me.Postremus.WarGear;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ArenaReseter 
{
	private Arena arena;
	private WarGear plugin;
	private int currXChange;
	private int currZChange;
	private int taskid;
	private List<ProtectedRegion> regionsList;
	private int regionIdx;
	private int groundHeight;
	private World arenaWorld;
	
	public ArenaReseter(WarGear plugin, Arena arena)
	{
		this.arena = arena;
		this.plugin = plugin;
		currXChange = Integer.MIN_VALUE+1;
		currZChange = Integer.MIN_VALUE+1;
		taskid = -1;
		groundHeight = this.plugin.getRepo().getGroundHeight(arena);
		arenaWorld = this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(arena));
	}
	
	public void reset()
	{
		regionIdx = 0;
		regionsList = new ArrayList<ProtectedRegion>();
		
		WorldGuardPlugin worldGuard = this.plugin.getRepo().getWorldGuard();
		regionsList.add(getRegion(worldGuard, this.plugin.getRepo().getRegionNameTeam1(arena), arenaWorld));
		regionsList.add(getRegion(worldGuard, this.plugin.getRepo().getRegionNameTeam2(arena), arenaWorld));
		
		if (taskid != -1)
		{
			this.plugin.getServer().getScheduler().cancelTask(taskid);
		}
		
		taskid = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
		{
			public void run() 
			{
				if (clear(arenaWorld, ArenaReseter.this.regionsList.get(ArenaReseter.this.regionIdx)))
				{
					if (ArenaReseter.this.regionIdx == 1)
					{
						ArenaReseter.this.stopClear();
					}
					else
					{
						ArenaReseter.this.regionIdx = 1;
					}
				}
			}
		}, 0, 1);
		
		try {
			this.pasteGround(arenaWorld);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeItems(arenaWorld, regionsList.get(0), regionsList.get(1));
	}
	
	private void stopClear()
	{
		this.plugin.getServer().getScheduler().cancelTask(taskid);
		removeItems(arenaWorld, regionsList.get(0), regionsList.get(1));
	}
	
	private boolean clear(World arenaWorld, ProtectedRegion region)
	{	
		if (currXChange > region.getMaximumPoint().getBlockX())
		{
			currXChange = Integer.MIN_VALUE +1;
			currZChange = Integer.MIN_VALUE +1;
			return true;
		}
		if (currXChange < region.getMinimumPoint().getBlockX())
		{
			currXChange = region.getMinimumPoint().getBlockX();
		}
		for (currZChange = region.getMinimumPoint().getBlockZ();currZChange<=region.getMaximumPoint().getBlockZ();currZChange++)
		{
			for (int y=groundHeight;y<=region.getMaximumPoint().getBlockY();y++)
			{
				Block b = new Location(arenaWorld, currXChange, y, currZChange).getBlock();
				if (!b.getChunk().isLoaded())
				{
					b.getChunk().load();
					return false;
				}
				b.setType(Material.AIR);
			}
		}
		currXChange++;
		return false;
	}
	
	private void pasteGround(World arenaWorld) throws FilenameException, IOException, DataException, MaxChangedBlocksException
	{
	    WorldEditPlugin wePlugin = this.plugin.getRepo().getWorldEdit();
        LocalConfiguration config = wePlugin.getLocalConfiguration();
        LocalPlayer player = wePlugin.wrapCommandSender(this.plugin.getServer().getConsoleSender());
        
        File dir = wePlugin.getWorldEdit().getWorkingDirectoryFile(config.saveDir);
        String schemName = this.plugin.getRepo().getGroundSchematicName(this.arena);
        File f = wePlugin.getWorldEdit().getSafeOpenFile(player, dir, schemName, "schematic", "schematic");
    
        EditSession es = new EditSession(new BukkitWorld(arenaWorld), 999999999);
        CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(f);
        Vector calculatedOrigin = cc.getOrigin();
        calculatedOrigin.add(cc.getOffset());
        cc.setOffset(new Vector());
        cc.paste(es, calculatedOrigin, false, true);
	}
	
	private ProtectedRegion getRegion(WorldGuardPlugin worldGuard, String regionName, World world)
	{
		RegionManager regionManager = worldGuard.getRegionManager(world);
		return regionManager.getRegion(regionName);
	}
	
	private void removeItems(World arenaWorld, ProtectedRegion regionTeam1, ProtectedRegion regionTeam2)
	{
		List<BlockVector> vectors = new ArrayList<BlockVector>();
		vectors.add(regionTeam1.getMinimumPoint());
		vectors.add(regionTeam2.getMinimumPoint());
		vectors.add(regionTeam1.getMaximumPoint());
		vectors.add(regionTeam2.getMaximumPoint());
		BlockVector min = getMinBlockVec(vectors);
		BlockVector max = getMaxBlockVec(vectors);
		for (Entity curr : arenaWorld.getEntitiesByClass(Item.class))
		{
			if (curr.getLocation().getBlockX() > min.getBlockX() &&
					curr.getLocation().getBlockX() < max.getBlockX() &&
					curr.getLocation().getBlockZ() > min.getBlockZ() &&
					curr.getLocation().getBlockZ() < max.getBlockZ())
			{
				curr.remove();
			}
		}
	}
	
	private BlockVector getMinBlockVec(List<BlockVector> toCheck)
	{
		if (toCheck.size() == 0)
		{
			return null;
		}
		BlockVector ret = toCheck.get(0);
		int minY = 256;
		for (BlockVector current : toCheck)
		{
			if (current.getBlockY() < minY)
			{
				minY = current.getBlockY();
			}
			if (current.getBlockX() <= ret.getBlockX() &&
					current.getBlockZ() <= ret.getBlockZ())
			{
				ret = current;
			}
		}
		ret.setY(minY);
		return ret;
	}
	
	private BlockVector getMaxBlockVec(List<BlockVector> toCheck)
	{
		if (toCheck.size() == 0)
		{
			return null;
		}
		BlockVector ret = toCheck.get(0);
		int maxY = 0;
		for (BlockVector current : toCheck)
		{
			if (current.getBlockY() > maxY)
			{
				maxY = current.getBlockY();
			}
			if (current.getBlockX() >= ret.getBlockX() &&
					current.getBlockZ() >= ret.getBlockZ())
			{
				ret = current;
			}
		}
		ret.setY(maxY);
		return ret;
	}
}
