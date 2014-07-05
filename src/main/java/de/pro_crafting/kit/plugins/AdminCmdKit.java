package de.pro_crafting.kit.plugins;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.Balor.Kit.KitInstance;
import be.Balor.bukkit.AdminCmd.ACHelper;
import de.pro_crafting.kit.KitPlugin;

public class AdminCmdKit implements KitPlugin
{
	public boolean existsKit(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit != null;
	}

	public void giveKit(String kitName, Player p) {
	    p.getInventory().addItem(getKitItems(kitName));
	}

	public ItemStack[] getKitItems(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit.getItemStacks().toArray(new ItemStack[] {});
	}
}
