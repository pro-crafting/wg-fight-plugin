package me.Postremus.WarGear.Events;

import me.Postremus.WarGear.TeamWinReason;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.WgTeam;

public class WinQuitEvent extends FightQuitEvent
{
	private WgTeam winnerTeam;
	private WgTeam looserTeam;
	private TeamWinReason reason;
	
	public WinQuitEvent(Arena arena, String message, WgTeam winnerTeam, WgTeam looserTeam, TeamWinReason reason)
	{
		super(arena, message);
		this.winnerTeam = winnerTeam;
		this.looserTeam = looserTeam;
		this.reason = reason;
	}
    
    public WgTeam getWinnerTeam()
    {
    	return this.winnerTeam;
    }
    
    public WgTeam getLooserTeam()
    {
    	return this.looserTeam;
    }
    
    public TeamWinReason getReason()
    {
    	return this.reason;
    }
}
