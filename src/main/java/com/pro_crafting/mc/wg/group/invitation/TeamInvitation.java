package com.pro_crafting.mc.wg.group.invitation;

import com.pro_crafting.mc.wg.group.GroupMember;
import com.pro_crafting.mc.wg.group.PlayerGroupKey;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.arena.State;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamInvitation extends Invitation {

  private WarGear plugin;

  public TeamInvitation(GroupMember inviter, UUID invited, int remainigTime, InvitationType type) {
    super(inviter, invited, remainigTime, type);
    this.plugin = WarGear.getPlugin(WarGear.class);
  }

  @Override
  public void sendInvitationDescription() {
    Player invited = Bukkit.getPlayer(getInvited());
    if (inviter.isOnline() && invited != null) {
      inviter.getPlayer().sendMessage("§7Du hast §B" + invited.getDisplayName() + "§7 eingeladen.");
      invited.sendMessage("§B" + inviter.getName() + "§7 hat dich in sein Team eingeladen.");
    }
    super.sendInvitationDescription();
  }

  @Override
  public void sendAcceptMessages(Player invited) {
    super.sendAcceptMessages(invited);
    if (invited != null) {
      PlayerGroupKey groupKey = this.plugin.getArenaManager().getGroup(inviter.getPlayer());
      Arena arena = groupKey.getArena();
      if (arena.getState() != State.Setup) {
        invited.sendMessage("§7Dieser Fight läuft bereits.");
        return;
      }

      invited.sendMessage("§7Du hast die Einladung von §B" + inviter.getName() + "§7 angenommen.");
      invited.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
      if (inviter.isOnline()) {
        inviter.getPlayer()
            .sendMessage("§B" + invited.getDisplayName() + "§7 ist deinem Team beigetreten.");
      }

      groupKey.getGroup().add(invited, false);
      this.plugin.getScoreboard()
          .addTeamMember(arena, groupKey.getGroup().getMember(invited), groupKey.getRole());
    }
  }
}
