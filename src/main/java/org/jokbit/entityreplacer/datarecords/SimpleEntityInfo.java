package org.jokbit.entityreplacer.datarecords;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;


public record SimpleEntityInfo (
        String mobId,
        double x,
        double y,
        double z,
        CompoundTag nbt,
        ResourceKey<net.minecraft.world.level.Level> levelKey
){

}
