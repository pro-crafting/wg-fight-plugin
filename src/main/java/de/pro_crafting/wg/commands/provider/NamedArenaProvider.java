package de.pro_crafting.wg.commands.provider;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.commands.WarGearException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class NamedArenaProvider implements Provider<Arena> {
    private WarGear plugin;

    public NamedArenaProvider(WarGear plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Arena get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        String arenaName = arguments.next();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            throw new WarGearException("§cDie Arena " + arenaName + " existiert nicht.");
        }
        return arena;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        List<String> ret = new ArrayList<String>();
        for (String arenaName : this.plugin.getArenaManager().getArenaNames()) {
            if (arenaName.startsWith(prefix)) {
                ret.add(arenaName);
            }
        }
        return ret;
    }
}
