package me.Postremus.WarGear;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("start") && sender.hasPermission("wargear.fight.start"))
				{
					this.logik.start(sender);
				}
				else if (args[0].equalsIgnoreCase("count") && sender.hasPermission("wargear.count"))
				{
					this.logik.StartManuelCountdown();
				}
				else if (args[0].equalsIgnoreCase("setup") && sender.hasPermission("wargear.fight.setup"))
				{
					this.logik.setup(sender);
				}
				else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("wargear.reload"))
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
			}
			if (args.length > 1)
			{
				if (args[0].equalsIgnoreCase("setup") && args.length == 2 && sender.hasPermission("wargear.fight.start"))
				{
					this.logik.setup(sender, args[1]);
				}
				else if (args[0].equalsIgnoreCase("team1") && sender.hasPermission("wargear.fight.team1"))
				{
					List<String> teamMember = Arrays.asList(args);
					teamMember = teamMember.subList(1, teamMember.size());
					this.logik.setTeam(sender, "team1", teamMember);	
				}
				else if (args[0].equalsIgnoreCase("team2") && sender.hasPermission("wargear.fight.team2"))
				{
					List<String> teamMember = Arrays.asList(args);
					teamMember = teamMember.subList(1, teamMember.size());
					this.logik.setTeam(sender, "team2", teamMember);	
				}
				else if (args[0].equalsIgnoreCase("kit") && sender.hasPermission("wargear.fight.kit"))
				{
					this.logik.setKit(sender, args[1]);
				}
				else if (args[0].equalsIgnoreCase("quit") && sender.hasPermission("wargear.fight.quit"))
				{
					if  ((!args[1].equalsIgnoreCase("team1")) && (!args[1].equalsIgnoreCase("team2")))
					{
						help(sender);
						return true;
					}
					else
					{
						this.logik.quit(sender, args[1]);
					}
				}
				else if (args[0].equalsIgnoreCase("mode") && sender.hasPermission("wargear.fight.start"))
				{
					this.logik.setMode(sender, args[1]);
				}
				else if (args[0].equalsIgnoreCase("arena"))
				{
					String arenaName = "";
					if (args.length == 3)
					{
						arenaName = args[2];
					}
					else
					{
						arenaName = this.plugin.getRepo().getArenaOfPlayer((Player)sender);
						if (arenaName == "")
						{
							arenaName = this.plugin.getRepo().getDefaultArenaName();
						}
					}
					if (!this.plugin.getRepo().existsArena(arenaName))
					{
						sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
						return true;
					}
					if (args[1].equalsIgnoreCase("open") && sender.hasPermission("wargear.arena.open"))
					{
						this.logik.getArenaManager().getArena(arenaName).open();
					}
					else if (args[1].equalsIgnoreCase("close") && sender.hasPermission("wargear.arena.close"))
					{
						this.logik.getArenaManager().getArena(arenaName).close();
					}
					else if (args[1].equalsIgnoreCase("list") && sender.hasPermission("wargear.arena.list"))
					{
						this.logik.showArenaNames(sender);
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
		sender.sendMessage("/wgk setup [arena]");
		sender.sendMessage("/wgk start");
		sender.sendMessage("/wgk quit team1");
		sender.sendMessage("/wgk quit team2");
		sender.sendMessage("/wgk team1 <User1> [User2] [Userx]");
		sender.sendMessage("/wgk team2 <User1> [User2] [Userx]");
		sender.sendMessage("/wgk kit <kitName>");
		sender.sendMessage("/wgk arena open [arena]");
		sender.sendMessage("/wgk arena close [arena]");
		sender.sendMessage("/wgk arena list");
		sender.sendMessage("/wgk count");
		sender.sendMessage("/wgk reload");
	}
}
