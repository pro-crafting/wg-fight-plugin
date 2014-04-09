package de.hrc_gaming.kit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface KitPlugin 
{
	boolean existsKit(String kitName);
	void giveKit(String kitName, Player p);
	ItemStack[] getKitItems(String kitName);
}
