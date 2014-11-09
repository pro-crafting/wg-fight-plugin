package de.pro_crafting.wg.event;

import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.group.Group;

public class DrawQuitEvent extends FightQuitEvent{
	
	public DrawQuitEvent(Arena arena, String message, Group team1, Group team2, FightQuitReason reason) {
		super(arena, message, team1, team2, reason);
	}

	public Group getTeam1()
	{
		return super.team1;
	}
	
	public Group getTeam2()
	{
		return super.team2;
	}
}
