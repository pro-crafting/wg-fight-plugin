package me.Postremus.WarGear.FightModes;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;

import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.TeamMember;

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
			this.plugin.getKitApi().giveKit(this.arena.getKit(), member.getPlayer());
		}
		for (TeamMember member : this.arena.getTeam().getTeam2().getTeamMembers())
		{
			this.plugin.getKitApi().giveKit(this.arena.getKit(), member.getPlayer());
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
			this.arena.broadcastMessage(ChatColor.GOLD + "60 Sekunden");
		}
		else if (counter == 10)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "50 Sekunden");
		}
		else if (counter == 20)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "40 Sekunden");
		}
		else if (counter == 30)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "30 Sekunden");
		}
		else if (counter == 40)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "20 Sekunden");
		}
		else if (counter == 45)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "15 Sekunden");
		}
		else if (counter == 50)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + "10 Sekunden");
		}
		else if (counter > 50 && 60-counter > 3)
		{
			int diff = 60-counter;
			this.arena.broadcastMessage(ChatColor.GOLD + "" + diff +" Sekunden");
		}
		else if (counter > 56 && 60-counter > 0)
		{
			int diff = 60-counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+ diff +" Sekunden");
		}
		else if (counter == 60)
		{
			this.plugin.getServer().getScheduler().cancelTask(taskId);
			this.arena.getRepo().getWorld().setDifficulty(Difficulty.EASY);
			this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
			this.arena.updateFightState(FightState.Running);
			arena.open();
		}
		counter++;
	}

	@Override
	public void stop() {
		super.stop();
		this.plugin.getServer().getScheduler().cancelTask(this.taskId);
	}

	@Override
	public String getName() {
		return "kit";
	}
}
