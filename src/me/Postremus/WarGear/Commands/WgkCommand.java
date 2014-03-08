package me.Postremus.WarGear.Commands;

import java.util.ArrayList;
import java.util.List;

import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class WgkCommand implements CommandExecutor{

	private WgkCommandLogik logik;
	private WarGear plugin;
	
	public WgkCommand(WarGear plugin)
	{
		this.plugin = plugin;
		this.logik = new WgkCommandLogik(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args.length == 0)
		{
			help(sender);
			return true;
		}
		if (args.length == 1)
		{
			if (args[0].equalsIgnoreCase("count") && this.hasPermissionWrapper(sender, "wargear.count"))
			{
				this.logik.StartManuelCountdown();
			}
			else if (args[0].equalsIgnoreCase("reload") && this.hasPermissionWrapper(sender, "wargear.reload"))
			{
				this.plugin.reloadConfig();
				this.plugin.getServer().getPluginManager().disablePlugin(plugin);
				this.plugin.getServer().getPluginManager().enablePlugin(plugin);
				sender.sendMessage("Plugin wurde gereloadet.");
			}
			else
			{
				help(sender);
			}
			return true;
		}
		String arenaName = this.getArenaOfCommand(sender, args);
		args = this.removeFlagsFromArgs(args);
		if (arenaName.equals("") || !this.plugin.getArenaManager().isArenaLoaded(arenaName))
		{
			sender.sendMessage("§cDie Arena "+ arenaName+" existiert nicht.");
			return true;
		}
		if (args[0].equalsIgnoreCase("team"))
		{
			if (args[1].equalsIgnoreCase("leader")  && this.hasPermissionWrapper(sender, "wargear.team.leader"))
			{
				this.logik.addTeamLeader(sender, arenaName, args[2]);
			}
			else if (args[1].equalsIgnoreCase("leave")  && this.hasPermissionWrapper(sender, "wargear.team.leave"))
			{
				this.logik.leaveTeam(sender, arenaName);
			}
			else if (args[1].equalsIgnoreCase("add")  && this.hasPermissionWrapper(sender, "wargear.team.add"))
			{
				this.logik.addTeamMember(sender, arenaName, args[2]);
			}
			else if (args[1].equalsIgnoreCase("remove")  && this.hasPermissionWrapper(sender, "wargear.team.remove"))
			{
				this.logik.removeTeamMember(sender, arenaName, args[2]);
			}
			else if (args[1].equalsIgnoreCase("ready")  && this.hasPermissionWrapper(sender, "wargear.team.ready"))
			{
				this.logik.readifyTeam(sender, arenaName);
			}
			else
			{
				help(sender);
			}
		}
		else if (args[0].equalsIgnoreCase("kit") && this.hasPermissionWrapper(sender, "wargear.fight.kit"))
		{
			this.logik.setKit(sender, args[1], arenaName);
		}
		else if (args[0].equalsIgnoreCase("warp"))
		{
			if (args.length < 2)
			{
				help(sender);
				return true;
			}
			arenaName = args[1];
			if (!this.plugin.getArenaManager().isArenaLoaded(arenaName))
			{
				sender.sendMessage("§cDie Arena "+ arenaName+" existiert nicht.");
				return true;
			}
			Arena arena = this.plugin.getArenaManager().getArena(arenaName);
			if (args.length == 2  && this.hasPermissionWrapper(sender, "wargear.warp"))
			{
				if (!(sender instanceof Player))
				{
					sender.sendMessage("§cDu musst ein Spieler sein.");
					return true;
				}
				arena.teleport((Player)sender);
			}
			if (args.length == 3  && this.hasPermissionWrapper(sender, "wargear.warp.other"))
			{
				Player p = this.plugin.getServer().getPlayer(args[2]);
				if (p == null)
				{
					sender.sendMessage("§cIst nicht online.");
					return true;
				}
				arena.teleport(p);
			}
		}
		else if (args[0].equalsIgnoreCase("arena"))
		{
			if (args[1].equalsIgnoreCase("open") && this.hasPermissionWrapper(sender, "wargear.arena.open"))
			{
				this.plugin.getArenaManager().getArena(arenaName).open();
			}
			else if (args[1].equalsIgnoreCase("close") && this.hasPermissionWrapper(sender, "wargear.arena.close"))
			{
				this.plugin.getArenaManager().getArena(arenaName).close();
			}
			else if (args[1].equalsIgnoreCase("list") && this.hasPermissionWrapper(sender, "wargear.arena.list"))
			{
				this.logik.showArenaNames(sender);
			}
			else if (args[1].equalsIgnoreCase("info")  && this.hasPermissionWrapper(sender, "wargear.arena.info"))
			{
				this.logik.showArenaInfo(sender, arenaName);
			}
			else if (args[1].equalsIgnoreCase("reset") && this.hasPermissionWrapper(sender, "wargear.arena.reset"))
			{
				this.logik.resetArena(sender, arenaName);
			}
			else
			{
				help(sender);
			}
		}
		else
		{
			help(sender);
		}
		return true;
	}
	
	private void help(CommandSender sender)
	{
		sender.sendMessage("§c§LKein passender Befehl gefunden!");
		sender.sendMessage("§B/wgk team leader <playername>");
		sender.sendMessage("§B/wgk team add <playername>");
		sender.sendMessage("§B/wgk team remove <playername>");
		sender.sendMessage("§B/wgk team leave");
		sender.sendMessage("§B/wgk kit <kitName>");
		sender.sendMessage("§B/wgk arena open");
		sender.sendMessage("§B/wgk arena close");
		sender.sendMessage("§B/wgk arena list");
		sender.sendMessage("§B/wgk arena info");
		sender.sendMessage("§B/wgk arena reset");
		sender.sendMessage("§B/wgk warp <arenaname> [playernname]");
		sender.sendMessage("§B/wgk count");
		sender.sendMessage("§B/wgk reload");
	}
	
	private String getArenaOfCommand(CommandSender sender, String[] args)
	{
		boolean hasFoundArenaFlag = false;
		String ret = "";
		for (String argument : args)
		{
			if (hasFoundArenaFlag)
			{
				ret = argument;
			}
			hasFoundArenaFlag = argument.equalsIgnoreCase("-a");
		}
		if (ret != "")
		{
			return ret;
		}
		if (!(sender instanceof ConsoleCommandSender))
		{
			Arena arena = null;
			if (sender instanceof Player)
			{
				arena = this.plugin.getArenaManager().getArenaAtLocation(((Player)sender).getLocation());
			}
			else if (sender instanceof BlockCommandSender)
			{
				arena = this.plugin.getArenaManager().getArenaAtLocation(((BlockCommandSender)sender).getBlock().getLocation());
			}
			if (arena != null)
			{
				return arena.getArenaName();
			}
		}
		return ret;
	}
	
	private String[] removeFlagsFromArgs(String[] args)
	{
		List<String> ret = new ArrayList<String>();
		boolean removeNextArg = false;
		for (String argument : args)
		{
			if (argument.equals("-a"))
			{
				removeNextArg = true;
				continue;
			}
			if (removeNextArg)
			{
				removeNextArg = false;
				continue;
			}
			ret.add(argument);
		}
		String[] retType = new String[0];
		return ret.toArray(retType);
	}
	
	private boolean hasPermissionWrapper(CommandSender sender, String permission)
	{
		if (sender.hasPermission(permission))
		{
			return true;
		}
		else
		{
			sender.sendMessage("§c§LDir fehlt die " + permission + " Berechtigung.");
			return false;
		}
	}
}
