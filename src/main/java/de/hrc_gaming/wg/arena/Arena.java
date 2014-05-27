package de.hrc_gaming.wg.arena;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.hrc_gaming.wg.FightMode;
import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.ui.ScoreBoardDisplay;
import de.hrc_gaming.wg.event.ArenaStateChangedEvent;
import de.hrc_gaming.wg.modes.ChestMode;
import de.hrc_gaming.wg.modes.KitMode;
import de.hrc_gaming.wg.team.TeamManager;
import de.hrc_gaming.wg.team.WgTeam;

public class Arena{
	private WarGear plugin;
	private String name;
	private TeamManager team;
	private String kitname;
	private FightMode fightMode;
	private Reseter reseter;
	private WaterRemover remover;
	private State state;
	private List<UUID> players;
	private ScoreBoardDisplay scores;
	private Repository repo;
	private SpectatorMode spectator;
	
	public Arena(WarGear plugin, String arenaName)
	{
		this.plugin = plugin;	
		this.name = arenaName;
		
		this.state = State.Idle;
		this.kitname = "";
		this.players = new ArrayList<UUID>();
		this.repo = new Repository(this.plugin, this);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public TeamManager getTeam() {
		return team;
	}
	
	public Reseter getReseter()
	{
		return this.reseter;
	}
	
	public WaterRemover getRemover()
	{
		return this.remover;
	}
	
	public State getState()
	{
		return this.state;
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
	
	public List<UUID> getPlayers()
	{
		return this.players;
	}
	
	public void join(Player p)
	{
		if (!this.players.contains(p.getUniqueId()))
		{
			this.players.add(p.getUniqueId());
			this.scores.addViewer(p);
		}
	}
	
	public void leave(Player p)
	{
		if (this.players.contains(p.getUniqueId()))
		{
			this.players.remove(p.getUniqueId());
			this.scores.removeViewer(p);
		}
	}
	
	public Repository getRepo()
	{
		return this.repo;
	}
	
	public void open()
	{
		this.setOpeningFlags(true);
		this.remover.start();
		this.broadcastMessage(ChatColor.GREEN + "Arena Freigegeben!");
	}
	
	public void close()
	{
		this.setOpeningFlags(false);
		this.remover.stop();
		this.broadcastMessage(ChatColor.GREEN + "Arena gesperrt!");
	}
	
	public boolean load()
	{
		if (this.repo.load())
		{
			this.team = new TeamManager(plugin, this);
			this.setFightMode(new KitMode(this.plugin, this));
			this.reseter = new Reseter(this.plugin, this);
			this.remover = new WaterRemover(this.plugin, this);
			this.scores = new ScoreBoardDisplay(this.plugin, this);
			this.spectator = new SpectatorMode(this.plugin, this);
			return true;
		}
		return false;
	}
	
	public void unload()
	{
		HandlerList.unregisterAll(this.team);
		HandlerList.unregisterAll(this.reseter);
		HandlerList.unregisterAll(this.scores);
		remover.stop();
	}
	
	public void setOpeningFlags(Boolean allowed)
	{
		com.sk89q.worldguard.protection.flags.StateFlag.State value = allowed ? com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW : com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
		
		setOpeningFlags(this.repo.getTeam1Region(), value);
		setOpeningFlags(this.repo.getTeam2Region(), value);
	}
	
	private void setOpeningFlags(ProtectedRegion region, com.sk89q.worldguard.protection.flags.StateFlag.State value)
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
	
	public void updateState(State to)
	{
		State from = this.state;
		this.state = processStateChange(to);
		ArenaStateChangedEvent arenaStateEvent = new ArenaStateChangedEvent(this, from, this.state);
		this.plugin.getServer().getPluginManager().callEvent(arenaStateEvent);
	}

	private State processStateChange(State to)
	{
		if (to == State.Spectate && !this.repo.isScoreboardEnabled())
		{
			to = State.Resetting;
		}
		if (to == State.Resetting && !this.repo.getAutoReset())
		{
			to = State.Idle;
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
		SimpleEntry<Vector, Vector> minMax = getMinMax(vectors);
		return new CuboidRegion(minMax.getKey(), minMax.getValue());
	}
	
	private SimpleEntry<Vector, Vector> getMinMax(List<BlockVector> toCheck)
	{
		Vector min = new BlockVector(toCheck.get(0));
		Vector max = new BlockVector(toCheck.get(0));
		for (BlockVector vec : toCheck)
		{
			if (vec.getBlockX() < min.getBlockX())
			{
				min = min.setX(vec.getBlockX());
			}
			if (vec.getBlockY() < min.getBlockY())
			{
				min = min.setY(vec.getBlockY());
			}
			if (vec.getBlockZ() < min.getBlockZ())
			{
				min = min.setZ(vec.getBlockZ());
			}
			if (vec.getBlockX() > max.getBlockX())
			{
				max = max.setX(vec.getBlockX());
			}
			if (vec.getBlockY() > max.getBlockY())
			{
				max = max.setY(vec.getBlockY());
			}
			if (vec.getBlockZ() > max.getBlockZ())
			{
				max = max.setZ(vec.getBlockZ());
			}
		}
		SimpleEntry<Vector, Vector> ret = new SimpleEntry<Vector, Vector>(min, max);
		return ret;
	}
	
	public void startFight(CommandSender sender)
	{
		if (this.getKit() == null || this.getKit().length() == 0)
		{
			if (this.plugin.getRepo().getDefaultKitName() == null || this.plugin.getRepo().getDefaultKitName().length() == 0)
			{
				sender.sendMessage("§cEs wurde kein Kit ausgewählt oder ein Standard Kit angegeben.");
				return;
			}
			else
			{
				this.setKit(this.plugin.getRepo().getDefaultKitName());
			}
		}
		if (!this.getFightMode().getName().equalsIgnoreCase(this.getRepo().getFightMode()))
		{
			if (this.getRepo().getFightMode().equalsIgnoreCase("kit"))
			{
				this.setFightMode(new KitMode(this.plugin, this));
			}
			else
			{
				this.setFightMode(new ChestMode(this.plugin, this));
			}
		}
		this.setOpeningFlags(false);
		this.getTeam().sendTeamOutput();
		this.getFightMode().start();
		this.updateState(State.PreRunning);
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

	public ScoreBoardDisplay getScore() {
		return scores;
	}
	
	public Location getSpawnLocation(Player p)
	{
		if (this.state == State.Spectate || this.state == State.Setup)
		{
			WgTeam playerTeam = this.team.getTeamOfPlayer(p);
			if (playerTeam != null)
			{
				return this.team.getTeamSpawn(playerTeam.getTeamName());
			}
		}
		return this.repo.getFightEndWarp();
	}
	
	public SpectatorMode getSpectatorMode()
	{
		return this.spectator;
	}
}
