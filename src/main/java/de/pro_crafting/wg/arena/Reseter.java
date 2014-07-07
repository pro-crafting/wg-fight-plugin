package de.pro_crafting.wg.arena;

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

import de.pro_crafting.generator.CuboidJob;
import de.pro_crafting.generator.Job;
import de.pro_crafting.generator.JobState;
import de.pro_crafting.generator.JobStateChangedCallback;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.event.ArenaStateChangedEvent;

public class Reseter implements Listener, JobStateChangedCallback
{
	private Arena arena;
	private WarGear plugin;
	private int groundHeight;
	
	public Reseter(WarGear plugin, Arena arena)
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
		this.plugin.getGenerator().addJob(new CuboidJob(min, max, Material.AIR, this, (byte)0));
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
		
		if (event.getTo() == State.PreRunning)
		{
			this.removeItems(this.arena.getRepo().getWorld());
		}
		else if (event.getTo() == State.Resetting)
		{
			this.reset();
		}
	}
	
	public void jobStateChanged(Job job, JobState fromState) {
		if (job.getState() != JobState.Finished)
		{
			return;
		}
		removeItems(this.arena.getRepo().getWorld());
		try {
			this.pasteGround(this.arena.getRepo().getWorld());
		} catch (Exception e) {
			this.plugin.getLogger().info("Boden in "+this.arena.getName()+" konnte nicht geresetet werden.");
		}
		if (this.arena.getState() == State.Resetting)
		{
			this.arena.updateState(State.Idle);
		}
	}
}
