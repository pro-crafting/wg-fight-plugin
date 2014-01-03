package me.Postremus.WarGear.FightModes;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;

import me.Postremus.WarGear.AdmincmdWrapper;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.TeamMember;
import me.Postremus.WarGear.TeamNames;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;

import java.util.Timer;
import java.util.TimerTask;

 public class KitMode implements IFightMode, Listener{

	WarGear plugin;
	Arena arena;
	Timer timer;
	int counter;
	
	public KitMode(WarGear plugin, Arena arena)
	{
		this.plugin = plugin;
		this.arena = arena;
		timer = new Timer();
	}
	
	@Override
	public void start() {
		this.plugin.getServer().broadcastMessage(ChatColor.YELLOW+"Gleich: WarGear-Kampf in der "+this.arena.getArenaName()+" Arena");
		for (TeamMember player : this.arena.getTeam().getTeamMembers())
		{
			player.getPlayer().getInventory().clear();
			player.getPlayer().getInventory().setArmorContents(null);
		    AdmincmdWrapper.giveKit(this.arena.getKit(), player.getPlayer(), this.plugin.getServer());
		    
		    player.getPlayer().teleport(this.plugin.getRepo().getWarpForTeam(player.getTeam(), this.arena), TeleportCause.PLUGIN);
		    player.getPlayer().setGameMode(GameMode.SURVIVAL);
			AdmincmdWrapper.disableFly(player.getPlayer());
			AdmincmdWrapper.heal(player.getPlayer());
			for (PotionEffect effect : player.getPlayer().getActivePotionEffects())
			{
				player.getPlayer().removePotionEffect(effect.getType());
			}
		}
		counter = 0;
		timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
	         public void run() {
				finalStartCountdown();          
	         }
		}, 0, 1000);
	}
	
	public void finalStartCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.YELLOW+"Bitte alle Teilnehmer in ihre Wargears");
			this.arena.broadcastMessage(ChatColor.YELLOW+"Fight startet in:");
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "60 Sekunden");
		}
		else if (counter == 10)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "50 Sekunden");
		}
		else if (counter == 20)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "40 Sekunden");
		}
		else if (counter == 30)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "30 Sekunden");
		}
		else if (counter == 40)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "20 Sekunden");
		}
		else if (counter == 45)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "15 Sekunden");
		}
		else if (counter == 50)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "10 Sekunden");
		}
		else if (counter > 50 && 60-counter > 3)
		{
			int diff = 60-counter;
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "" + diff +" Sekunden");
		}
		else if (counter > 56 && 60-counter > 0)
		{
			int diff = 60-counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+ diff +" Sekunden");
		}
		else if (counter == 60)
		{
			timer.cancel();
			this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this.arena)).setDifficulty(Difficulty.EASY);
			this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
			this.arena.open();
		}
		counter++;
	}

	@Override
	public void stop() {
		timer.cancel();
		this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this.arena)).setDifficulty(Difficulty.PEACEFUL);
		PlayerMoveEvent.getHandlerList().unregister(this);
	}

	@Override
	public String getName() {
		return "kit";
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
		if (this.arena.getTeam().isPlayerInTeam(event.getPlayer().getName(), TeamNames.Team1) || this.arena.getTeam().isPlayerInTeam(event.getPlayer().getName(), TeamNames.Team2))
		{
			event.getPlayer().damage(1);
		}
	}
}
