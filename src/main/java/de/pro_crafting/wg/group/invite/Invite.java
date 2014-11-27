package de.pro_crafting.wg.group.invite;

import java.util.UUID;

import de.pro_crafting.wg.group.PlayerGroupKey;

public class Invite {
	private PlayerGroupKey groupKey;
	private UUID invited;
	private int remainigTime;
	
	public Invite(PlayerGroupKey groupKey, UUID invited, int time) {
		this.groupKey = groupKey;
		this.invited = invited;
		this.remainigTime = time;
	}
	
	public PlayerGroupKey getGroupKey() {
		return this.groupKey;
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
