package de.pro_crafting.wg.event;

import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.team.WgTeam;

public class DrawQuitEvent extends FightQuitEvent{
	
	public DrawQuitEvent(Arena arena, String message, WgTeam team1, WgTeam team2, FightQuitReason reason) {
		super(arena, message, team1, team2, reason);
	}

	public WgTeam getTeam1()
	{
		return super.team1;
	}
	
	public WgTeam getTeam2()
	{
		return super.team2;
	}
}
