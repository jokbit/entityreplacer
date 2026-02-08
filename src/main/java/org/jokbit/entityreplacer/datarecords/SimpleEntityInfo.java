package org.jokbit.entityreplacer.datarecords;

import net.minecraft.resources.ResourceKey;


public record SimpleEntityInfo (
        String mobId,
        double x,
        double y,
        double z,
        ResourceKey<net.minecraft.world.level.Level> levelKey
){

}
