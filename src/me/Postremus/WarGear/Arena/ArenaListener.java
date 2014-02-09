package me.Postremus.WarGear.Arena;

import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.WarGear;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void playerMoveHandler(PlayerMoveEvent event)
	{
		if (!event.getTo().getWorld().getName().equalsIgnoreCase(this.arena.getRepo().getWorld().getName()))
		{
			return;
		}
		boolean isInArena = this.arena.getRepo().getArenaRegion().contains(BukkitUtil.toVector(event.getTo()));
		boolean isPlayerInArena = this.arena.getPlayersInArena().contains(event.getPlayer());
		if (!isInArena && isPlayerInArena)
		{
			this.arena.getScore().leaveArena(event.getPlayer());
			this.arena.getPlayersInArena().remove(event.getPlayer());
		}
		else if (isInArena && !isPlayerInArena)
		{
			this.arena.getScore().enterArena(event.getPlayer());
			this.arena.getPlayersInArena().add(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void playerJoinHandler(PlayerJoinEvent event)
	{
		if (this.arena.getRepo().getArenaRegion().contains(BukkitUtil.toVector(event.getPlayer().getLocation())))
		{
			this.arena.getScore().enterArena(event.getPlayer());
			this.arena.getPlayersInArena().add(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void playerQuitHandler(PlayerQuitEvent event)
	{
		if (this.arena.getRepo().getArenaRegion().contains(BukkitUtil.toVector(event.getPlayer().getLocation())))
		{
			this.arena.getScore().leaveArena(event.getPlayer());
			this.arena.getPlayersInArena().remove(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void playerKickHandler(PlayerKickEvent event)
	{
		if (this.arena.getRepo().getArenaRegion().contains(BukkitUtil.toVector(event.getPlayer().getLocation())))
		{
			this.arena.getScore().leaveArena(event.getPlayer());
			this.arena.getPlayersInArena().remove(event.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void playerTeleportHandler(PlayerTeleportEvent event)
	{
		if (this.arena.getRepo().getArenaRegion().contains(BukkitUtil.toVector(event.getTo())))
		{
			this.arena.getScore().enterArena(event.getPlayer());
			this.arena.getPlayersInArena().add(event.getPlayer());
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
		if (this.arena.getFightState() != FightState.Running)
		{
			//event.setCancelled(true);
			//return;
		}
		this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable(){
			public void run()
			{
				ArenaListener.this.arena.getScore().updateHealthOfPlayer(player, (int)player.getHealth());
			}
		});
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
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
					System.out.println("Health: "+player.getHealth());
					ArenaListener.this.arena.getScore().updateHealthOfPlayer(player, (int)player.getHealth());
				}
			});
		}
		else
		{
			System.out.println("Kein Team");
		}
	}
}
