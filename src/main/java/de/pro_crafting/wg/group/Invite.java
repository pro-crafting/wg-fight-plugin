package de.pro_crafting.wg.group;

import java.util.UUID;

public class Invite {
	private UUID inviter;
	private UUID invited;
	private int remainigTime;
	
	public Invite(UUID inviter, UUID invited, int time) {
		this.inviter = inviter;
		this.invited = invited;
		this.remainigTime = time;
	}
	
	public UUID getInviter() {
		return this.inviter;
	}
	
	public UUID getInvited() {
		return this.invited;
	}

	public int getRemainigTime() {
		return this.remainigTime;
	}

	public void setRemainigTime(int remainigTime) {
		this.remainigTime = remainigTime;
	}
	
}
