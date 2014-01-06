package me.Postremus.WarGear.FightModes;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;

import me.Postremus.WarGear.AdmincmdWrapper;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.TeamMember;
import me.Postremus.WarGear.Team.TeamNames;

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
		if (event.getTo().getY() > this.plugin.getRepo().getGroundHeight(this.arena))
		{
			return;
		}
		if (!this.plugin.getRepo().getArenaAtLocation(event.getTo()).equalsIgnoreCase(this.arena.getArenaName()))
		{
			return;
		}
		if (this.arena.getTeam().getTeamOfPlayer(event.getPlayer()) != null)
		{
			event.getPlayer().damage(1);
		}
	}
}
