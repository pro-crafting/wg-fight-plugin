package de.pro_crafting.kit.plugins;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.Balor.Kit.KitInstance;
import be.Balor.bukkit.AdminCmd.ACHelper;
import de.pro_crafting.kit.KitProvider;

public class AdminCmdProvider implements KitProvider
{
	public boolean existsKit(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit != null;
	}

	public ItemStack[] getItems(String kitName) {
		KitInstance kit = ACHelper.getInstance().getKit(kitName);
		return kit.getItemStacks().toArray(new ItemStack[] {});
	}

	public void distribute(String kitName, Player player) {
		player.getInventory().addItem(getItems(kitName));
	}

	public String getName() {
		return "Admincmd";
	}
}
