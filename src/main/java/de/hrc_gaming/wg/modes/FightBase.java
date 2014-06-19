package de.hrc_gaming.wg.modes;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import de.hrc_gaming.wg.FightMode;
import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.Arena;

public abstract class FightBase implements FightMode, Listener
{
	protected WarGear plugin;
	protected Arena arena;
	
	public FightBase(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
	}
	
	public void start() { 
		this.plugin.getServer().broadcastMessage(ChatColor.YELLOW+"Gleich: WarGear-Kampf in der "+this.arena.getName()+" Arena");
		this.arena.broadcastOutside("§7Mit §B\"/wgk warp "+this.arena.getName().toLowerCase()+"\" §7 kommst du in die Arena.");
		this.arena.getTeam().prepareFightTeams();
	}

	public void stop() 
	{
		PlayerMoveEvent.getHandlerList().unregister(this);
	}

	public String getName() {
		return "base, you stupid boy";
	}

	@EventHandler
	public void playerMoveHandler(PlayerMoveEvent event)
	{
		if (event.getTo().getY() > this.arena.getRepo().getGroundHeight())
		{
			return;
		}
		if (!arena.contains(event.getTo()))
		{
			return;
		}
		if (this.arena.getTeam().getTeamOfPlayer(event.getPlayer()) != null)
		{
			event.getPlayer().damage(arena.getRepo().getGroundDamage());
		}
	}
}
