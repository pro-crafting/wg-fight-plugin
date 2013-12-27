package me.Postremus.WarGear;

import me.Postremus.KitApi.API;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class AdmincmdWrapper {
	public static Boolean existsKit(String kitName, Server server)
	{
		API kitApi = new API(server);
		return kitApi.existsKit(kitName);
	}
	
	public static void giveKit(String kitName, Player player, Server server)
	{
		API kitApi = new API(server);
		kitApi.giveKit(kitName, player);
	}

	public static void heal(Player player)
	{
		player.setHealth(player.getMaxHealth());
		player.setFireTicks(0);
		player.setFoodLevel(20);
	}
	
	public static void disableFly(Player player)
	{
		player.setAllowFlight(false);
		player.setFlying(false);
	}
}
