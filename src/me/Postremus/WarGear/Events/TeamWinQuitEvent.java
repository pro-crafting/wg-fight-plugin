package me.Postremus.WarGear.Events;

import me.Postremus.WarGear.TeamWinReason;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.WgTeam;

public class TeamWinQuitEvent extends FightQuitEvent
{
	private WgTeam winnerTeam;
	private WgTeam looserTeam;
	private TeamWinReason reason;
	
	public TeamWinQuitEvent(WgTeam winnerTeam, WgTeam looserTeam, Arena arena, TeamWinReason reason)
	{
		super(arena);
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
