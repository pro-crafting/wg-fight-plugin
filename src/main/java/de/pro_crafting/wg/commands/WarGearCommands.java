package de.pro_crafting.wg.commands;

import de.pro_crafting.commandframework.Command;
import de.pro_crafting.commandframework.CommandArgs;
import de.pro_crafting.commandframework.Completer;
import de.pro_crafting.wg.FightQuitReason;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.event.DrawQuitEvent;
import de.pro_crafting.wg.event.FightQuitEvent;
import de.pro_crafting.wg.event.WinQuitEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarGearCommands {

  private WarGear plugin;

  public WarGearCommands(WarGear plugin) {
    this.plugin = plugin;
  }

  @Command(name = "wgk", aliases = {
      "wgk.help"}, description = "Zeigt die Hilfe an.", usage = "/wgk", permission = "wargear.help")
  public void WarGear(CommandArgs args) {
    CommandSender sender = args.getPlayer();
    sender.sendMessage("§c§LKein passender Befehl gefunden!");
    sender.sendMessage("§B/wgk team ...");
    sender.sendMessage("§B/wgk arena ...");
    sender.sendMessage("§B/wgk kit <kitName>");
    sender.sendMessage("§B/wgk warp <arenaname> [playername]");
    sender.sendMessage("§B/wgk quit [team1|team2]");
    sender.sendMessage("§B/wgk reload");
    sender.sendMessage("§B/wgk start");
  }

  @Command(name = "wgk.reload", description = "Reloadet die Config.", usage = "/wgk reload", permission = "wargear.reload")
  public void reload(CommandArgs args) {
    this.plugin.reloadConfig();
    this.plugin.getServer().getPluginManager().disablePlugin(plugin);
    this.plugin.getServer().getPluginManager().enablePlugin(plugin);
    args.getSender().sendMessage("Plugin wurde gereloadet.");
  }

  @Command(name = "wgk.warp", description = "Teleport zu der Arena.", usage = "/wgk warp <arenaname> [player]", permission = "wargear.warp")
  public void warp(CommandArgs args) {
    if (args.length() < 1) {
      args.getSender().sendMessage("§cEs muss eine Arena angegeben werden.");
      return;
    }
    String arenaName = args.getArgs(0);
    Arena arena = this.plugin.getArenaManager().getArena(arenaName);
    if (arena == null) {
      args.getSender().sendMessage("§cDie Arena " + arenaName + " existiert nicht.");
      return;
    }

    Player toWarp = args.getPlayer();
    if (args.length() >= 2) {
      if (!args.getSender().hasPermission("wargear.warp.other")) {
        args.getSender().sendMessage("§cDu hast keine Rechte dafür.");
        return;
      }
      if (this.plugin.getServer().getPlayer(args.getArgs(1)) == null) {
        args.getSender().sendMessage("§c" + args.getArgs(1) + " Ist nicht online.");
        return;
      }
      toWarp = this.plugin.getServer().getPlayer(args.getArgs(1));
    }

    if (toWarp == null) {
      args.getSender().sendMessage("§cEs muss ein Spieler angegeben werden.");
      return;
    }

    arena.teleport(toWarp);
  }

  @Completer(name = "wgk.warp")
  public List<String> completeWarpName(CommandArgs args) {
    if (args.getArgs().length > 1) {
      return null;
    }
    String startWith = "";
    if (args.getArgs().length == 1) {
      startWith = args.getArgs(0);
    }

    List<String> ret = new ArrayList<String>();
    for (String arenaName : this.plugin.getArenaManager().getArenaNames()) {
      if (arenaName.startsWith(startWith)) {
        ret.add(arenaName);
      }
    }
    return ret;
  }

  @Command(name = "wgk.kit", description = "Legt das Kit für den Fight fest.", usage = "/wgk kit name", permission = "wargear.kit")
  public void kit(CommandArgs args) {
    CommandSender sender = args.getSender();
    Arena arena = Util.getArenaFromSender(plugin, sender, args.getArgs());
    if (arena == null) {
      sender.sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }
    if (args.length() == 0) {
      sender.sendMessage("§cDu hast kein Kit angegeben.");
      return;
    }
    if (arena.getState() != State.Setup) {
      sender.sendMessage("§cEs muss bereits mindestens ein Team geben.");
      return;
    }
    String kitName = args.getArgs(0);
    if (!this.plugin.getRepo().getKit().existsKit(kitName)) {
      sender.sendMessage("§cDas Kit " + kitName + " gibt es nicht.");
      return;
    }
    arena.setKit(kitName);
  }

  @Command(name = "wgk.quit", description = "Beendet einen Fight.", usage = "/wgk quit <team1|team2>", permission = "wargear.quit")
  public void quit(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }

    if (arena.getState() != State.PreRunning && arena.getState() != State.Running) {
      args.getSender().sendMessage("§cIn dieser Arena läuft kein Fight.");
      return;
    }

    FightQuitEvent event = null;
    if (args.length() == 0) {
      event = new DrawQuitEvent(arena, "Unentschieden", arena.getGroupManager().getGroup1(),
          arena.getGroupManager().getGroup2(), FightQuitReason.FightLeader);
    } else if (args.getArgs(0).equalsIgnoreCase("team1")) {
      event = new WinQuitEvent(arena, "", arena.getGroupManager().getGroup1(),
          arena.getGroupManager().getGroup2(), FightQuitReason.FightLeader);
    } else if (args.getArgs(0).equalsIgnoreCase("team2")) {
      event = new WinQuitEvent(arena, "", arena.getGroupManager().getGroup2(),
          arena.getGroupManager().getGroup1(), FightQuitReason.FightLeader);
    }

    if (event != null) {
      this.plugin.getServer().getPluginManager().callEvent(event);
    }
  }

  @Command(name = "wgk.start", description = "Startet einen Fight.", usage = "/wgk start", permission = "wargear.start")
  public void start(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }

    if (arena.getState() != State.Setup) {
      args.getSender().sendMessage("§cHier kann kein Fight gestartet werden.");
      return;
    }

    if (arena.getGroupManager().getGroup1().getMembers().size() == 0
        || arena.getGroupManager().getGroup2().getMembers().size() == 0) {
      args.getSender().sendMessage("§cBeide Teams müssen einen Spieler haben.");
      return;
    }

    arena.startFight(args.getSender());
  }
}
