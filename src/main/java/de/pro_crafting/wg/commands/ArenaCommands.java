package de.pro_crafting.wg.commands;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Switch;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class ArenaCommands {
    private WarGear plugin;

    public ArenaCommands(WarGear plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"", "help"}, desc = "Zeigt die Hilfe an.")
    @Require("wargear.arena")
    public void arena(CommandSender sender) {
        sender.sendMessage("§c§LKein passender Befehl gefunden!");
        sender.sendMessage("§B/wgk arena open");
        sender.sendMessage("§B/wgk arena close");
        sender.sendMessage("§B/wgk arena list");
        sender.sendMessage("§B/wgk arena info");
        sender.sendMessage("§B/wgk arena reset");
        sender.sendMessage("§B/wgk arena replace");
        sender.sendMessage("§B/wgk arena reload");
    }

    @Command(aliases = {"close"}, desc = "Schließt die Arena")
    @Require("wargear.arena.close")
    public void close(CommandSender sender, Arena arena) {
        arena.close();
    }

    @Command(aliases = {"open"}, desc = "Öffnet die Arena")
    @Require("wargear.arena.open")
    public void open(CommandSender sender, @Switch('a') Arena arena) {
        arena.open();
    }

    @Command(aliases = {"info"}, desc = "Zeigt Einstellungen der Arena an")
    @Require("wargear.arena.info")
    public void info(CommandSender sender, @Switch('a') Arena arena) {
        sender.sendMessage("§a---Arena Info---");
        sender.sendMessage("§7Arena Name: §B" + arena.getName());
        sender.sendMessage("§7Status: §B" + arena.getState().toString());
        sender.sendMessage("§7Welt: §B" + arena.getRepo().getWorld().getName());
        sender.sendMessage("§7Fight Modus: §B" + arena.getRepo().getFightMode());
        sender.sendMessage("§7Bodenhöhe: §B" + arena.getRepo().getGroundHeight());
        sender.sendMessage("§7BodenSchematic: §B" + arena.getRepo().getGroundSchematic());
        sender.sendMessage("§7Auto Reset: §B" + arena.getRepo().getAutoReset());
        sender.sendMessage("§7Region Team1: §B" + arena.getRepo().getTeam1Region().getId());
        sender.sendMessage("§7Region Team2: §B" + arena.getRepo().getTeam2Region().getId());
        sender.sendMessage("§7Region Inner: §B" + arena.getRepo().getInnerRegion().getId());
        sender.sendMessage("§7Warp Team1: §B" + getStringFromLocation(arena.getRepo().getTeam1Warp()));
        sender.sendMessage("§7Warp Team2: §B" + getStringFromLocation(arena.getRepo().getTeam2Warp()));
        sender.sendMessage("§7Warp Fight Ende: §B" + getStringFromLocation(arena.getRepo().getSpawnWarp()));
    }

    private String getStringFromLocation(Location loc) {
        String ret = "x: %.2f; y: %.2f; z: %.2f";
        return String.format(ret, new Object[]{loc.getX(), loc.getY(), loc.getZ()});
    }

    @Command(aliases = {"reset"}, desc = "Resetet die Arena")
    @Require("wargear.arena.reset")
    public void reset(CommandSender sender, @Switch('a') Arena arena) {
        arena.getReseter().reset();
        sender.sendMessage("§7Arena §B" + arena.getName() + " §7wird resetet.");
    }

    @Command(aliases = {"list"}, desc = "Listet die Arena auf")
    @Require("wargear.arena.list")
    public void list(CommandSender sender, @Switch('a') Arena arena) {
        sender.sendMessage("§a---Verfügbare Arenen---");
        Set<String> arenas = this.plugin.getArenaManager().getArenaNames();
        for (String arenaName : arenas) {
            sender.sendMessage("§7" + arenaName);
        }
    }

    @Command(aliases = {"reload"}, desc = "Lädt die die Arena neu")
    @Require("wargear.arena.reload")
    public void reload(CommandSender sender, @Switch('a') Arena arena) {
        arena.unload();
        arena.load();
        sender.sendMessage("§7Arena §B" + arena.getName() + " §7wurde gereloadet.");
    }

    @Command(aliases = {"replace"}, desc = "Replaced die MGs in der Arena")
    @Require("wargear.arena.replace")
    public void replace(CommandSender sender, @Switch('a') Arena arena) {
        arena.replaceMG();
        sender.sendMessage("§7Obsidian in §B" + arena.getName() + " §7wurde replaced.");
    }
}
