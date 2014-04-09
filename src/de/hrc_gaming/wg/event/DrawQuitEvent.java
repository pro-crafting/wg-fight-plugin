package de.hrc_gaming.wg.event;

import de.hrc_gaming.wg.DrawReason;
import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.team.WgTeam;

public class DrawQuitEvent extends FightQuitEvent{
	private WgTeam team1;
	private WgTeam team2;
	private DrawReason reason;
	
	public DrawQuitEvent(Arena arena, String message, WgTeam team1, WgTeam team2, DrawReason reason) {
		super(arena, message);
		this.team1 = team1;
		this.team2 = team2;
		this.reason = reason;
	}

	public WgTeam getTeam1()
	{
		return this.team1;
	}
	
	public WgTeam getTeam2()
	{
		return this.team2;
	}
	
	public DrawReason getReason()
	{
		return this.reason;
	}
}
