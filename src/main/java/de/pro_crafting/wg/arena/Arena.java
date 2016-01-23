package de.pro_crafting.wg.arena;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

import de.pro_crafting.common.Point;
import de.pro_crafting.common.Size;
import de.pro_crafting.generator.BlockData;
import de.pro_crafting.generator.JobState;
import de.pro_crafting.generator.JobStateChangedCallback;
import de.pro_crafting.generator.criteria.Criteria;
import de.pro_crafting.generator.criteria.SingleBlockCriteria;
import de.pro_crafting.generator.job.Job;
import de.pro_crafting.generator.job.SimpleJob;
import de.pro_crafting.generator.provider.BlockSearchProvider;
import de.pro_crafting.generator.provider.SingleBlockProvider;
import de.pro_crafting.region.Region;
import de.pro_crafting.region.flags.Flag;
import de.pro_crafting.region.flags.StateValue;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.event.ArenaStateChangeEvent;
import de.pro_crafting.wg.group.*;
import de.pro_crafting.wg.modes.FightMode;
import de.pro_crafting.wg.modes.KitMode;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena{
	private WarGear plugin;
	private String name;
	private GroupManager team;
	private String kitname;
	private FightMode fightMode;
	private Reseter reseter;
	private WaterRemover remover;
	private State state;
	private List<UUID> players;
	private Repository repo;
	private SpectatorMode spectator;
	private boolean isOpen;
	
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

	public GroupManager getGroupManager() {
		return team;
	}
	
	public Reseter getReseter()
	{
		return this.reseter;
	}
	
	public WaterRemover getRemover() {
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
			this.plugin.getScoreboard().addViewer(this.getGroupManager().getGroupKey(p), p);
		}
	}
	
	public void leave(Player p)
	{
		if (this.players.contains(p.getUniqueId()))
		{
			this.players.remove(p.getUniqueId());
			this.plugin.getScoreboard().removeViewer(this, p);
		}
	}
	
	public Repository getRepo()
	{
		return this.repo;
	}
	
	public void open()
	{
		this.setOpen(true);
		this.remover.start();
		this.broadcastMessage(ChatColor.GREEN + "Arena Freigegeben!");
	}
	
	public void close()
	{
		this.setOpen(false);
		this.remover.stop();
		this.broadcastMessage(ChatColor.GREEN + "Arena gesperrt!");
	}
	
	public boolean load()
	{
		if (this.repo.load())
		{
			this.team = new GroupManager(plugin, this);
			this.setFightMode(new KitMode(this.plugin, this));
			this.reseter = new Reseter(this.plugin, this);
			this.remover = new WaterRemover(this.plugin, this);
			this.spectator = new SpectatorMode(this.plugin, this);
			this.setOpen(false);
			return true;
		}
		return false;
	}
	
	public void unload()
	{
		HandlerList.unregisterAll(this.reseter);
		this.remover.stop();
		this.players.clear();
		this.setOpen(false);
		this.plugin.getScoreboard().clearScoreboard(this);
	}
	
	public void setOpen(Boolean isOpen)
	{
		this.isOpen = isOpen;
		StateValue value = isOpen ? StateValue.Allow : StateValue.Deny;
		setOpeningFlags(PlayerRole.Team1, value);
		setOpeningFlags(PlayerRole.Team2, value);
		
		setInnerRegionFlags(this.repo.getInnerRegion(), value);
	}	
	
	private void setInnerRegionFlags(Region region, StateValue value) {
		StateValue forcedValue = StateValue.Deny;
	    region.setFlag(Flag.TNT, value);
	    region.setFlag(Flag.PVP, value);
	    region.setFlag(Flag.Fire_Spread, forcedValue);
	    region.setFlag(Flag.Ghast_Fireball, forcedValue);
	    region.setFlag(Flag.Build, forcedValue);
	}

	private void setOpeningFlags(PlayerRole role, StateValue value) {
		Region region = getGroupManager().getGroupKey(role).getRegion();
		region.setFlag(Flag.TNT, value);
		region.setFlag(Flag.PVP, value);
		region.setFlag(Flag.Fire_Spread, value);
		region.setFlag(Flag.Ghast_Fireball, value);
		region.setFlag(Flag.Build, value);

		removeOwners(role);
		if (value == StateValue.Allow) {
			updateRegion(role);
		}
	}
	
	public void updateRegion(PlayerRole role) {
		removeOwners(role);
		PlayerGroupKey key = this.getGroupManager().getGroupKey(role);
		for (GroupMember player : key.getGroup().getMembers()) {
			if (player.isAlive()) {
				key.getRegion().getOwners().add( player.getOfflinePlayer());
			}
		}
	}
	
	private void removeOwners(PlayerRole role) {
		this.getGroupManager().getGroupKey(role).getRegion().getOwners().clear();;
	}
	
	public void broadcastMessage(String message)
	{
		for (UUID id : getPlayers()) {
			Player player = Bukkit.getPlayer(id);
			if (player != null) {
				player.sendMessage(message);
			}
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
	
	public void updateState(State to) {
		to = processStateChange(to);
		ArenaStateChangeEvent arenaStateEvent = new ArenaStateChangeEvent(this, this.state, to);
		this.plugin.getServer().getPluginManager().callEvent(arenaStateEvent);
		if (!arenaStateEvent.isCancelled()) {
			this.state = arenaStateEvent.getTo();
		}
	}

	private State processStateChange(State to) {
		if (to == State.Spectate && !this.repo.isScoreboardEnabled()) {
			to = State.Resetting;
		}
		if (to == State.Resetting && !this.repo.getAutoReset()) {
			to = State.Idle;
		}
		return to;
	}
	
	public boolean contains(Location loc)
	{
		return this.repo.getArenaRegion().contains(loc) &&
				this.getRepo().getWorld().getName().equals(loc.getWorld().getName());
	}
	
	public void teleport(Player player)
	{
		player.teleport(this.getSpawnLocation(player), TeleportCause.PLUGIN);
	}
	
	public void startFight(CommandSender sender)
	{
		if (this.getKit() == null || this.getKit().isEmpty())
		{
			if (this.plugin.getRepo().getDefaultKitName() == null || this.plugin.getRepo().getDefaultKitName().isEmpty())
			{
				sender.sendMessage("§cEs wurde kein Kit ausgewählt oder ein Standard Kit angegeben.");
				return;
			}
			else
			{
				this.setKit(this.plugin.getRepo().getDefaultKitName());
			}
		}
		this.setFightMode(plugin.getModes().get(this.getRepo().getFightMode(), this));
		if (this.getFightMode() == null) {
			Bukkit.getLogger().warning("Fightmode "+this.getRepo().getFightMode()+" unknown in arena "+this.getName()+"!");
			Bukkit.getLogger().info("Falling back to kit mode");
			this.setFightMode(new KitMode(this.plugin, this));
		}
		this.setOpen(false);
		this.getFightMode().start();
		this.updateState(State.PreRunning);
		countCannons();
	}
	
	private void countCannons() {
		startCannonCounterJob(this.getRepo().getTeam1Region(), this.getGroupManager().getGroupKey(PlayerRole.Team1));
		startCannonCounterJob(this.getRepo().getTeam2Region(), this.getGroupManager().getGroupKey(PlayerRole.Team2));
	}
	
	private void startCannonCounterJob(Region rg, final PlayerGroupKey groupKey) {
		Point origin = new Point(rg.getMin().getX(), rg.getMin().getY(), rg.getMin().getZ());
		Point max = new Point(rg.getMax().getX(), rg.getMax().getY(), rg.getMax().getZ());
		Size size = new Size(max.getX()-origin.getX(), max.getY()-origin.getY(), max.getZ()-origin.getZ());
		this.plugin.getGenerator().addJob(new SimpleJob(origin, size, getRepo().getWorld(), new JobStateChangedCallback() {
			
			public void jobStateChanged(Job job, JobState state) {
				if (job.getState() == JobState.Finished) {
					plugin.getScoreboard().updateCannons(groupKey.getArena(), groupKey.getRole(), groupKey.getGroup().getCannons());
				}
			}
		}, new BlockSearchProvider(new Criteria() {
			Group group = groupKey.getGroup();
			public void wrap(Criteria arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public boolean matches(Point point, BlockData data) {
				if (data.getType() == Material.JACK_O_LANTERN) {
					group.setCannons(group.getCannons()+1);
				}
				return true;
			}
		})));
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
	
	public Location getSpawnLocation(Player p)
	{
		if (this.state == State.Running || this.state == State.PreRunning
				|| this.state == State.Setup || this.state == State.Spectate)
		{
			Group playerTeam = this.team.getGroupOfPlayer(p);
			if (playerTeam != null)
			{
				if (this.state == State.Running && !playerTeam.getMember(p).isAlive()) {
					return this.repo.getSpawnWarp();
				}
				return this.team.getGroupSpawn(playerTeam.getRole());
			}
		}
		return this.repo.getSpawnWarp();
	}
	
	public SpectatorMode getSpectatorMode()
	{
		return this.spectator;
	}
	
	public boolean isOpen()
	{
		return this.isOpen;
	}
	
	public CuboidRegion getPlayGroundRegion() {
		Region innerRegion = this.repo.getInnerRegion();
		Vector min = new Vector().add(innerRegion.getMin().getX() , innerRegion.getMin().getY() , innerRegion.getMin().getZ());
		Vector max = new Vector().add(innerRegion.getMax().getX() , innerRegion.getMax().getY() , innerRegion.getMax().getZ());
		return new CuboidRegion(min, max);
	}
	
	public ArenaPosition getPosition(Location where) {
		CuboidRegion innerRegion = getPlayGroundRegion();
		Vector vector = BukkitUtil.toVector(where);
		
		if (!contains(where)) {
			return ArenaPosition.Outside;
		}
		if (!innerRegion.contains(vector)) {
			return ArenaPosition.Platform;
		}
		
		Region team1 = this.repo.getTeam1Region();
		if (this.repo.getTeam1Region().contains(BukkitUtil.toLocation( team1.getWorld() , vector))) {
			return ArenaPosition.Team1WG;
		}
		
		Region team2 = this.repo.getTeam2Region();
		if (team2.contains(BukkitUtil.toLocation(team2.getWorld(), vector))) {
			return ArenaPosition.Team2WG;
		}
		
		double distanceTeam1Squared = vector.distanceSq(BukkitUtil.toVector( team1.getMin().toLocation( team1.getWorld()))) + 
				vector.distanceSq(BukkitUtil.toVector( team1.getMax().toLocation(team1.getWorld())));
		
		double distanceTeam2Squared = vector.distanceSq(BukkitUtil.toVector( team2.getMin().toLocation( team2.getWorld()))) + 
				vector.distanceSq(BukkitUtil.toVector( team2.getMax().toLocation(team2.getWorld())));
		
		if ((distanceTeam1Squared - distanceTeam2Squared) > 0) {
			return ArenaPosition.Team2PlayField;
		} else {
			return ArenaPosition.Team1PlayField;
		}
	}
	
	public void replaceMG() {
		//cuboid.wrap(new SingleBlockCriteria(Material.OBSIDIAN));
		World world = this.repo.getWorld();
		CuboidRegion innerRegion = getPlayGroundRegion();
		Point origin = new Point(BukkitUtil.toLocation(world, innerRegion.getMinimumPoint()));
		Size size = new Size(innerRegion.getWidth(), innerRegion.getHeight(), innerRegion.getLength());
		this.plugin.getGenerator().addJob(new SimpleJob(origin, size, world, null, 
				new SingleBlockProvider(new SingleBlockCriteria(Material.OBSIDIAN), Material.TNT, (byte)0), true));
	}
}
