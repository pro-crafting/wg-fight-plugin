package me.Postremus.Arena.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeadCreator 
{
	public ItemStack createHead(String fromPlayer)
	{
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW+fromPlayer);
		item.setItemMeta(meta);
		return item;
	}
	
	public List<ItemStack> createHead(List<String> fromPlayers)
	{
		List<ItemStack> ret = new ArrayList<ItemStack>();
		for (String playerName : fromPlayers)
		{
			ret.add(createHead(playerName));
		}
		return ret;
	}
}
