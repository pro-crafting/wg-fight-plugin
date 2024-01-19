package com.pro_crafting.mc.wg.arena;

import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.OfflineRunnable;
import com.pro_crafting.mc.wg.Util;
import com.pro_crafting.mc.wg.WarGear;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class SpectatorMode {

  private WarGear plugin;
  private Arena arena;
  private int counter;
  private BukkitTask task;

  public SpectatorMode(WarGear plugin, Arena arena) {
    this.plugin = plugin;
    this.arena = arena;

  }

  public void start() {
    this.arena.broadcastMessage(ChatColor.GOLD + "Begutachtet die WarGears!");
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
      prepareTeamSpectating(SpectatorMode.this.arena.getGroupManager().getGroup1());
      prepareTeamSpectating(SpectatorMode.this.arena.getGroupManager().getGroup2());
    }, 1);
    counter = 0;
    task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::spectateEndCountdown, 0, 20);
  }

  private void spectateEndCountdown() {
    int time = this.arena.getRepo().getSpectatorModeTime();
    int diff = time - counter;
    if (counter == time) {
      finishTeamSpectating(this.arena.getGroupManager().getGroup1());
      finishTeamSpectating(this.arena.getGroupManager().getGroup2());
      this.arena.broadcastMessage(ChatColor.AQUA + "Zeit vorbei!");
      task.cancel();
      this.arena.updateState(State.Resetting);
    } else if (counter == 0) {
      this.arena.broadcastMessage(ChatColor.GOLD + "Zeit endet in");
      this.arena.broadcastMessage(ChatColor.GOLD + "" + time + " Sekunden");
    } else if (counter % 30 == 0 && counter < time) {
      this.arena.broadcastMessage(ChatColor.GOLD + "" + diff + " Sekunden");
    } else if (diff > 0 && diff < 4) {
      this.arena.broadcastMessage(ChatColor.AQUA + "" + diff + " Sekunden");
    } else if (diff > 3 && diff < 6) {
      this.arena.broadcastMessage(ChatColor.GOLD + "" + diff + " Sekunden");
    }
    counter++;
  }

  private void prepareTeamSpectating(Group team) {
    OfflineRunnable teamSpectatingPreparer = member -> {
      Player player = member.getPlayer();
      arena.teleport(player);
      Util.enableFly(player);
      Util.clearPlayer(player);
    };
    this.plugin.getOfflineManager().queueOnlineExecution(teamSpectatingPreparer, team);
  }

  private void finishTeamSpectating(Group team) {
    OfflineRunnable teamSpectatingFinisher = member -> {
      Player player = member.getPlayer();
      Util.disableFly(player);
      player.setGameMode(GameMode.SURVIVAL);
    };
    this.plugin.getOfflineManager().queueOnlineExecution(teamSpectatingFinisher, team);
  }
}
