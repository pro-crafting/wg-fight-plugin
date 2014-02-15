package me.Postremus.WarGear;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import me.Postremus.WarGear.Arena.Arena;

public class WarGearUtil {
	public static Arena getArenaFromSender(WarGear plugin, CommandSender sender, String[] args)
	{
		boolean hasFoundArenaFlag = false;
		Arena ret = null;
		for (String argument : args)
		{
			if (hasFoundArenaFlag)
			{
				ret = plugin.getArenaManager().getArena(argument);
			}
			hasFoundArenaFlag = argument.equalsIgnoreCase("-a");
		}
		if (ret != null)
		{
			return ret;
		}
		if (!(sender instanceof ConsoleCommandSender))
		{
			Arena arena = null;
			if (sender instanceof Player)
			{
				arena = plugin.getArenaManager().getArenaAtLocation(((Player)sender).getLocation());
			}
			else if (sender instanceof BlockCommandSender)
			{
				arena = plugin.getArenaManager().getArenaAtLocation(((BlockCommandSender)sender).getBlock().getLocation());
			}
			if (arena != null)
			{
				return arena;
			}
		}
		return ret;
	}
	
	public static String[] removeFlagsFromArgs(String[] args)
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
	
	public static void makeHealthy(Player player)
	{
		feed(player);
		heal(player);
		player.setFireTicks(0);
	}
	
	public static void feed(Player player)
	{
		player.setFoodLevel(20);
	}
	
	public static void heal(Player player)
	{
		player.setHealth(player.getMaxHealth());
	}
	
	public static void disableFly(Player player)
	{
		player.setAllowFlight(false);
		player.setFlying(false);
	}
	
	public static void enableFly(Player player)
	{
		player.setAllowFlight(true);
		player.setFlying(true);
	}
}
