package de.hrc_gaming.wg;

import net.gravitydevelopment.updater.Updater.UpdateResult;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.arena.State;
import de.hrc_gaming.wg.event.ArenaStateChangedEvent;
import de.hrc_gaming.wg.event.FightQuitEvent;
import de.hrc_gaming.wg.event.WinQuitEvent;
import de.hrc_gaming.wg.modes.KitMode;
import de.hrc_gaming.wg.team.TeamNames;
import de.hrc_gaming.wg.team.WgTeam;

public class WgListener implements Listener {
	private WarGear plugin;
	
	public WgListener(WarGear plugin)
	{
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void handlePlayerJoinEvent(PlayerJoinEvent event)
	{
		if (!event.getPlayer().hasPermission("wargear.update"))
		{
			return;
		}
		if (this.plugin.getUpdater() != null && this.plugin.getUpdater().getResult() == UpdateResult.UPDATE_AVAILABLE)
		{
			event.getPlayer().sendMessage("§7Version "+this.plugin.getUpdater().getLatestName()+" von "+this.plugin.getName()+" ist veröffentlicht.");
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void arenaStateChangedHandler(ArenaStateChangedEvent event)
	{
		if (event.getTo() == State.Idle)
		{
			event.getArena().getTeam().quitFight();
			event.getArena().setFightMode(new KitMode(this.plugin, event.getArena()));
		}
		if (event.getTo() == State.Spectate)
		{
			event.getArena().getSpectatorMode().start();
		}
		if (event.getTo() == State.Running)
		{
			event.getArena().getTeam().healTeam(event.getArena().getTeam().getTeam1());
			event.getArena().getTeam().healTeam(event.getArena().getTeam().getTeam2());
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void asyncPlayerChatHandler(AsyncPlayerChatEvent event)
	{
		if (!this.plugin.getRepo().isPrefixEnabled())
		{
			return;
		}
		Player player = event.getPlayer();
		Arena arena = this.plugin.getArenaManager().getArenaAtLocation(event.getPlayer().getLocation());
		if (arena == null)
		{
			return;
		}
		String color = "§7";
		WgTeam team = arena.getTeam().getTeamOfPlayer(player);
		if (team != null)
		{
			if (team.getTeamName() == TeamNames.Team1)
			{
				color = "§c";
			}
			else if (team.getTeamName() == TeamNames.Team2)
			{
				color = "§1";
			}
		}
		event.setFormat(color+"["+arena.getName()+"]"+event.getFormat());
	}
	
	 @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
	 public void playerRespawnHandler(PlayerRespawnEvent event)
	 {
		 final Player respawned = event.getPlayer();
		 Arena arena = this.plugin.getArenaManager().getArenaAtLocation(event.getPlayer().getLocation());
		 if (arena != null)
		 {
			 event.setRespawnLocation(arena.getSpawnLocation(respawned));
			 
			 this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){
				 public void run() {
					respawned.getInventory().clear();
				}
		 	}, 60);
		 }
	 }
	 
	@EventHandler (priority = EventPriority.LOWEST)
	public void quit(FightQuitEvent event)
	{
		State state = event.getArena().getState();
		if (state != State.PreRunning && state != State.Running)
		{
			return;
		}
		event.getArena().close();
		if (event.getMessage().length() > 0)
		{
			event.getArena().broadcastMessage(ChatColor.DARK_GREEN + event.getMessage());
		}
		if (event instanceof WinQuitEvent)
		{
			WinQuitEvent winEvent = (WinQuitEvent)event;
			event.getArena().getTeam().sendWinnerOutput(winEvent.getWinnerTeam().getTeamName());
		}
		event.getArena().getFightMode().stop();
		event.getArena().updateState(State.Spectate);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerMoveHandler(PlayerMoveEvent event)
	{
		if (event.getTo().getBlockX() == event.getFrom().getBlockX() &&
				event.getTo().getBlockY() == event.getFrom().getBlockY() &&
				event.getTo().getBlockZ() == event.getFrom().getBlockZ())
		{
			return;
		}
		Arena arenaFrom = this.plugin.getArenaManager().getArenaAtLocation(event.getFrom());
		Arena arenaTo = this.plugin.getArenaManager().getArenaAtLocation(event.getTo());
		if (arenaFrom != null)
		{
			arenaFrom.leave(event.getPlayer());
		}
		if (arenaTo != null)
		{
			arenaTo.join(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerJoinHandler(PlayerJoinEvent event)
	{
		Arena arenaTo = this.plugin.getArenaManager().getArenaAtLocation(event.getPlayer().getLocation());
		if (arenaTo != null)
		{
			arenaTo.join(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerQuitHandler(PlayerQuitEvent event)
	{
		Arena arenaFrom = this.plugin.getArenaManager().getArenaAtLocation(event.getPlayer().getLocation());
		if (arenaFrom != null)
		{
			arenaFrom.leave(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerTeleportHandler(PlayerTeleportEvent event)
	{
		Arena arenaFrom = this.plugin.getArenaManager().getArenaAtLocation(event.getFrom());
		Arena arenaTo = this.plugin.getArenaManager().getArenaAtLocation(event.getTo());
		if (arenaFrom != null)
		{
			arenaFrom.leave(event.getPlayer());
		}
		if (arenaTo != null)
		{
			arenaTo.join(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void entityDamgeHandler(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		final Player player = (Player)event.getEntity();
		final Arena arena = this.plugin.getArenaManager().getArenaAtLocation(player.getLocation());
		if (arena == null)
		{
			return;
		}
		if (arena.getTeam().getTeamOfPlayer(player) == null)
		{
			return;
		}
		if (arena.getState() != State.Running)
		{
			event.setCancelled(true);
			return;
		}
		if (event instanceof EntityDamageByEntityEvent)
		{
			checkTeamDamaging((EntityDamageByEntityEvent)event, player, arena);
		}
		
		this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable(){
			public void run()
			{
				arena.getScore().updateHealthOfPlayer(player);
			}
		});
	}
	
	private void checkTeamDamaging(EntityDamageByEntityEvent event, Player player, Arena arena)
	{
		Player damager = null;
		if (event.getDamager() instanceof Projectile
				&& ((Projectile)event.getDamager()).getShooter() instanceof Player)
		{
			damager = (Player) ((Projectile)event.getDamager()).getShooter();
		}
		else if (event.getDamager() instanceof Player)
		{
			damager = (Player) event.getDamager();
		}
		if (damager != null && arena.getTeam().getTeamOfPlayer(player).equals(arena.getTeam().getTeamOfPlayer(damager)))
		{
			damager.sendMessage("§7Du darfst keinen Spieler aus deinem eigenen Team Schaden zufügen.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void entityRegainHealthHandler(EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		final Player player = (Player)event.getEntity();
		final Arena arena = this.plugin.getArenaManager().getArenaAtLocation(player.getLocation());
		if (arena != null)
		{
			if (arena.getTeam().getTeamOfPlayer(player) != null)
			{
				this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable(){
					public void run()
					{
						arena.getScore().updateHealthOfPlayer(player);
					}
				});
			}
		}
	}
}
