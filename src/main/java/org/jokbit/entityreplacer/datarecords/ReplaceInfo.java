package org.jokbit.entityreplacer.datarecords;

import java.util.List;

public record ReplaceInfo(
        List<Replacer> replacers,
        double chance,
        int rolls,
        boolean additionMode,
        int totalWeight,
        int[] weightList
) {
    public ReplaceInfo(List<Replacer> replacers, double chance, int rolls, boolean additionMode, int totalWeight) {
        this(replacers, chance, rolls, additionMode, totalWeight, new int[replacers.size()]);
        int index = 0;
        for (Replacer replacer : replacers) {
            weightList[index] = Math.max(0, replacer.weight()) + Math.max(0, index > 0 ? weightList[index - 1] : 0);
            index++;
        }
    }
}
