package org.jokbit.entityreplacer.json;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.lang.reflect.Type;


public class CompoundTagTypeAdapter implements JsonSerializer<CompoundTag>, JsonDeserializer<CompoundTag> {
    
    @Override
    public CompoundTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        
        if (json.isJsonNull()) {
            return new CompoundTag();
        }
        
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
            return new CompoundTag();
        }
        
        String nbtString = json.getAsString();
        
        if (nbtString == null || nbtString.trim().isEmpty()) {
            return new CompoundTag();
        }
        
        try {
            return TagParser.parseTag(nbtString);
        } catch (CommandSyntaxException e) {
            return new CompoundTag();
        }
    }
    
    @Override
    public JsonElement serialize(CompoundTag src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        
        return new JsonPrimitive(src.toString());
    }
}