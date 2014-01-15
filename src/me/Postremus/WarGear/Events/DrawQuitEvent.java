package me.Postremus.WarGear.Events;

import me.Postremus.WarGear.DrawReason;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.WgTeam;

public class DrawQuitEvent extends FightQuitEvent{
	private WgTeam team1;
	private WgTeam team2;
	private DrawReason reason;
	
	public DrawQuitEvent(Arena arena, WgTeam team1, WgTeam team2) {
		super(arena);
		this.team1 = team1;
		this.team2 = team2;
		reason = DrawReason.Time;
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