package de.pro_crafting.wg.ui;

import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.event.DrawQuitEvent;

public class ArenaTimerRunnable implements Runnable {
	private Arena arena;
	private WarGear plugin;
	private int time;
	
	public ArenaTimerRunnable(WarGear plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.time = this.arena.getRepo().getScoreboardTime();
	}
	
	public void run() {
		this.plugin.getScoreboard().updateTime(arena, time);
		if (this.time == 0) {
			DrawQuitEvent draw = new DrawQuitEvent(this.arena, "Zeit abgelaufen - Unentschieden", this.arena.getGroupManager().getTeam1(), this.arena.getGroupManager().getTeam2(), FightQuitReason.Time);
			this.plugin.getServer().getPluginManager().callEvent(draw);
			this.plugin.getScoreboard().stopTimer(this.arena);
		}
		this.time -= 1;
	}
	
}
