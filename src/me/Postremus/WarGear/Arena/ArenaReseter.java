package me.Postremus.WarGear.Arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.Postremus.Generator.CuboidGeneratorJob;
import me.Postremus.Generator.GeneratorJobState;
import me.Postremus.Generator.JobStateChangedEvent;
import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Events.FightStateChangedEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ArenaReseter implements Listener
{
	private Arena arena;
	private WarGear plugin;
	private int groundHeight;
	
	public ArenaReseter(WarGear plugin, Arena arena)
	{
		this.arena = arena;
		this.plugin = plugin;
		groundHeight = arena.getRepo().getGroundHeight();
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	public void reset()
	{
		CuboidRegion rg = getPlayGroundRegion();
		Location min = BukkitUtil.toLocation(this.arena.getRepo().getWorld(), rg.getMinimumPoint());
		min.setY(groundHeight);
		Location max = BukkitUtil.toLocation(this.arena.getRepo().getWorld(), rg.getMaximumPoint());
		this.plugin.getGenerator().addJob(new CuboidGeneratorJob(min, max, Material.AIR, "ArenaReseter:"+this.arena.getArenaName()));
	}
	
	private void stopClear()
	{
		removeItems(this.arena.getRepo().getWorld());
		try {
			this.pasteGround(this.arena.getRepo().getWorld());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.arena.updateFightState(FightState.Idle);
	}
	
	private void pasteGround(World arenaWorld) throws FilenameException, IOException, DataException, MaxChangedBlocksException
	{
	    WorldEditPlugin wePlugin = this.plugin.getRepo().getWorldEdit();
        LocalConfiguration config = wePlugin.getLocalConfiguration();
        LocalPlayer player = wePlugin.wrapCommandSender(this.plugin.getServer().getConsoleSender());
        
        File dir = wePlugin.getWorldEdit().getWorkingDirectoryFile(config.saveDir);
        String schemName = this.arena.getRepo().getGroundSchematic();
        File f = wePlugin.getWorldEdit().getSafeOpenFile(player, dir, schemName, "schematic", "schematic");
    
        EditSession es = new EditSession(new BukkitWorld(arenaWorld), 999999999);
        CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(f);
        es.enableQueue();
        cc.place(es, cc.getOrigin(), false);
        es.flushQueue();
	}
	
	private void removeItems(World arenaWorld)
	{
		CuboidRegion rg = getPlayGroundRegion();
		for (Entity curr : arenaWorld.getEntitiesByClasses(Item.class, Arrow.class))
		{
			if (rg.contains(BukkitUtil.toVector(curr.getLocation())))
			{
				curr.remove();
			}
		}
	}
	
	private CuboidRegion getPlayGroundRegion()
	{
		List<BlockVector> vectors = new ArrayList<BlockVector>();
		vectors.add(arena.getRepo().getTeam1Region().getMinimumPoint());
		vectors.add(arena.getRepo().getTeam2Region().getMinimumPoint());
		vectors.add(arena.getRepo().getTeam1Region().getMaximumPoint());
		vectors.add(arena.getRepo().getTeam2Region().getMaximumPoint());
		return new CuboidRegion(getMinBlockVec(vectors), getMaxBlockVec(vectors));
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
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void generatorJobStateChangedHandler(JobStateChangedEvent event)
	{
		if (event.getTo() != GeneratorJobState.Finished)
		{
			return;
		}
		String name = event.getJob().getJobName();
		if (!name.startsWith("ArenaReseter"))
		{
			return;
		}
		String[] splited = name.split(":");
		if (splited.length>1 && splited[1].equals(this.arena.getArenaName()))
		{
			ArenaReseter.this.stopClear();
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void fightStateChangedHandler(FightStateChangedEvent event)
	{
		if (!event.getArenaName().equalsIgnoreCase(this.arena.getArenaName()))
		{
			return;
		}
		
		if (event.getTo() == FightState.Running)
		{
			this.removeItems(this.arena.getRepo().getWorld());
		}
		else if (event.getTo() == FightState.Running)
		{
			if (this.arena.getRepo().getAutoReset())
			{
				event.setTo(FightState.Reseting);
				this.reset();
			}
		}
	}
}
