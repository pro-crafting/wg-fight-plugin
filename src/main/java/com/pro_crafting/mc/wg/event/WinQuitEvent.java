package com.pro_crafting.mc.wg.event;

import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.FightQuitReason;
import com.pro_crafting.mc.wg.arena.Arena;

public class WinQuitEvent extends FightQuitEvent {

  public WinQuitEvent(Arena arena, String message, Group winnerTeam, Group looserTeam,
                      FightQuitReason reason) {
    super(arena, message, winnerTeam, looserTeam, reason);
  }

  public Group getWinnerTeam() {
    return super.team1;
  }

  public Group getLooserTeam() {
    return super.team2;
  }
}
