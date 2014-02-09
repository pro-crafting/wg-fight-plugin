package me.Postremus.WarGear.Arena;

import me.Postremus.WarGear.WarGear;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ArenaRepository 
{
	private WarGear plugin;
	private String world;
	private ProtectedRegion arenaRegion;
	private String mode;
	private int groundHeight;
	private String groundSchematic;
	private boolean autoReset;
	private ProtectedRegion team1Region;
	private ProtectedRegion team2Region;
	private Location team1Warp;
	private Location team2Warp;
	private Location fightEndWarp;
	
	private String worldPath;
	private String arenaRegionPath;
	private String modePath;
	private String groundHeightPath;
	private String groundSchematicPath;
	private String autoResetPath;
	private String team1RegionPath;
	private String team2RegionPath;
	private String team1WarpPath;
	private String team2WarpPath;
	private String fightEndWarpPath;
	
	public ArenaRepository(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		
		worldPath = "wgk.arenas."+arena.getArenaName()+".world";
		arenaRegionPath = "wgk.arenas."+arena.getArenaName()+".arenaRegion";
		modePath = "wgk.arenas."+arena.getArenaName()+".mode";
		groundHeightPath = "wgk.arenas."+arena.getArenaName()+".groundHeight";
		groundSchematicPath = "wgk.arenas."+arena.getArenaName()+".groundschematic";
		autoResetPath = "wgk.arenas."+arena.getArenaName()+".auto-reset";
		team1RegionPath = "wgk.arenas."+arena.getArenaName()+".regions.Team1";
		team2RegionPath = "wgk.arenas."+arena.getArenaName()+".regions.Team2";
		team1WarpPath = "wgk.arenas."+arena.getArenaName()+".warpFightStart.Team1";
		team2WarpPath = "wgk.arenas."+arena.getArenaName()+".warpFightStart.Team2";
		fightEndWarpPath = "wgk.arenas."+arena.getArenaName()+".warpFightEnd";
	}
	
	public boolean load()
	{
		if (!loadWorld())
		{
			return false;
		}
		if (!loadArenaRegion())
		{
			return false;
		}
		if (!loadMode())
		{
			return false;
		}
		if (!loadGroundHeight())
		{
			return false;
		}
		if (!loadGroundSchematic())
		{
			return false;
		}
		if (!this.loadAutoReset())
		{
			return false;
		}
		if (!this.loadTeam1Region())
		{
			return false;
		}
		if (!this.loadTeam2Region())
		{
			return false;
		}
		if (!this.loadTeam1Warp())
		{
			return false;
		}
		if (!this.loadTeam2Warp())
		{
			return false;
		}
		if (!this.loadFightEndWarp())
		{
			return false;
		}
		return true;
	}
	
	private boolean loadWorld()
	{
		String worldName = this.plugin.getConfig().getString(this.worldPath);
		if (!this.existsWorld(worldName))
		{
			return false;
		}
		this.world = worldName;
		return true;
	}
	
	private boolean loadArenaRegion()
	{
		String id = this.plugin.getConfig().getString(this.arenaRegionPath);
		if (!this.existsWorldGuardRegion(id))
		{
			return false;
		}
		this.arenaRegion = this.getWorldGuardRegion(id);
		return true;
	}
	
	private boolean loadMode()
	{
		this.mode = this.plugin.getConfig().getString(modePath, "kit");
		return true;
	}
	
	private boolean loadGroundHeight()
	{
		int groundHeight = this.plugin.getConfig().getInt(groundHeightPath, -1);
		if (groundHeight < 0 && groundHeight > this.getWorld().getMaxHeight())
		{
			return false;
		}
		this.groundHeight = groundHeight;
		return true;
	}
	
	private boolean loadGroundSchematic()
	{
		this.groundSchematic = this.plugin.getConfig().getString(groundSchematicPath);
		return true;
	}
	
	private boolean loadAutoReset()
	{
		this.autoReset = this.plugin.getConfig().getBoolean(autoResetPath, true);
		return true;
	}
	
	private boolean loadTeam1Region()
	{
		String id = this.plugin.getConfig().getString(this.team1RegionPath);
		if (!this.existsWorldGuardRegion(id))
		{
			return false;
		}
		this.team1Region = this.getWorldGuardRegion(id);
		return true;
	}
	
	private boolean loadTeam2Region()
	{
		String id = this.plugin.getConfig().getString(this.team2RegionPath);
		if (!this.existsWorldGuardRegion(id))
		{
			return false;
		}
		this.team2Region = this.getWorldGuardRegion(id);
		return true;
	}
	
	private boolean loadTeam1Warp()
	{
		this.team1Warp = this.loadLocationFromConfig(this.team1WarpPath, getWorld());
		return true;
	}
	
	private boolean loadTeam2Warp()
	{
		this.team2Warp = this.loadLocationFromConfig(this.team2WarpPath, getWorld());
		return true;
	}
	
	private boolean loadFightEndWarp()
	{
		this.fightEndWarp = this.loadLocationFromConfig(this.fightEndWarpPath, getWorld());
		return true;
	}
	
	private Location loadLocationFromConfig(String node, World world)
	{
		int x = this.plugin.getConfig().getInt(node+".x");
		int y = this.plugin.getConfig().getInt(node+".y");
		int z = this.plugin.getConfig().getInt(node+".z");
		return new Location(world, x, y, z);
	}
	
	private boolean existsWorld(String name)
	{
		for (World w : this.plugin.getServer().getWorlds())
		{
			if (w.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean existsWorldGuardRegion(String id)
	{
		RegionManager rm = this.plugin.getRepo().getWorldGuard().getRegionManager(this.getWorld());
		ProtectedRegion rg = rm.getRegion(id);
		return rg != null;
	}
	
	private ProtectedRegion getWorldGuardRegion(String id)
	{
		RegionManager rm = this.plugin.getRepo().getWorldGuard().getRegionManager(this.getWorld());
		return  rm.getRegion(id);
	}
	
	public boolean save()
	{
		return false;
	}
	
	public World getWorld()
	{
		return this.plugin.getServer().getWorld(world);
	}
	
	public void setWorld(String name)
	{
		if (this.existsWorld(name))
		{
			this.world = name;
		}
	}
	
	public ProtectedRegion getArenaRegion()
	{
		return this.arenaRegion;
	}
	
	public void setArenaRegion(String id)
	{
		RegionManager rm = this.plugin.getRepo().getWorldGuard().getRegionManager(this.getWorld());
		ProtectedRegion rg = rm.getRegion(id);
		if (rg != null)
		{
			this.arenaRegion = rg;
		}
	}

	public String getFightMode()
	{
		return this.mode;
	}
	
	public void setFightMode(String modeName)
	{
		this.mode = modeName;
	}
	
	public int getGroundHeight()
	{
		return this.groundHeight;
	}
	
	public void setGroundHeight(int height)
	{
		if (height >= 0 && height <= this.getWorld().getMaxHeight())
		{
			this.groundHeight = height;
		}
	}
	
	public String getGroundSchematic()
	{
		return this.groundSchematic;
	}
	
	public void setGroundSchematic(String groundSchematic)
	{
		this.groundSchematic = groundSchematic;
	}
	
	public boolean getAutoReset()
	{
		return this.autoReset;
	}
	
	public void setAutoReset(boolean autoReset)
	{
		this.autoReset = autoReset;
	}
	
	public ProtectedRegion getTeam1Region()
	{
		return this.team1Region;
	}
	
	public void setTeam1Region(String id)
	{
		RegionManager rm = this.plugin.getRepo().getWorldGuard().getRegionManager(this.getWorld());
		ProtectedRegion rg = rm.getRegion(id);
		if (rg != null)
		{
			this.team1Region = rg;
		}
	}
	
	public ProtectedRegion getTeam2Region()
	{
		return this.team2Region;
	}
	
	public void setTeam2Region(String id)
	{
		RegionManager rm = this.plugin.getRepo().getWorldGuard().getRegionManager(this.getWorld());
		ProtectedRegion rg = rm.getRegion(id);
		if (rg != null)
		{
			this.team2Region = rg;
		}
	}
	
	public Location getTeam1Warp()
	{
		return this.team1Warp;
	}
	
	public void setTeam1Warp(Location warp)
	{
		this.team1Warp = warp;
	}
	
	public Location getTeam2Warp()
	{
		return this.team2Warp;
	}
	
	public void setTeam2Warp(Location warp)
	{
		this.team2Warp = warp;
	}
	
	public Location getFightEndWarp()
	{
		return this.fightEndWarp;
	}
	
	public void setFightEndWarp(Location warp)
	{
		this.fightEndWarp = warp;
	}
	
}
