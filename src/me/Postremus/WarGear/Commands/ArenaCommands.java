package me.Postremus.WarGear.Commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import me.Postremus.CommandFramework.Command;
import me.Postremus.CommandFramework.CommandArgs;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.WarGearUtil;
import me.Postremus.WarGear.Arena.Arena;

public class ArenaCommands {
	private WarGear plugin;
	
	public ArenaCommands(WarGear plugin)
	{
		this.plugin = plugin;
	}
	
	@Command(name = "wgk.arena", aliases = { "wgk.arena.help" }, description = "Zeigt die Hilfe an.", usage = "/wgk arena", permission="wargear.arena")
	public void arena(CommandArgs args)
	{
		args.getSender().sendMessage("§c§LKein passender Befehl gefunden!");
		args.getSender().sendMessage("§B/wgk arena open");
		args.getSender().sendMessage("§B/wgk arena close");
		args.getSender().sendMessage("§B/wgk arena list");
		args.getSender().sendMessage("§B/wgk arena info");
		args.getSender().sendMessage("§B/wgk arena reset");
	}
	
	@Command(name = "wgk.arena.close", description = "Schließt die Arena", 
			usage = "/wgk arena close", permission="wargear.arena.close")
	public void close(CommandArgs args)
	{
		Arena arena = WarGearUtil.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		arena.close();
	}
	
	@Command(name = "wgk.arena.open", description = "Öffnet die Arena", 
			usage = "/wgk arena open", permission="wargear.arena.open")
	public void open(CommandArgs args)
	{
		Arena arena = WarGearUtil.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		arena.open();
	}
	
	@Command(name = "wgk.arena.info", description = "Zeigt Einstellungen der Arena an", 
			usage = "/wgk arena info", permission="wargear.arena.info")
	public void info(CommandArgs args)
	{
		Arena arena = WarGearUtil.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		args.getSender().sendMessage(ChatColor.GREEN + "---Arena Info---");
		args.getSender().sendMessage(ChatColor.GRAY+"Arena Name: " + ChatColor.AQUA + arena.getArenaName());
		args.getSender().sendMessage(ChatColor.GRAY+"Welt: " + ChatColor.AQUA + arena.getRepo().getWorld().getName());
		args.getSender().sendMessage(ChatColor.GRAY+"Fight Modus: " + ChatColor.AQUA + arena.getRepo().getFightMode());
		args.getSender().sendMessage(ChatColor.GRAY+"Bodenhöhe: " + ChatColor.AQUA + arena.getRepo().getGroundHeight());
		args.getSender().sendMessage(ChatColor.GRAY+"BodenSchematic: " + ChatColor.AQUA + arena.getRepo().getGroundSchematic());
		args.getSender().sendMessage(ChatColor.GRAY+"Auto Reset: " + ChatColor.AQUA + arena.getRepo().getAutoReset());
		args.getSender().sendMessage(ChatColor.GRAY+"Region Team1: " + ChatColor.AQUA + arena.getRepo().getTeam1Region().getId());
		args.getSender().sendMessage(ChatColor.GRAY+"Region Team2: " + ChatColor.AQUA + arena.getRepo().getTeam2Region().getId());
		args.getSender().sendMessage(ChatColor.GRAY+"Warp Team1: " + ChatColor.AQUA + getStringFromLocation(arena.getRepo().getTeam1Warp()));
		args.getSender().sendMessage(ChatColor.GRAY+"Warp Team2: " + ChatColor.AQUA + getStringFromLocation(arena.getRepo().getTeam2Warp()));
		args.getSender().sendMessage(ChatColor.GRAY+"Warp Fight Ende: " + ChatColor.AQUA + getStringFromLocation(arena.getRepo().getFightEndWarp()));
	}
	
	private String getStringFromLocation(Location loc)
	{
		String ret = "x: %.2f; y: %.2f; z: %.2f";
		return String.format(ret, new Object[]{loc.getX(), loc.getY(), loc.getZ()});
	}
	
	@Command(name = "wgk.arena.reset", description = "Resetet die Arena", 
			usage = "/wgk arena reset", permission="wargear.arena.reset")
	public void reset(CommandArgs args)
	{
		Arena arena = WarGearUtil.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		arena.getReseter().reset();
		args.getSender().sendMessage("§7Arena §B"+arena.getArenaName()+" §7wird resetet.");
	}
	
	@Command(name = "wgk.arena.list", description = "Listet die Arenen", 
			usage = "/wgk arena list", permission="wargear.arena.list")
	public void list(CommandArgs args)
	{
		args.getSender().sendMessage(ChatColor.GREEN + "---Verfügbare Arenen---");
		List<String> arenas = this.plugin.getRepo().getArenaNames();
		for (String arenaName : arenas)
		{
			args.getSender().sendMessage(arenaName);
		}
	}
}
