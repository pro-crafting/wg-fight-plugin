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

 public class KitMode extends FightBase{

	int counter;
	int taskId;
	
	public KitMode(WarGear plugin, Arena arena)
	{
		super(plugin, arena);
	}
	
	@Override
	public void start() {
		super.start();
		counter = 0;
		for (TeamMember member : this.arena.getTeam().getTeam1().getTeamMembers())
		{
			AdmincmdWrapper.giveKit(this.arena.getKit(), member.getPlayer(), this.plugin.getServer());
		}
		for (TeamMember member : this.arena.getTeam().getTeam2().getTeamMembers())
		{
			AdmincmdWrapper.giveKit(this.arena.getKit(), member.getPlayer(), this.plugin.getServer());
		}
		taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable(){
			@Override
			public void run() {
				finalStartCountdown();
			}          
		}, 0, 20);
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
			this.plugin.getServer().getScheduler().cancelTask(taskId);
			this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this.arena)).setDifficulty(Difficulty.EASY);
			this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
			this.arena.open();
		}
		counter++;
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public String getName() {
		return "kit";
	}
}
