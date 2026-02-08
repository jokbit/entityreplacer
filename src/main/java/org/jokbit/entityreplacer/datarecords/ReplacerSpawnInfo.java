package org.jokbit.entityreplacer.datarecords;


public record ReplacerSpawnInfo(
        SimpleEntityInfo originEntityInfo,
        ReplaceInfo replaceInfo
){
}
