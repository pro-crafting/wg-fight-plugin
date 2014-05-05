package de.hrc_gaming.invitations;

import org.bukkit.Bukkit;

public class Invitation {
	private String fromPlayer;
	private String toPlayer;
	private int duration;
	private String name;
	private InvitationState state;
	
	public Invitation(String fromPlayer, String toPlayer, int duration, String name)
	{
		this.fromPlayer = fromPlayer;
		this.toPlayer = toPlayer;
		this.duration = duration;
		this.name = name;
		this.state = InvitationState.Open;
	}
	
	public String getFromPlayer()
	{
		return this.fromPlayer;
	}
	
	public String getToPlayer()
	{
		return this.toPlayer;
	}
	
	public int getDuration()
	{
		return this.duration;
	}
	
	public void setDuration(int duration)
	{
		this.duration = duration;
	}
	
	public InvitationState getState()
	{
		return this.state;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void updateState(InvitationState to)
	{
		InvitationStateChangedEvent event = new InvitationStateChangedEvent(this, this.state, to);
		Bukkit.getServer().getPluginManager().callEvent(event);
		this.state = to;
	}
}
