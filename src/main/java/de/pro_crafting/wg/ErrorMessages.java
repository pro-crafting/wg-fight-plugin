package de.pro_crafting.wg;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class ErrorMessages {
    private Set<String> warnings;
    private Set<String> errors;

    public ErrorMessages() {
        this.warnings = new HashSet<>();
        this.errors = new HashSet<>();
    }

    public void addWarning(String message) {
        this.warnings.add(message);
    }

    public boolean hasWarnings() {
        return this.warnings.size() > 0;
    }

    public void addError(String message) {
        this.errors.add(message);
    }

    public boolean hasErrors() {
        return this.errors.size() > 0;
    }
}
