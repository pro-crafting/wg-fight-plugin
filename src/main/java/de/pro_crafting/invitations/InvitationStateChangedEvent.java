package de.pro_crafting.invitations;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InvitationStateChangedEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Invitation invitation;
	private InvitationState from;
	private InvitationState to;
	
	public InvitationStateChangedEvent(Invitation invitation, InvitationState from, InvitationState to)
	{
		this.invitation = invitation;
		this.from = from;
		this.to = to;
	}
	
	public Invitation getInivite()
	{
		return this.invitation;
	}
	
	public InvitationState getFrom()
	{
		return this.from;
	}
	
	public InvitationState getTo()
	{
		return this.to;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
