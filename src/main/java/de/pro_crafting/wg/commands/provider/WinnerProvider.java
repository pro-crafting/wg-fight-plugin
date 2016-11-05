package de.pro_crafting.wg.commands.provider;

import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;

public class WinnerProvider implements Provider<String> {
    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public String get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return arguments.next();
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
