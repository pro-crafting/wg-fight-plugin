package me.Postremus.WarGear.Arena;

import java.util.ArrayList;
import java.util.List;

import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.ui.ScoreBoardDisplay;
import me.Postremus.WarGear.Events.FightStateChangedEvent;
import me.Postremus.WarGear.FightModes.KitMode;
import me.Postremus.WarGear.Team.TeamManager;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Arena{
	private WarGear plugin;
	private String name;
	private TeamManager team;
	private String kitname;
	private IFightMode fightMode;
	private ArenaReseter reseter;
	private WaterRemover remover;
	private ProtectedRegion regionTeam1;
	private ProtectedRegion regionTeam2;
	private ProtectedRegion arenaRegion;
	private FightState arenaState;
	private List<Player> playersInArena;
	private ScoreBoardDisplay scores;
	private ArenaListener listener;
	
	public Arena(WarGear plugin, String arenaName)
	{
		this.plugin = plugin;	
		this.name = arenaName;
		
		loadRegions();
		this.arenaState = FightState.Idle;
		this.team = new TeamManager(plugin, this);
		this.kitname = "";
		this.setFightMode(new KitMode(this.plugin, this));
		this.reseter = new ArenaReseter(this.plugin, this);
		this.remover = new WaterRemover(this.plugin, this);
		scores = new ScoreBoardDisplay(this.plugin, this);
		this.playersInArena = new ArrayList<Player>();
		this.listener = new ArenaListener(this.plugin, this);
		this.plugin.getServer().getPluginManager().registerEvents(this.listener, this.plugin);
	}
	
	private void loadRegions()
	{
		WorldGuardPlugin worldGuard = this.plugin.getRepo().getWorldGuard();
		World arenaWorld = this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this));
		
		this.arenaRegion = this.getRegion(worldGuard, this.plugin.getRepo().getArenaRegion(this), arenaWorld);
		this.regionTeam1 = this.getRegion(worldGuard, this.plugin.getRepo().getRegionNameTeam1(this), arenaWorld);
		this.regionTeam2 = this.getRegion(worldGuard, this.plugin.getRepo().getRegionNameTeam2(this), arenaWorld);
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
	
	public ArenaReseter getReseter()
	{
		return this.reseter;
	}
	
	public WaterRemover getRemover()
	{
		return this.remover;
	}
	
	public FightState getFightState()
	{
		return this.arenaState;
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
	
	public ProtectedRegion getRegionTeam1()
	{
		return this.regionTeam1;
	}
	
	public ProtectedRegion getRegionTeam2()
	{
		return this.regionTeam2;
	}
	
	public ProtectedRegion getArenaRegion()
	{
		return this.arenaRegion;
	}
	
	public List<Player> getPlayersInArena()
	{
		return this.playersInArena;
	}
	
	public ScoreBoardDisplay getScore()
	{
		return this.scores;
	}
	
	public void open()
	{
		this.setArenaOpeningFlags(true);
		this.remover.start();
		this.broadcastMessage(ChatColor.GREEN + "Arena Freigegeben!");
	}
	
	public void close()
	{
		this.setArenaOpeningFlags(false);
		this.remover.stop();
		this.broadcastMessage(ChatColor.GREEN + "Arena gesperrt!");
	}
	
	public void setArenaOpeningFlags(Boolean allowed)
	{
		String value = "allow";
		if (!allowed)
		{
			value = "deny";
		}
		
		setFlag(this.getRegionTeam1(), DefaultFlag.TNT, value);
		setFlag(this.getRegionTeam1(), DefaultFlag.BUILD, value);
		setFlag(this.getRegionTeam1(), DefaultFlag.PVP, value);
		setFlag(this.getRegionTeam1(), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.getRegionTeam1(), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.getRegionTeam1(), DefaultFlag.CHEST_ACCESS, value);
		setFlag(this.getRegionTeam2(), DefaultFlag.TNT, value);
		setFlag(this.getRegionTeam2(), DefaultFlag.BUILD, value);
		setFlag(this.getRegionTeam2(), DefaultFlag.PVP, value);
		setFlag(this.getRegionTeam2(), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.getRegionTeam2(), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.getRegionTeam2(), DefaultFlag.CHEST_ACCESS, value);
	}
	
	public void setFlag(ProtectedRegion region, StateFlag flag, String value)
    {
		WorldGuardPlugin worldGuard = this.plugin.getRepo().getWorldGuard();
	    try {
			region.setFlag(flag, flag.parseInput(worldGuard, this.plugin.getServer().getConsoleSender(), value));
		} catch (InvalidFlagFormat e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	private ProtectedRegion getRegion(WorldGuardPlugin worldGuard, String regionName, World world)
	{
		RegionManager regionManager = worldGuard.getRegionManager(world);
		return regionManager.getRegion(regionName);
	}
	
	public void broadcastMessage(String message)
	{
		for (Player player : this.plugin.getRepo().getPlayerOfRegion(this.getArenaRegion()))
		{
			player.sendMessage(message);
		}
	}
	
	public void updateFightState(FightState state)
	{
		FightStateChangedEvent fightStateEvent = new FightStateChangedEvent(this.name, this.arenaState, state);
		this.plugin.getServer().getPluginManager().callEvent(fightStateEvent);
		if (this.arenaState == FightState.Running && state == FightState.Idle && this.plugin.getRepo().getAutoReset(this))
		{
			this.arenaState = FightState.Reseting;
			this.reseter.reset();
		}
		this.arenaState = state;
		this.scores.fightStateChanged();
	}
}
