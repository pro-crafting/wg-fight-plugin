package de.pro_crafting.wg.modes;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import de.pro_crafting.wg.FightMode;
import de.pro_crafting.wg.OfflineRunable;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.team.TeamMember;

public abstract class FightBase implements FightMode, Listener
{
	protected WarGear plugin;
	protected Arena arena;
	protected OfflineRunable preparer;
	protected Runnable starter;
	
	public FightBase(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
	}
	
	public void start() { 
		this.plugin.getServer().broadcastMessage(ChatColor.YELLOW+"Gleich: WarGear-Kampf in der "+this.arena.getName()+" Arena");
		this.arena.broadcastOutside("§7Mit §B\"/wgk warp "+this.arena.getName().toLowerCase()+"\" §7 kommst du in die Arena.");
		
		OfflineRunable fightTeamPreparer = new OfflineRunable() {
			
			public void run(TeamMember member) {
				Player player = member.getPlayer();
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				
				player.setGameMode(GameMode.SURVIVAL);
				Util.disableFly(player);
				Util.makeHealthy(player);
				Util.removePotionEffects(player);
				arena.teleport(player);
				if (preparer != null) {
					preparer.run(member);
				}
			}
		};
		
		this.plugin.getOfflineManager().run(fightTeamPreparer, this.arena.getTeam().getTeam1());
		this.plugin.getOfflineManager().run(fightTeamPreparer, this.arena.getTeam().getTeam2());
		starter.run();
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
		TeamMember member = this.arena.getTeam().getTeamMember(event.getPlayer());
		if (member != null && member.isAlive())
		{
			event.getPlayer().damage(arena.getRepo().getGroundDamage());
			arena.getScore().updateHealthOfPlayer(event.getPlayer());
		}
	}
}
