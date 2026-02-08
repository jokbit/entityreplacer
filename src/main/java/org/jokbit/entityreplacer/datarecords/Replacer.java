package org.jokbit.entityreplacer.datarecords;

import com.google.gson.annotations.JsonAdapter;
import net.minecraft.nbt.CompoundTag;
import org.jokbit.entityreplacer.json.CompoundTagTypeAdapter;

public record Replacer(
        String mobId,
        @JsonAdapter(CompoundTagTypeAdapter.class)
        CompoundTag nbt,
        int weight
) {
}
