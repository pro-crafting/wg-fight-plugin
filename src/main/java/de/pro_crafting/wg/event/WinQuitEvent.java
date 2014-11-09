package de.pro_crafting.wg.event;

import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.group.WgTeam;

public class WinQuitEvent extends FightQuitEvent
{	
	public WinQuitEvent(Arena arena, String message, WgTeam winnerTeam, WgTeam looserTeam, FightQuitReason reason)
	{
		super(arena, message, winnerTeam, looserTeam, reason);
	}
    
    public WgTeam getWinnerTeam()
    {
    	return super.team1;
    }
    
    public WgTeam getLooserTeam()
    {
    	return super.team2;
    }
}
