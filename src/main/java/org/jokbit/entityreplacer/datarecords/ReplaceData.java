package org.jokbit.entityreplacer.datarecords;

import java.util.Set;

public record ReplaceData(
        Set<String> replaceds,
        Set<Replacer> replacers,
        double chance,
        int rolls,
        boolean join,
        boolean additionMode
) {
    public ReplaceData {
        if (replaceds == null) {
            replaceds = Set.of();
        }
        if (replacers == null) {
            replacers = Set.of();
        }
        if (chance == 0.0d) {
            chance = 1.0;
        }
        if (rolls < 0) {
            rolls = 1;
        }
    }
}
