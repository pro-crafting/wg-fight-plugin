package de.pro_crafting.kit;

import org.bukkit.inventory.ItemStack;

public interface KitProvider 
{
	boolean existsKit(String kitName);
	ItemStack[] getItems(String kitName);
}
