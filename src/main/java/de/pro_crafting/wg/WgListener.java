package de.pro_crafting.wg;

import java.util.AbstractMap.SimpleEntry;

import net.gravitydevelopment.updater.Updater.UpdateResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.ArenaPosition;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.event.ArenaStateChangeEvent;
import de.pro_crafting.wg.event.FightQuitEvent;
import de.pro_crafting.wg.event.PlayerArenaChangeEvent;
import de.pro_crafting.wg.event.WinQuitEvent;
import de.pro_crafting.wg.group.Group;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.PlayerRole;
import de.pro_crafting.wg.modes.KitMode;

public class WgListener implements Listener {
	private WarGear plugin;
	
	public WgListener(WarGear plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void handlePlayerJoinEvent(PlayerJoinEvent event) {
		if (!event.getPlayer().hasPermission("wargear.update")) {
			return;
		}
		if (this.plugin.getUpdater() != null && this.plugin.getUpdater().getResult() == UpdateResult.UPDATE_AVAILABLE) {
			event.getPlayer().sendMessage("§7Version "+this.plugin.getUpdater().getLatestName()+" von "+this.plugin.getName()+" ist veröffentlicht.");
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void arenaStateChangedHandler(ArenaStateChangeEvent event) {
		if (event.getTo() == State.Idle) {
			event.getArena().getGroupManager().quitFight();
			event.getArena().setFightMode(new KitMode(this.plugin, event.getArena()));
		}
		if (event.getTo() == State.Spectate) {
			event.getArena().getSpectatorMode().start();
		}
		if (event.getTo() == State.Running) {
			event.getArena().getGroupManager().healGroup(event.getArena().getGroupManager().getGroup1());
			event.getArena().getGroupManager().healGroup(event.getArena().getGroupManager().getGroup2());
		}
		if (event.getTo() == State.PreRunning) {
			event.getArena().replaceMG();
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void asyncPlayerChatHandler(AsyncPlayerChatEvent event) {
		if (!this.plugin.getRepo().isPrefixEnabled()) {
			return;
		}
		Player player = event.getPlayer();
		Arena arena = this.plugin.getArenaManager().getArenaAt(event.getPlayer().getLocation());
		if (arena == null) {
			return;
		}
		String color = arena.getGroupManager().getPrefix(arena.getGroupManager().getRole(player));
		event.setFormat("§8["+color+arena.getName()+"§8]"+event.getFormat());
	}
	
	 @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
	 public void playerRespawnHandler(PlayerRespawnEvent event) {
		 final Player respawned = event.getPlayer();
		 
		 Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(respawned);
		 if (arena != null) {
			 event.setRespawnLocation(arena.getSpawnLocation(respawned));
			 
			 this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){
				 public void run() {
					respawned.getInventory().clear();
				}
		 	}, 60);
		 }
	 }
	 
	@EventHandler (priority = EventPriority.LOWEST)
	public void quit(FightQuitEvent event) {
		State state = event.getArena().getState();
		if (state != State.PreRunning && state != State.Running) {
			return;
		}
		event.getArena().close();
		if (event.getMessage().length() > 0) {
			event.getArena().broadcastMessage(ChatColor.DARK_GREEN + event.getMessage());
		}
		if (event instanceof WinQuitEvent) {
			WinQuitEvent winEvent = (WinQuitEvent)event;
			event.getArena().getGroupManager().sendWinnerOutput(winEvent.getWinnerTeam().getRole());
		}
		event.getArena().getFightMode().stop();
		event.getArena().updateState(State.Spectate);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerMoveHandler(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Arena arenaFrom = this.plugin.getArenaManager().getArenaAt(event.getFrom());
		Arena arenaTo = this.plugin.getArenaManager().getArenaAt(event.getTo());
		if (arenaFrom != null && !arenaFrom.equals(arenaTo)) {
			arenaFrom.leave(player);
			Bukkit.getPluginManager().callEvent(new PlayerArenaChangeEvent(player, arenaFrom, arenaTo));
		}
		if (arenaTo != null) {
			arenaTo.join(player);
			doGroundDamage(event.getTo(), arenaTo, event.getPlayer());
			
			Bukkit.getPluginManager().callEvent(new PlayerArenaChangeEvent(player, arenaFrom, arenaTo));
			
			if (player.hasPermission("wargear.arena.bypass")) {
				return;
			}
			
			ArenaPosition to = arenaTo.getPosition(event.getTo());
			ArenaPosition from = arenaTo.getPosition(event.getFrom());
			Group team = arenaTo.getGroupManager().getGroupOfPlayer(player);
			if (team == null && to != ArenaPosition.Platform) {
				resetPlayerMovement(to, from, event.getFrom(), player, arenaTo);
			} else if (team != null && team.getRole() == PlayerRole.Team1 && (to == ArenaPosition.Team2PlayField || to == ArenaPosition.Team2WG)) {
				resetPlayerMovement(to, from, event.getFrom(), player, arenaTo);
			} else if (team != null && team.getRole() == PlayerRole.Team2 && (to == ArenaPosition.Team1PlayField || to == ArenaPosition.Team1WG)) {
				resetPlayerMovement(to, from, event.getFrom(), player, arenaTo);
			}
		}
	}
	
	private void doGroundDamage(Location to, Arena arenaTo, Player player) {
		if (arenaTo.getState() != State.Running) {
			return;
		}
		
		if (to.getY() > arenaTo.getRepo().getGroundHeight()) {
			return;
		}
		if (!arenaTo.contains(to)) {
			return;
		}
		GroupMember member = arenaTo.getGroupManager().getGroupMember(player);
		if (member != null && member.isAlive()) {
			player.damage(arenaTo.getRepo().getGroundDamage());
			this.plugin.getScoreboard().updateHealthOfPlayer(arenaTo, player);
		}
	}
	
	private void resetPlayerMovement(ArenaPosition position, ArenaPosition from, Location loc, Player player, Arena arena) {
		if (from == position) {
			arena.teleport(player);
		} else {
			player.teleport(loc);
			player.setVelocity(new Vector(0, 0, 0));
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerJoinHandler(PlayerJoinEvent event) {
		Arena arenaTo = this.plugin.getArenaManager().getArenaAt(event.getPlayer().getLocation());
		if (arenaTo != null) {
			arenaTo.join(event.getPlayer());
			Bukkit.getPluginManager().callEvent(new PlayerArenaChangeEvent(event.getPlayer(), null, arenaTo));
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerQuitHandler(PlayerQuitEvent event) {
		Arena arenaFrom = this.plugin.getArenaManager().getArenaAt(event.getPlayer().getLocation());
		if (arenaFrom != null) {
			arenaFrom.leave(event.getPlayer());
			Bukkit.getPluginManager().callEvent(new PlayerArenaChangeEvent(event.getPlayer(), arenaFrom, null));
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerTeleportHandler(PlayerTeleportEvent event){
		Arena arenaFrom = this.plugin.getArenaManager().getArenaAt(event.getFrom());
		Arena arenaTo = this.plugin.getArenaManager().getArenaAt(event.getTo());
		if (arenaFrom != null && !arenaFrom.equals(arenaTo)) {
			arenaFrom.leave(event.getPlayer());
			Bukkit.getPluginManager().callEvent(new PlayerArenaChangeEvent(event.getPlayer(), arenaFrom, arenaTo));
		}
		if (arenaTo != null && !arenaTo.equals(arenaFrom)) {
			arenaTo.join(event.getPlayer());
			Bukkit.getPluginManager().callEvent(new PlayerArenaChangeEvent(event.getPlayer(), arenaFrom, arenaTo));
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void entityDamgeHandler(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		final Player player = (Player)event.getEntity();
		final Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(player);
		if (arena == null) {
			return;
		}
		
		if (arena.getState() != State.Running) {
			event.setCancelled(true);
			return;
		}
		
		if (event instanceof EntityDamageByEntityEvent) {
			checkTeamDamaging((EntityDamageByEntityEvent)event, player, arena);
		}
		
		Bukkit.getScheduler().runTask(this.plugin, new Runnable(){
 			public void run()
 			{
 				WgListener.this.plugin.getScoreboard().updateHealthOfPlayer(arena, player);
			}
		});
	}
	
	private void checkTeamDamaging(EntityDamageByEntityEvent event, Player player, Arena arena) {
		Player damager = null;
		if (event.getDamager() instanceof Projectile
				&& ((Projectile)event.getDamager()).getShooter() instanceof Player) {
			damager = (Player) ((Projectile)event.getDamager()).getShooter();
		} else if (event.getDamager() instanceof Player) {
			damager = (Player) event.getDamager();
		}
		if (damager != null && arena.getGroupManager().getGroupOfPlayer(player).equals(arena.getGroupManager().getGroupOfPlayer(damager))) {
			damager.sendMessage("§7Du darfst Spielern aus deinem Team keinen Schaden zufügen.");
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void entityRegainHealthHandler(EntityRegainHealthEvent event){
		if (event.getEntity().getType() != EntityType.PLAYER) {
			return;
		}
		final Player player = (Player)event.getEntity();
		final Arena arena = this.plugin.getArenaManager().getArenaAt(player.getLocation());
		if (arena != null) {
			if (arena.getGroupManager().getGroupOfPlayer(player) != null) {
				this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable(){
					public void run() {
						WgListener.this.plugin.getScoreboard().updateHealthOfPlayer(arena, player);
					}
				});
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=false)
	public void playerInteractHandler(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(player);
		if (arena == null) {
			return;
		}
		if (block != null && block.getType() == Material.CAKE_BLOCK) {
			player.sendMessage("§7Du darfst kein Essen benutzen.");
			event.setCancelled(true);
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && block.getType() == Material.JACK_O_LANTERN) {
			player.sendMessage("§7Du darfst keine Kürbislaternen abbauen.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=false)
	public void playerItemConsumeHandler(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if (this.plugin.getArenaManager().getArenaOfTeamMember(player) != null) {
			if (item.getType().isEdible() || item.getType() == Material.POTION) {
				player.sendMessage("§7Du darfst kein Essen und keine Tränke benutzen.");
				player.getInventory().remove(item.getType());
				player.getInventory().setArmorContents(null);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void craftItemHandler(CraftItemEvent event) {
		Player player = (Player)event.getWhoClicked();
		if (this.plugin.getArenaManager().getArenaOfTeamMember(player) != null) {
			player.sendMessage("§7Du darfst nicht craften.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		final Arena arena = this.plugin.getArenaManager().getArenaAt(player.getLocation());
		if (arena == null || arena.getState() != State.Running) {
			return;
		}
		final Group team = arena.getGroupManager().getGroupOfPlayer(player);
		if (team != null && team.getMember(player).isAlive()) {
			team.getMember(player).setAlive(false);
			String color = arena.getGroupManager().getPrefix(team.getRole());
			String message = "§8["+color+arena.getName()+"§8] "+ChatColor.DARK_GREEN+player.getDisplayName()+" ist gestorben.";
			event.setDeathMessage(null);
			arena.broadcastMessage(message);
			Bukkit.getScheduler().runTask(this.plugin, new Runnable() {
				public void run() {            
					WgListener.this.checkAlives(team, arena);
				}
			});
		}
	}
	
	private void checkAlives(Group team, Arena arena) {
		if (!team.isAlive()) {
			Group winnerTeam = arena.getGroupManager().getGroup1();
			if (team.getRole() == PlayerRole.Team1) {
				winnerTeam = arena.getGroupManager().getGroup2();
			}
			String message = "Jeder aus dem ["+team.getRole().toString().toUpperCase()+"] ist tot.";
			this.plugin.getServer().getPluginManager().callEvent(new WinQuitEvent(arena, message, winnerTeam, team, FightQuitReason.Death));
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
    public void entityExplodeHandler(EntityExplodeEvent event)
	{
		Arena arena = this.plugin.getArenaManager().getArenaAt(event.getLocation());
		if (arena == null) {
			return;
		}
		ArenaPosition position = arena.getPosition(event.getLocation());
		if (position != ArenaPosition.Team1WG && position != ArenaPosition.Team2WG) {
			return;
		}
		PlayerGroupKey group = arena.getGroupManager().getGroupKey(position == ArenaPosition.Team1WG ? PlayerRole.Team1 : PlayerRole.Team2);
		for (Block b : event.blockList())
		{
			if (b.getType() == Material.JACK_O_LANTERN)
			{
				group.getGroup().setCannons(group.getGroup().getCannons()-1);
				this.plugin.getScoreboard().updateCannons(arena, group.getRole(), group.getGroup().getCannons());
				event.setYield(0);
			}
			if (b.getType() != Material.WATER || b.getType() != Material.STATIONARY_WATER)
			{
				arena.getRemover().add(b.getLocation());
			}
		}
	}
}
