package de.hrc_gaming.wg.arena;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

import de.hrc_gaming.generator.CuboidJob;
import de.hrc_gaming.generator.Job;
import de.hrc_gaming.generator.JobState;
import de.hrc_gaming.generator.JobStateChangedCallback;
import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.event.ArenaStateChangedEvent;

public class ArenaReseter implements Listener, JobStateChangedCallback
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
		CuboidRegion rg = this.arena.getPlayGroundRegion();
		Location min = BukkitUtil.toLocation(this.arena.getRepo().getWorld(), rg.getMinimumPoint());
		min.setY(groundHeight);
		Location max = BukkitUtil.toLocation(this.arena.getRepo().getWorld(), rg.getMaximumPoint());
		this.plugin.getGenerator().addJob(new CuboidJob(min, max, Material.AIR, this));
	}
	
	private void stopClear()
	{
		removeItems(this.arena.getRepo().getWorld());
		try {
			this.pasteGround(this.arena.getRepo().getWorld());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (this.arena.getState() == ArenaState.Resetting)
		{
			this.arena.updateState(ArenaState.Idle);
		}
	}
	
	private void pasteGround(World arenaWorld) throws FilenameException, IOException, DataException, MaxChangedBlocksException
	{
	    WorldEdit we = this.plugin.getRepo().getWorldEdit().getWorldEdit();
        LocalConfiguration config = we.getConfiguration();
        
        File dir = we.getWorkingDirectoryFile(config.saveDir);
        String schemName = this.arena.getRepo().getGroundSchematic();
        File schematic = we.getSafeOpenFile(null, dir, schemName, "schematic");
        
        EditSession es = new EditSession(new BukkitWorld(arenaWorld), config.maxChangeLimit);
        CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(schematic);
        es.enableQueue();
        es.setFastMode(true);
        cc.place(es, cc.getOrigin(), false);
        es.setFastMode(false);
        es.flushQueue();
	}
	
	private void removeItems(World arenaWorld)
	{
		CuboidRegion rg = this.arena.getPlayGroundRegion();
		for (Entity curr : arenaWorld.getEntitiesByClasses(Item.class, Arrow.class))
		{
			if (rg.contains(BukkitUtil.toVector(curr.getLocation())))
			{
				curr.remove();
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void arenaStateChangedHandler(ArenaStateChangedEvent event)
	{
		if (!event.getArena().equals(this.arena))
		{
			return;
		}
		
		if (event.getTo() == ArenaState.PreRunning)
		{
			this.removeItems(this.arena.getRepo().getWorld());
		}
		else if (event.getTo() == ArenaState.Resetting)
		{
			this.reset();
		}
	}
	
	public void jobStateChanged(Job job, JobState fromState) {
		if (job.getState() != JobState.Finished)
		{
			return;
		}
		ArenaReseter.this.stopClear();
	}
}
