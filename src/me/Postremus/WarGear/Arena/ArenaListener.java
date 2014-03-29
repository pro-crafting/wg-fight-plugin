package me.Postremus.WarGear.Arena;

import me.Postremus.WarGear.DrawReason;
import me.Postremus.WarGear.ArenaState;
import me.Postremus.WarGear.TeamWinReason;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Events.DrawQuitEvent;
import me.Postremus.WarGear.Events.FightQuitEvent;
import me.Postremus.WarGear.Events.ArenaStateChangedEvent;
import me.Postremus.WarGear.Events.TeamWinQuitEvent;
import me.Postremus.WarGear.FightModes.KitMode;
import me.Postremus.WarGear.Team.TeamMember;
import me.Postremus.WarGear.Team.TeamNames;
import me.Postremus.WarGear.Team.WgTeam;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.sk89q.worldedit.bukkit.BukkitUtil;

public class ArenaListener implements Listener
{
	Arena arena;
	WarGear plugin;
	
	public ArenaListener(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
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
		boolean isInArena = this.arena.contains(event.getTo());
		if (!isInArena)
		{
			this.removePlayer(event.getPlayer());
		}
		else
		{
			this.addPlayer(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerJoinHandler(PlayerJoinEvent event)
	{
		if (this.arena.contains(event.getPlayer().getLocation()))
		{
			this.addPlayer(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerQuitHandler(PlayerQuitEvent event)
	{
		if (this.arena.contains(event.getPlayer().getLocation()))
		{
			this.removePlayer(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerKickHandler(PlayerKickEvent event)
	{
		if (this.arena.contains(event.getPlayer().getLocation()))
		{
			this.removePlayer(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void playerTeleportHandler(PlayerTeleportEvent event)
	{
		if (this.arena.contains(event.getTo()))
		{
			this.addPlayer(event.getPlayer());
		}
		else
		{
			this.removePlayer(event.getPlayer());
		}
	}
	
	private void addPlayer(Player p)
	{
		if (!this.arena.getPlayersInArena().contains(p))
		{
			this.arena.getScore().enterArena(p);
			this.arena.getPlayersInArena().add(p);
		}
	}
	
	private void removePlayer(Player p)
	{
		if (this.arena.getPlayersInArena().contains(p))
		{
			this.arena.getScore().leaveArena(p);
			this.arena.getPlayersInArena().remove(p);
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
		if (this.arena.getTeam().getTeamOfPlayer(player) == null)
		{
			return;
		}
		if (this.arena.getFightState() != ArenaState.Running)
		{
			event.setCancelled(true);
			return;
		}
		Player damager = null;
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent)event;
			if (damageEvent.getDamager() instanceof Projectile
					&& ((Projectile)damageEvent.getDamager()).getShooter() instanceof Player)
			{
				damager = (Player) ((Projectile)damageEvent.getDamager()).getShooter();
			}
			else if (damageEvent.getDamager() instanceof Player)
			{
				damager = (Player) damageEvent.getDamager();
			}
			if (player != null && this.arena.getTeam().getTeamOfPlayer(player).equals(this.arena.getTeam().getTeamOfPlayer(damager)))
			{
				damager.sendMessage("§7Du darfst keinen Spieler aus deinem eigenen Team Schaden zufügen.");
				event.setCancelled(true);
				return;
			}
		}
		
		this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable(){
			public void run()
			{
				ArenaListener.this.arena.getScore().updateHealthOfPlayer(player);
			}
		});
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void entityRegainHealthHandler(EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		final Player player = (Player)event.getEntity();
		if (this.arena.getTeam().getTeamOfPlayer(player) != null)
		{
			this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable(){
				public void run()
				{
					ArenaListener.this.arena.getScore().updateHealthOfPlayer(player);
				}
			});
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void quit(FightQuitEvent event)
	{
		if (!this.arena.equals(event.getArena()))
		{
			return;
		}
		event.getArena().close();
		event.getArena().updateFightState(ArenaState.Spectate);
		event.getArena().getFightMode().stop();
		if (event instanceof TeamWinQuitEvent)
		{
			TeamWinQuitEvent winEvent = (TeamWinQuitEvent)event;
			String toBroadcast = "";
			if (winEvent.getReason() == TeamWinReason.Death)
			{
				toBroadcast = ChatColor.DARK_GREEN + "Jeder aus dem ["+winEvent.getLooserTeam().getTeamName().toString().toUpperCase()+"] ist tot.";
			}
			event.getArena().broadcastMessage(toBroadcast);
			event.getArena().getTeam().GenerateWinnerTeamOutput(winEvent.getWinnerTeam().getTeamName());
		}
		else if (event instanceof DrawQuitEvent)
		{
			DrawQuitEvent drawEvent = (DrawQuitEvent)event;
			if (drawEvent.getReason() == DrawReason.Time)
			{
				event.getArena().broadcastMessage(ChatColor.DARK_GREEN + "Zeit abgelaufen - Unentschieden");
			}
		}
	}
	
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void fightStateChangedHandler(ArenaStateChangedEvent event)
	{
		if (!event.getArena().equals(this.arena))
		{
			return;
		}
		if (event.getTo() == ArenaState.Setup)
		{
			for (Player p : this.arena.getPlayersInArena())
			{
				this.arena.getScore().enterArena(p);
			}
		}
		if (event.getTo() == ArenaState.Idle)
		{
			this.arena.getTeam().quitFight();
			this.arena.setFightMode(new KitMode(this.plugin, this.arena));
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void asyncPlayerChatHandler(AsyncPlayerChatEvent event)
	{
		WgTeam team = this.arena.getTeam().getTeamOfPlayer(event.getPlayer());
		String color = "§7";
		
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
		else if (!this.arena.contains(event.getPlayer().getLocation()))
		{
			return;
		}
		event.setFormat(color+"["+this.arena.getArenaName()+"]"+event.getFormat());
	}
}
