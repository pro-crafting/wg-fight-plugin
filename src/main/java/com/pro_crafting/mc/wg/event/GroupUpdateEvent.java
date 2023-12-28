package com.pro_crafting.mc.wg.event;

import com.pro_crafting.mc.wg.group.GroupMember;
import com.pro_crafting.mc.wg.group.PlayerGroupKey;

import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
@ToString
public class GroupUpdateEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Collection<GroupMember> oldMembers;
  private Collection<GroupMember> newMembers;
  private PlayerGroupKey playerGroupKey;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public Collection<GroupMember> getOldMembers() {
    return Collections.unmodifiableCollection(oldMembers);
  }

  public Collection<GroupMember> getNewMembers() {
    return Collections.unmodifiableCollection(newMembers);
  }
}
