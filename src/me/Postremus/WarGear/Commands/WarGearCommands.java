package me.Postremus.WarGear.Commands;

import org.bukkit.entity.Player;

import me.Postremus.CommandFramework.Command;
import me.Postremus.CommandFramework.CommandArgs;
import me.Postremus.WarGear.ArenaState;
import me.Postremus.WarGear.DrawReason;
import me.Postremus.WarGear.TeamWinReason;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.WarGearUtil;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Events.DrawQuitEvent;
import me.Postremus.WarGear.Events.TeamWinQuitEvent;

public class WarGearCommands {
	private WarGear plugin;
	
	public WarGearCommands(WarGear plugin)
	{
		this.plugin = plugin;
	}
	
	@Command(name = "wgk", aliases = { "wgk.help" }, description = "Zeigt die Hilfe an.", usage = "/wgk", permission="wargear")
	public void WarGear(CommandArgs args)
	{
		args.getSender().sendMessage("§c§LKein passender Befehl gefunden!");
		args.getSender().sendMessage("§B/wgk team ...");
		args.getSender().sendMessage("§B/wgk arena ...");
		args.getSender().sendMessage("§B/wgk kit <kitName>");
		args.getSender().sendMessage("§B/wgk warp <arenaname> [playername]");
		args.getSender().sendMessage("§B/wgk reload");
	}
	
	@Command(name = "wgk.reload", description = "Reloadet die Config.", usage="/wgk reload", permission="wargear.reload")
	public void reload(CommandArgs args)
	{
		this.plugin.reloadConfig();
		this.plugin.getServer().getPluginManager().disablePlugin(plugin);
		this.plugin.getServer().getPluginManager().enablePlugin(plugin);
		args.getSender().sendMessage("Plugin wurde gereloadet.");
	}
	
	@Command(name = "wgk.warp", description = "Teleport zu der Arena.", usage="/wgk warp <arenaname> [player]", permission="wargear.warp")
	public void warp(CommandArgs args)
	{
		if (args.getArgs().length < 1)
		{
			args.getSender().sendMessage("§cEs muss eine Arena angegeben werden.");
			return;
		}
		String arenaName = args.getArgs()[0];
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena == null)
		{
			args.getSender().sendMessage("§cDie Arena "+ arenaName+" existiert nicht.");
			return;
		}
		
		Player toWarp = args.getPlayer();
		if (args.getArgs().length >= 2)
		{
			if (!args.getSender().hasPermission("wargear.warp.other"))
			{
				args.getSender().sendMessage("§cDu nichts Rechte dafür.");
				return;
			}
			if (this.plugin.getServer().getPlayer(args.getArgs()[1]) == null)
			{
				args.getSender().sendMessage("§c"+args.getArgs()[1]+" Ist nicht online.");
				return;
			}
			toWarp = this.plugin.getServer().getPlayer(args.getArgs()[1]);
		}
		
		if (toWarp == null)
		{
			args.getSender().sendMessage("§cEs muss ein Spieler angegeben werden.");
			return;
		}
		
		arena.teleport(toWarp);
	}
	
	@Command(name = "wgk.kit", description="Legt das Kit für den Fight fest.", usage="/wgk kit name", permission="wargear.kit")
	public void kit(CommandArgs args)
	{
		Arena arena = WarGearUtil.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.getArgs().length == 0)
		{
			args.getSender().sendMessage("§cDu hast kein Kit angegeben.");
			return;
		}
		if (arena.getFightState() != ArenaState.Setup)
		{
			args.getSender().sendMessage("§cEs muss bereits min. ein Team geben.");
			return;
		}
		String kitName = args.getArgs()[0];
		if (!this.plugin.getKitApi().existsKit(kitName))
		{
			args.getSender().sendMessage("§cDas Kit " + kitName + " gibt es nicht.");
			return;
		}
		arena.setKit(kitName);
	}
	
	@Command(name="wgk.quit", description="Beendet einen Fight.", usage="/wgk quit <team1|team2>",permission="wargear.quit")
	public void quit(CommandArgs args)
	{
		Arena arena = WarGearUtil.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		
		if (arena.getFightState() != ArenaState.PreRunning && arena.getFightState() != ArenaState.Running)
		{
			args.getSender().sendMessage("§cIn dieser Arena läuft kein Fight.");
			return;
		}
		
		if (args.getArgs().length == 0)
		{
			DrawQuitEvent event = new DrawQuitEvent(arena, "Unentschieden", arena.getTeam().getTeam1(), arena.getTeam().getTeam2(), DrawReason.FightLeader);
			this.plugin.getServer().getPluginManager().callEvent(event);
		}
		else if (args.getArgs()[0].equalsIgnoreCase("team1"))
		{
			TeamWinQuitEvent event = new TeamWinQuitEvent(arena, "Team1 hat gewonnen", arena.getTeam().getTeam1(), arena.getTeam().getTeam2(), TeamWinReason.FightLeader);
			this.plugin.getServer().getPluginManager().callEvent(event);
		}
		else if (args.getArgs()[0].equalsIgnoreCase("team2"))
		{
			TeamWinQuitEvent event = new TeamWinQuitEvent(arena, "Team2 hat gewonnen", arena.getTeam().getTeam2(), arena.getTeam().getTeam1(), TeamWinReason.FightLeader);
			this.plugin.getServer().getPluginManager().callEvent(event);
		}
	}
}
