package de.pro_crafting.wg.commands.provider;

import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.commands.WarGearException;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;

public class KitProvider implements Provider<String> {
    private WarGear plugin;

    public KitProvider(WarGear plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public String get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        if (!arguments.hasNext()) {
            throw new WarGearException("§cDu hast kein Kit angegeben.");
        }
        String kitName = arguments.next();
        if (!this.plugin.getRepo().getKit().existsKit(kitName)) {
            throw new WarGearException("§cDas Kit " + kitName + " gibt es nicht.");
        }
        return kitName;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
