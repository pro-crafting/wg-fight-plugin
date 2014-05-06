package de.hrc_gaming.wg.event;

import de.hrc_gaming.wg.FightQuitReason;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.team.WgTeam;

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
