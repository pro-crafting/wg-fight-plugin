package me.Postremus.KitApi;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public interface IKitPlugin 
{
	void setServer(Server server);
	boolean existsKit(String kitName);
	void giveKit(String kitName, Player p);
}
