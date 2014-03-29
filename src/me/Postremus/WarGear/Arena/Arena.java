package me.Postremus.WarGear.Arena;

import java.util.ArrayList;
import java.util.List;

import me.Postremus.WarGear.ArenaState;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.ui.ScoreBoardDisplay;
import me.Postremus.WarGear.Events.ArenaStateChangedEvent;
import me.Postremus.WarGear.FightModes.KitMode;
import me.Postremus.WarGear.Team.TeamManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.sk89q.worldguard.bukkit.BukkitUtil;
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
	private ArenaState arenaState;
	private List<Player> playersInArena;
	private ScoreBoardDisplay scores;
	private ArenaListener listener;
	private ArenaRepository repo;
	
	public Arena(WarGear plugin, String arenaName)
	{
		this.plugin = plugin;	
		this.name = arenaName;
		
		this.arenaState = ArenaState.Idle;
		this.kitname = "";
		this.playersInArena = new ArrayList<Player>();
		this.repo = new ArenaRepository(this.plugin, this);
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
	
	public ArenaState getFightState()
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
	
	public List<Player> getPlayersInArena()
	{
		return this.playersInArena;
	}
	
	public ScoreBoardDisplay getScore()
	{
		return this.scores;
	}
	
	public ArenaRepository getRepo()
	{
		return this.repo;
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
	
	public boolean load()
	{
		if (this.repo.load())
		{
		this.team = new TeamManager(plugin, this);
		this.setFightMode(new KitMode(this.plugin, this));
		this.reseter = new ArenaReseter(this.plugin, this);
		this.remover = new WaterRemover(this.plugin, this);
		scores = new ScoreBoardDisplay(this.plugin, this);
		this.listener = new ArenaListener(this.plugin, this);
		this.plugin.getServer().getPluginManager().registerEvents(this.listener, this.plugin);
		return true;
		}
		return false;
	}
	
	public void unload()
	{
		this.team = null;
		this.fightMode = null;
		this.reseter = null;
		this.remover = null;
		this.scores = null;
		this.listener = null;
	}
	
	public void setArenaOpeningFlags(Boolean allowed)
	{
		String value = "allow";
		if (!allowed)
		{
			value = "deny";
		}
		
		setFlag(this.repo.getTeam1Region(), DefaultFlag.TNT, value);
		setFlag(this.repo.getTeam1Region(), DefaultFlag.BUILD, value);
		setFlag(this.repo.getTeam1Region(), DefaultFlag.PVP, value);
		setFlag(this.repo.getTeam1Region(), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.repo.getTeam1Region(), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.repo.getTeam1Region(), DefaultFlag.CHEST_ACCESS, value);
		setFlag(this.repo.getTeam2Region(), DefaultFlag.TNT, value);
		setFlag(this.repo.getTeam2Region(), DefaultFlag.BUILD, value);
		setFlag(this.repo.getTeam2Region(), DefaultFlag.PVP, value);
		setFlag(this.repo.getTeam2Region(), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.repo.getTeam2Region(), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.repo.getTeam2Region(), DefaultFlag.CHEST_ACCESS, value);
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
	
	public void broadcastMessage(String message)
	{
		for (Player player : this.plugin.getRepo().getPlayerOfRegion(this.repo.getArenaRegion()))
		{
			player.sendMessage(message);
		}
	}
	
	public void broadcastOutside(String message)
	{
		for (Player player : this.plugin.getServer().getOnlinePlayers())
		{
			if (!this.contains(player.getLocation()))
			{
				player.sendMessage(message);
			}
		}
	}
	
	public void updateFightState(ArenaState to)
	{
		ArenaState from = this.arenaState;
		this.arenaState = processFightStateChange(to);
		ArenaStateChangedEvent arenaStateEvent = new ArenaStateChangedEvent(this, from, to);
		this.plugin.getServer().getPluginManager().callEvent(arenaStateEvent);
	}

	private ArenaState processFightStateChange(ArenaState to)
	{
		if (to == ArenaState.Spectate)
		{
			to = ArenaState.Idle;
		}
		if (to == ArenaState.Reseting && !this.repo.getAutoReset())
		{
			to = ArenaState.Idle;
		}
		return to;
	}
	
	public boolean contains(Location loc)
	{
		return this.repo.getArenaRegion().contains(BukkitUtil.toVector(loc)) &&
				this.getRepo().getWorld().getName().equals(loc.getWorld().getName());
	}
	
	public void teleport(Entity entity)
	{
		entity.teleport(this.getRepo().getFightEndWarp(), TeleportCause.PLUGIN);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Arena other = (Arena) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
