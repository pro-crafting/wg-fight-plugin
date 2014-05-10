package de.hrc_gaming.wg.modes;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;

import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.arena.State;
import de.hrc_gaming.wg.team.TeamMember;

 public class KitMode extends FightBase{

	private int counter;
	private int taskId;
	
	public KitMode(WarGear plugin, Arena arena)
	{
		super(plugin, arena);
	}
	
	@Override
	public void start() {
		super.start();
		counter = 0;
		for (TeamMember member : this.arena.getTeam().getTeam1().getTeamMembers().values())
		{
			this.plugin.getKitApi().giveKit(this.arena.getKit(), member.getPlayer());
		}
		for (TeamMember member : this.arena.getTeam().getTeam2().getTeamMembers().values())
		{
			this.plugin.getKitApi().giveKit(this.arena.getKit(), member.getPlayer());
		}
		taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable(){
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
			this.arena.updateState(State.Running);
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
