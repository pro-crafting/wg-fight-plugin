package me.Postremus.WarGear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		}
		if (args.length > 0)
		{
			String arenaName = this.getArenaOfCommand(sender, args);
			if (arenaName.equals("") || !this.plugin.getRepo().existsArena(arenaName))
			{
				sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
				return true;
			}
			args = this.removeFlagsFromArgs(args);
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
				if (args[0].equalsIgnoreCase("setup") && this.hasPermissionWrapper(sender, "wargear.fight.setup"))
				{
					this.logik.setup(sender, arenaName);
				}
				else if (args[0].equalsIgnoreCase("start") && this.hasPermissionWrapper(sender, "wargear.fight.start"))
				{
					this.logik.start(sender, arenaName);
				}
				else
				{
					help(sender);
				}
			}
			if (args.length > 1)
			{
				if (args[0].equalsIgnoreCase("team1") && this.hasPermissionWrapper(sender, "wargear.fight.team1"))
				{
					List<String> teamMember = Arrays.asList(args);
					teamMember = teamMember.subList(1, teamMember.size());
					this.logik.setTeam(sender, "team1", teamMember, arenaName);	
				}
				else if (args[0].equalsIgnoreCase("team2") && this.hasPermissionWrapper(sender, "wargear.fight.team2"))
				{
					List<String> teamMember = Arrays.asList(args);
					teamMember = teamMember.subList(1, teamMember.size());
					this.logik.setTeam(sender, "team2", teamMember, arenaName);	
				}
				else if (args[0].equalsIgnoreCase("kit") && this.hasPermissionWrapper(sender, "wargear.fight.kit"))
				{
					this.logik.setKit(sender, args[1], arenaName);
				}
				else if (args[0].equalsIgnoreCase("quit") && this.hasPermissionWrapper(sender, "wargear.fight.quit"))
				{
					if  ((!args[1].equalsIgnoreCase("team1")) && (!args[1].equalsIgnoreCase("team2")))
					{
						help(sender);
						return true;
					}
					else
					{
						this.logik.quit(sender, args[1], arenaName);
					}
				}
				else if (args[0].equalsIgnoreCase("arena"))
				{
					if (args[1].equalsIgnoreCase("open") && this.hasPermissionWrapper(sender, "wargear.arena.open"))
					{
						this.logik.getArenaManager().getArena(arenaName).open();
					}
					else if (args[1].equalsIgnoreCase("close") && this.hasPermissionWrapper(sender, "wargear.arena.close"))
					{
						this.logik.getArenaManager().getArena(arenaName).close();
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
			}
		}
		return true;
	}
	
	private void help(CommandSender sender)
	{
		sender.sendMessage("Kein passender Befehl gefunden!");
		sender.sendMessage("/wgk setup");
		sender.sendMessage("/wgk start");
		sender.sendMessage("/wgk quit team1");
		sender.sendMessage("/wgk quit team2");
		sender.sendMessage("/wgk team1 <User1> [User2] [Userx]");
		sender.sendMessage("/wgk team2 <User1> [User2] [Userx]");
		sender.sendMessage("/wgk kit <kitName>");
		sender.sendMessage("/wgk arena open");
		sender.sendMessage("/wgk arena close");
		sender.sendMessage("/wgk arena list");
		sender.sendMessage("/wgk arena info");
		sender.sendMessage("/wgk arena reset");
		sender.sendMessage("/wgk count");
		sender.sendMessage("/wgk reload");
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
			if (sender instanceof Player)
			{
				ret = this.plugin.getRepo().getArenaAtLocation(((Player)sender).getLocation());
			}
			else if (sender instanceof BlockCommandSender)
			{
				ret = this.plugin.getRepo().getArenaAtLocation(((BlockCommandSender)sender).getBlock().getLocation());
			}
		}
		if (ret == "")
		{
			return this.plugin.getRepo().getDefaultArenaName();
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
			sender.sendMessage("Dir fehlt die " + permission + " Berechtigung.");
			return false;
		}
	}
}
