package me.Postremus.WarGear;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
			Help(sender);
		}
		if (args.length > 0)
		{
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("start"))
				{
					this.logik.start(sender);
				}
				else if (args[0].equalsIgnoreCase("count"))
				{
					this.logik.StartManuelCountdown();
				}
				else if (args[0].equalsIgnoreCase("setup"))
				{
					this.logik.setup(sender);
				}
				else
				{
					Help(sender);
				}
			}
			if (args.length > 1)
			{
				if (args[0].equalsIgnoreCase("setup") && args.length == 2)
				{
					this.logik.setup(sender, args[1]);
				}
				else if (args[0].equalsIgnoreCase("team1"))
				{
					List<String> teamMember = Arrays.asList(args);
					teamMember = teamMember.subList(1, teamMember.size());
					this.logik.setTeam(sender, "team1", teamMember);	
				}
				else if (args[0].equalsIgnoreCase("team2"))
				{
					List<String> teamMember = Arrays.asList(args);
					teamMember = teamMember.subList(1, teamMember.size());
					this.logik.setTeam(sender, "team2", teamMember);	
				}
				else if (args[0].equalsIgnoreCase("kit"))
				{
					this.logik.setKit(sender, args[1]);
				}
				else if (args[0].equalsIgnoreCase("quit"))
				{
					if  ((!args[1].equalsIgnoreCase("team1")) && (!args[1].equalsIgnoreCase("team2")))
					{
						Help(sender);
						return true;
					}
					this.logik.quit(sender, args[1]);
				}
				else if (args[0].equalsIgnoreCase("mode"))
				{
					this.logik.setMode(args[1]);
				}
				else if (args[0].equalsIgnoreCase("arena"))
				{
					String arenaName = this.plugin.getRepo().getDefaultArenaName();
					if (args.length == 3)
					{
						arenaName = args[2];
						if (!this.plugin.getRepo().existsArena(arenaName))
						{
							sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
							return true;
						}
					}
					if (args[1].equalsIgnoreCase("open"))
					{
						this.logik.getArena().setArenaName(arenaName);
						this.logik.getArena().open();
					}
					else if (args[1].equalsIgnoreCase("close"))
					{
						this.logik.getArena().setArenaName(arenaName);
						this.logik.getArena().close();
					}
					else
					{
						Help(sender);
					}
				}
				else
				{
					Help(sender);
				}
			}
		}
		return true;
	}
	
	private void Help(CommandSender sender)
	{
		sender.sendMessage("Kein passender Befehl gefunden!");
		sender.sendMessage("/wgk setup");
		sender.sendMessage("/wgk start");
		sender.sendMessage("/wgk quit team1/team2");
		sender.sendMessage("/wgk team1 User1, User2, Userx");
		sender.sendMessage("/wgk team2 User1, User2, Userx");
		sender.sendMessage("/wgk kit kitName");
		sender.sendMessage("/wgk arena open/close");
		sender.sendMessage("/wgk count");
	}
}
