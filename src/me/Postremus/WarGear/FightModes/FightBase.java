package me.Postremus.WarGear.FightModes;

import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public abstract class FightBase implements IFightMode, Listener
{
	protected WarGear plugin;
	protected Arena arena;
	
	public FightBase(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
	}
	
	@Override
	public void start() {
		this.plugin.getServer().broadcastMessage(ChatColor.YELLOW+"Gleich: WarGear-Kampf in der "+this.arena.getArenaName()+" Arena");
		this.arena.broadcastOutside("§7Mit §B\"/wgk warp "+this.arena.getArenaName().toLowerCase()+"\" §7 kommst du in die Arena.");
		this.arena.getTeam().prepareFightTeams();
	}

	@Override
	public void stop() 
	{
		PlayerMoveEvent.getHandlerList().unregister(this);
	}

	@Override
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
			event.getPlayer().damage(1);
		}
	}
}
