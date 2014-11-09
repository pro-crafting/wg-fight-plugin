package de.pro_crafting.wg.event;

import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.group.Group;

public class WinQuitEvent extends FightQuitEvent
{	
	public WinQuitEvent(Arena arena, String message, Group winnerTeam, Group looserTeam, FightQuitReason reason)
	{
		super(arena, message, winnerTeam, looserTeam, reason);
	}
    
    public Group getWinnerTeam()
    {
    	return super.team1;
    }
    
    public Group getLooserTeam()
    {
    	return super.team2;
    }
}
