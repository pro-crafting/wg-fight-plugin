package com.pro_crafting.mc.wg.modes;

import com.pro_crafting.mc.wg.group.GroupMember;
import com.pro_crafting.mc.wg.OfflineRunable;
import com.pro_crafting.mc.wg.Util;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class FightBase implements FightMode, Listener {

  protected WarGear plugin;
  protected Arena arena;
  protected OfflineRunable preparer;
  protected Runnable starter;

  public FightBase(WarGear plugin, Arena arena) {
    this.plugin = plugin;
    this.arena = arena;
  }

  public void start() {
    this.plugin.getServer().broadcastMessage(
        ChatColor.YELLOW + "Gleich: WarGear-Kampf in der " + this.arena.getName() + " Arena");
    this.arena.broadcastOutside("§7Mit §B\"/wgk warp " + this.arena.getName().toLowerCase()
        + "\" §7 kommst du in die Arena.");
    arena.getGroupManager().sendGroupOutput();

    OfflineRunable fightTeamPreparer = new OfflineRunable() {

      public void run(GroupMember member) {
        Player player = member.getPlayer();

        player.setGameMode(GameMode.SURVIVAL);
        Util.clearPlayer(player);
        Util.disableFly(player);
        Util.makeHealthy(player);
        Util.removePotionEffects(player);
        arena.teleport(player);
        if (preparer != null) {
          preparer.run(member);
        }
      }
    };

    this.plugin.getOfflineManager()
        .run(fightTeamPreparer, this.arena.getGroupManager().getGroup1());
    this.plugin.getOfflineManager()
        .run(fightTeamPreparer, this.arena.getGroupManager().getGroup2());
    starter.run();
  }

  public void stop() {
  }

  public String getName() {
    return "base, you stupid boy";
  }
}
