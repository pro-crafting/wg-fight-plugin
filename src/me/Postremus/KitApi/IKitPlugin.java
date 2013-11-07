package me.Postremus.KitApi;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IKitPlugin 
{
	void setServer(Server server);
	boolean existsKit(String kitName);
	void giveKit(String kitName, Player p);
	ItemStack[] getKitItems(String kitName);
}
