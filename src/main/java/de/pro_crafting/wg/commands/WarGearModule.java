package de.pro_crafting.wg.commands;

import com.sk89q.intake.parametric.AbstractModule;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.commands.provider.ArenaProvider;
import de.pro_crafting.wg.commands.provider.CommandSenderProvider;
import de.pro_crafting.wg.commands.provider.Kit;
import de.pro_crafting.wg.commands.provider.KitProvider;
import de.pro_crafting.wg.commands.provider.Named;
import de.pro_crafting.wg.commands.provider.NamedArenaProvider;
import de.pro_crafting.wg.commands.provider.PlayerProvider;
import de.pro_crafting.wg.commands.provider.PlayerRoleProvider;
import de.pro_crafting.wg.commands.provider.Sender;
import de.pro_crafting.wg.commands.provider.SenderPlayerProvider;
import de.pro_crafting.wg.commands.provider.Winner;
import de.pro_crafting.wg.commands.provider.WinnerProvider;
import de.pro_crafting.wg.group.PlayerRole;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarGearModule extends AbstractModule {
    private WarGear plugin;

    public WarGearModule(WarGear plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Arena.class).toProvider(new ArenaProvider(this.plugin));
        bind(CommandSender.class).toProvider(new CommandSenderProvider());
        bind(Player.class).annotatedWith(Sender.class).toProvider(new SenderPlayerProvider());
        bind(Player.class).toProvider(new PlayerProvider());
        bind(PlayerRole.class).toProvider(new PlayerRoleProvider());
        bind(Arena.class).annotatedWith(Named.class).toProvider(new NamedArenaProvider(this.plugin));
        bind(String.class).annotatedWith(Kit.class).toProvider(new KitProvider(this.plugin));
        bind(String.class).annotatedWith(Winner.class).toProvider(new WinnerProvider());
    }
}
