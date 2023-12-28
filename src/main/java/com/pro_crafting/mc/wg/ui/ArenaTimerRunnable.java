package com.pro_crafting.mc.wg.ui;

import com.pro_crafting.mc.wg.event.DrawQuitEvent;
import com.pro_crafting.mc.wg.FightQuitReason;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;

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
      DrawQuitEvent draw = new DrawQuitEvent(this.arena, "Zeit abgelaufen - Unentschieden",
          this.arena.getGroupManager().getGroup1(), this.arena.getGroupManager().getGroup2(),
          FightQuitReason.Time);
      this.plugin.getServer().getPluginManager().callEvent(draw);
      this.plugin.getScoreboard().stopTimer(this.arena);
    }
    this.time -= 1;
  }

}
