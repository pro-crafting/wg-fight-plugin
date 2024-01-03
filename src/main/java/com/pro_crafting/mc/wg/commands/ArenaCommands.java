package com.pro_crafting.mc.wg.commands;

import com.pro_crafting.mc.commandframework.Command;
import com.pro_crafting.mc.commandframework.CommandArgs;
import com.pro_crafting.mc.wg.Util;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class ArenaCommands {

  private WarGear plugin;

  public ArenaCommands(WarGear plugin) {
    this.plugin = plugin;
  }

  @Command(name = "wgk.arena", aliases = {
      "wgk.arena.help"}, description = "Zeigt die Hilfe an.", usage = "/wgk arena", permission = "wargear.arena")
  public void arena(CommandArgs args) {
    CommandSender sender = args.getSender();
    sender.sendMessage("§c§LKein passender Befehl gefunden!");
    sender.sendMessage("§B/wgk arena open");
    sender.sendMessage("§B/wgk arena close");
    sender.sendMessage("§B/wgk arena list");
    sender.sendMessage("§B/wgk arena info");
    sender.sendMessage("§B/wgk arena reset");
    sender.sendMessage("§B/wgk arena replace");
    sender.sendMessage("§B/wgk arena reload");
  }

  @Command(name = "wgk.arena.close", description = "Schließt die Arena",
      usage = "/wgk arena close", permission = "wargear.arena.close")
  public void close(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }
    arena.close();
  }

  @Command(name = "wgk.arena.open", description = "öffnet die Arena",
      usage = "/wgk arena open", permission = "wargear.arena.open")
  public void open(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }
    arena.open();
  }

  @Command(name = "wgk.arena.info", description = "Zeigt Einstellungen der Arena an",
      usage = "/wgk arena info", permission = "wargear.arena.info")
  public void info(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }

    CommandSender sender = args.getSender();
    sender.sendMessage("§a---Arena Info---");
    sender.sendMessage("§7Arena Name: §B" + arena.getName());
    sender.sendMessage("§7Status: §B" + arena.getState().toString());
    sender.sendMessage("§7Welt: §B" + arena.getRepo().getWorld().getName());
    sender.sendMessage("§7Fight Modus: §B" + arena.getRepo().getFightMode());
    sender.sendMessage("§7Bodenhöhe: §B" + arena.getRepo().getGroundHeight());
    sender.sendMessage("§7BodenSchematic: §B" + arena.getRepo().getGroundSchematic());
    sender.sendMessage("§7Auto Reset: §B" + arena.getRepo().isAutoReset());
    sender.sendMessage("§7Region Team1: §B" + arena.getRepo().getTeam1Region().toString());
    sender.sendMessage("§7Region Team2: §B" + arena.getRepo().getTeam2Region().toString());
    sender.sendMessage("§7Region Inner: §B" + arena.getRepo().getInnerRegion().toString());
    sender.sendMessage("§7Warp Team1: §B" + getStringFromLocation(arena.getRepo().getTeam1Warp()));
    sender.sendMessage("§7Warp Team2: §B" + getStringFromLocation(arena.getRepo().getTeam2Warp()));
    sender.sendMessage(
        "§7Warp Fight Ende: §B" + getStringFromLocation(arena.getRepo().getSpawnWarp()));
  }

  private String getStringFromLocation(Location loc) {
    String ret = "x: %.2f; y: %.2f; z: %.2f";
    return String.format(ret, new Object[]{loc.getX(), loc.getY(), loc.getZ()});
  }

  @Command(name = "wgk.arena.reset", description = "Resetet die Arena",
      usage = "/wgk arena reset", permission = "wargear.arena.reset")
  public void reset(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }
    arena.getReseter().reset();
    args.getSender().sendMessage("§7Arena §B" + arena.getName() + " §7wird resetet.");
  }

  @Command(name = "wgk.arena.list", description = "Listet die Arenen",
      usage = "/wgk arena list", permission = "wargear.arena.list")
  public void list(CommandArgs args) {
    args.getSender().sendMessage("§a---Verfügbare Arenen---");
    Set<String> arenas = this.plugin.getArenaManager().getArenaNames();
    for (String arenaName : arenas) {
      args.getSender().sendMessage("§7" + arenaName);
    }
  }

  @Command(name = "wgk.arena.reload", description = "Reloaded die Arena",
      usage = "/wgk arena reload", permission = "wargear.arena.reload")
  public void reload(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }
    arena.unload();
    arena.load();
    args.getSender().sendMessage("§7Arena §B" + arena.getName() + " §7wurde gereloadet.");
  }

  @Command(name = "wgk.arena.replace", description = "Replaced die MGs in der Arena",
      usage = "/wgk arena replace", permission = "wargear.arena.replace")
  public void replace(CommandArgs args) {
    Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
    if (arena == null) {
      args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
      return;
    }
    arena.replaceMG();
    args.getSender().sendMessage("§7Obsidian in §B" + arena.getName() + " §7wurde replaced.");
  }
}
