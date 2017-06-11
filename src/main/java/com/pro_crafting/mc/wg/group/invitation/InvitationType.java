package com.pro_crafting.mc.wg.group.invitation;

import com.pro_crafting.mc.wg.group.GroupMember;

import java.util.UUID;

public class InvitationType {

  public static final InvitationType Team = new InvitationType();

  public Invitation create(GroupMember inviter, UUID invited, int remainigTime) {
    return this == InvitationType.Team ? new TeamInvitation(inviter, invited, remainigTime, this)
        : null;
  }
}
