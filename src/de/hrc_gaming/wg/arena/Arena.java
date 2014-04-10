package de.hrc_gaming.wg.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.hrc_gaming.wg.FightMode;
import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.ui.ScoreBoardDisplay;
import de.hrc_gaming.wg.event.ArenaStateChangedEvent;
import de.hrc_gaming.wg.modes.KitMode;
import de.hrc_gaming.wg.team.TeamManager;

public class Arena{
	private WarGear plugin;
	private String name;
	private TeamManager team;
	private String kitname;
	private FightMode fightMode;
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
	
	public ArenaState getState()
	{
		return this.arenaState;
	}
	
	public String getKit() {
		return kitname;
	}
	
	public void setKit(String kitname) {
		this.kitname = kitname;
	}

	public FightMode getFightMode() {
		return fightMode;
	}

	public void setFightMode(FightMode fightMode) {
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
		HandlerList.unregisterAll(this.team);
		HandlerList.unregisterAll(this.reseter);
		HandlerList.unregisterAll(this.scores);
		HandlerList.unregisterAll(this.listener);
		remover.stop();
	}
	
	public void setArenaOpeningFlags(Boolean allowed)
	{
		State value = allowed ? State.ALLOW : State.DENY;
		
		setArenaOpeningFlags(this.repo.getTeam1Region(), value);
		setArenaOpeningFlags(this.repo.getTeam2Region(), value);
	}
	
	private void setArenaOpeningFlags(ProtectedRegion region, State value)
	{
		region.setFlag(DefaultFlag.TNT, value);
		region.setFlag(DefaultFlag.BUILD, value);
		region.setFlag(DefaultFlag.PVP, value);
		region.setFlag(DefaultFlag.FIRE_SPREAD, value);
		region.setFlag(DefaultFlag.GHAST_FIREBALL, value);
		region.setFlag(DefaultFlag.CHEST_ACCESS, value);
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
	
	public void updateState(ArenaState to)
	{
		ArenaState from = this.arenaState;
		this.arenaState = processStateChange(to);
		ArenaStateChangedEvent arenaStateEvent = new ArenaStateChangedEvent(this, from, this.arenaState);
		this.plugin.getServer().getPluginManager().callEvent(arenaStateEvent);
	}

	private ArenaState processStateChange(ArenaState to)
	{
		if (to == ArenaState.Spectate)
		{
			to = ArenaState.Reseting;
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
	
	public CuboidRegion getPlayGroundRegion()
	{
		List<BlockVector> vectors = new ArrayList<BlockVector>();
		vectors.add(this.repo.getTeam1Region().getMinimumPoint());
		vectors.add(this.repo.getTeam2Region().getMinimumPoint());
		vectors.add(this.repo.getTeam1Region().getMaximumPoint());
		vectors.add(this.repo.getTeam2Region().getMaximumPoint());
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