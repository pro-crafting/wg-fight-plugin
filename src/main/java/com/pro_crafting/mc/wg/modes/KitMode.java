package com.pro_crafting.mc.wg.modes;

import com.pro_crafting.mc.kit.KitProvider;
import com.pro_crafting.mc.wg.group.GroupMember;
import com.pro_crafting.mc.wg.OfflineRunable;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.arena.State;
import org.bukkit.ChatColor;

public class KitMode extends FightBase {

  private int counter;
  private int taskId;

  public KitMode(WarGear plugin, Arena arena) {

    super(plugin, arena);
    preparer = new OfflineRunable() {
      KitProvider kit = KitMode.this.plugin.getRepo().getKit();

      public void run(GroupMember member) {
        if (kit.existsKit(KitMode.this.arena.getKit())) {
          kit.distribute(KitMode.this.arena.getKit(), member.getPlayer());
        } else {
          KitMode.this.plugin.getLogger()
              .severe("Fight in " + KitMode.this.arena.getName() + " could not be started " +
                  "with kitMode - kit " + KitMode.this.arena.getKit() + " could not be found!");
        }
      }
    };
    starter = new Runnable() {

      public void run() {
        KitMode.this.taskId = KitMode.this.plugin.getServer().getScheduler()
            .scheduleSyncRepeatingTask(KitMode.this.plugin, new Runnable() {
              public void run() {
                finalStartCountdown();
              }
            }, 0, 20);
      }
    };
  }

  @Override
  public void start() {
    counter = 0;
    super.start();
  }

  public void finalStartCountdown() {
    if (counter == 0) {
      this.arena.broadcastMessage(ChatColor.YELLOW + "Bitte alle Teilnehmer in ihre Wargears");
      this.arena.broadcastMessage(ChatColor.YELLOW + "Fight startet in:");
      this.arena.broadcastMessage(ChatColor.GOLD + "60 Sekunden");
    } else if (counter == 10) {
      this.arena.broadcastMessage(ChatColor.GOLD + "50 Sekunden");
    } else if (counter == 20) {
      this.arena.broadcastMessage(ChatColor.GOLD + "40 Sekunden");
    } else if (counter == 30) {
      this.arena.broadcastMessage(ChatColor.GOLD + "30 Sekunden");
    } else if (counter == 40) {
      this.arena.broadcastMessage(ChatColor.GOLD + "20 Sekunden");
    } else if (counter == 45) {
      this.arena.broadcastMessage(ChatColor.GOLD + "15 Sekunden");
    } else if (counter == 50) {
      this.arena.broadcastMessage(ChatColor.GOLD + "10 Sekunden");
    } else if (counter > 50 && 60 - counter > 3) {
      int diff = 60 - counter;
      this.arena.broadcastMessage(ChatColor.GOLD + "" + diff + " Sekunden");
    } else if (counter > 56 && 60 - counter > 0) {
      int diff = 60 - counter;
      this.arena.broadcastMessage(ChatColor.AQUA + "" + diff + " Sekunden");
    } else if (counter == 60) {
      this.plugin.getServer().getScheduler().cancelTask(taskId);
      this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
      this.arena.updateState(State.Running);
      arena.open();
    }
    counter++;
  }

  @Override
  public void stop() {
    super.stop();
    this.plugin.getServer().getScheduler().cancelTask(this.taskId);
  }

  @Override
  public String getName() {
    return "kit";
  }
}
