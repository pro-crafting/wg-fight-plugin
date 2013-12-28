package me.Postremus.WarGear;

import java.io.File;
import java.io.IOException;

import me.Postremus.WarGear.FightModes.KitMode;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Arena {
	private WarGear plugin;
	private String name;
	private TeamManager team;
	private boolean fightRunning;
	private String kitname;
	private IFightMode fightMode;
	
	public Arena(WarGear plugin)
	{
		this.init(plugin, this.plugin.getRepo().getDefaultArenaName());
	}

	public Arena(WarGear plugin, String arenaName)
	{
		this.init(plugin, arenaName);
	}
	
	private void init(WarGear plugin, String arenaName)
	{
		this.plugin = plugin;
		this.name = arenaName;
		this.team = new TeamManager(plugin, this);
		this.fightRunning = false;
		this.kitname = "";
		this.setFightMode(new KitMode(plugin, this));
	}
	
	public String getArenaName()
	{
		return this.name;
	}
	
	public void setArenaName(String arena)
	{
		this.name = arena;
	}

	public TeamManager getTeam() {
		return team;
	}
	
	public boolean getFightRunning()
	{
		return this.fightRunning;
	}
	
	public void setFightRunning(boolean state)
	{
		this.fightRunning = state;
	}
	
	public void open()
	{
		this.setArenaOpeningFlags(true);
		this.broadcastMessage(ChatColor.GREEN + "Arena Freigegeben!");
	}
	
	public void close()
	{
		this.setArenaOpeningFlags(false);
		this.broadcastMessage(ChatColor.GREEN + "Arena gesperrt!");
	}
	
	public void reset() throws FilenameException, IOException, DataException, MaxChangedBlocksException
	{
		WorldEditPlugin wePlugin = this.plugin.getRepo().getWorldEdit();
	    LocalConfiguration config = wePlugin.getLocalConfiguration();
	    LocalPlayer player = wePlugin.wrapCommandSender(this.plugin.getServer().getConsoleSender());
	    
	    File dir = wePlugin.getWorldEdit().getWorkingDirectoryFile(config.saveDir);
	    String schemName = this.plugin.getRepo().getGroundSchematicName(this);
	    File f = wePlugin.getWorldEdit().getSafeOpenFile(player, dir, schemName, "schematic", "schematic");
        
	    EditSession es = new EditSession(new BukkitWorld(this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this))), 999999999);
	    CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(f);
	    Vector calculatedOrigin = cc.getOrigin();
	    calculatedOrigin.add(cc.getOffset());
	    cc.setOffset(new Vector());
	    cc.paste(es, calculatedOrigin, false, true);
	}
	
	public void setArenaOpeningFlags(Boolean allowed)
	{
		String value = "allow";
		if (!allowed)
		{
			value = "deny";
		}
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.TNT, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.BUILD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.PVP, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.CHEST_ACCESS, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.TNT, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.BUILD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.PVP, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.CHEST_ACCESS, value);
	}
	
	public void setFlag(String RegionName, StateFlag flag, String value)
    {
    	WorldGuardPlugin worldGuard = this.plugin.getRepo().getWorldGuard();
		RegionManager regionManager = worldGuard.getRegionManager(this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this)));
		ProtectedRegion region = regionManager.getRegion(RegionName);
	    try {
			region.setFlag(flag, flag.parseInput(worldGuard, this.plugin.getServer().getConsoleSender(), value));
		} catch (InvalidFlagFormat e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void broadcastMessage(String message)
	{
		for (Player player : this.plugin.getRepo().getPlayerOfRegion(this.plugin.getRepo().getArenaRegion(this), this.plugin.getRepo().getWorldName(this)))
		{
			player.sendMessage(message);
		}
	}

	public String getKit() {
		return kitname;
	}

	public void setKit(String kitname) {
		this.kitname = kitname;
	}

	public IFightMode getFightMode() {
		return fightMode;
	}

	public void setFightMode(IFightMode fightMode) {
		this.fightMode = fightMode;
	}
}
