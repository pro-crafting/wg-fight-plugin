package me.Postremus.WarGear.Arena;

import me.Postremus.WarGear.WarGear;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
	
	@EventHandler
	public void playerMoveHandler(PlayerMoveEvent event)
	{
		if (!event.getTo().getWorld().getName().equalsIgnoreCase(this.plugin.getRepo().getWorldName(this.arena)))
		{
			return;
		}
		boolean isInArena = this.arena.getArenaRegion().contains(BukkitUtil.toVector(event.getTo()));
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
	
	@EventHandler
	public void playerJoinHandler(PlayerJoinEvent event)
	{
		if (this.arena.getArenaRegion().contains(BukkitUtil.toVector(event.getPlayer().getLocation())))
		{
			this.arena.getScore().enterArena(event.getPlayer());
			this.arena.getPlayersInArena().add(event.getPlayer());
		}
	}
	
	@EventHandler
	public void playerQuitHandler(PlayerQuitEvent event)
	{
		if (this.arena.getArenaRegion().contains(BukkitUtil.toVector(event.getPlayer().getLocation())))
		{
			this.arena.getScore().leaveArena(event.getPlayer());
			this.arena.getPlayersInArena().remove(event.getPlayer());
		}
	}
	
	@EventHandler
	public void playerKickHandler(PlayerKickEvent event)
	{
		if (this.arena.getArenaRegion().contains(BukkitUtil.toVector(event.getPlayer().getLocation())))
		{
			this.arena.getScore().leaveArena(event.getPlayer());
			this.arena.getPlayersInArena().remove(event.getPlayer());
		}
	}
	
	@EventHandler
	public void playerTeleportHandler(PlayerTeleportEvent event)
	{
		if (this.arena.getArenaRegion().contains(BukkitUtil.toVector(event.getTo())))
		{
			this.arena.getScore().enterArena(event.getPlayer());
			this.arena.getPlayersInArena().add(event.getPlayer());
		}
	}
	
	@EventHandler
	public void entityDamgeHandler(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		if (this.arena.getTeam().getTeamOfPlayer((Player)event.getEntity()) != null)
		{
			this.arena.getScore().update();
		}
	}
	
	@EventHandler
	public void entityRegainHealthHandler(EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		if (this.arena.getTeam().getTeamOfPlayer((Player)event.getEntity()) != null)
		{
			this.arena.getScore().update();
		}
	}
}
