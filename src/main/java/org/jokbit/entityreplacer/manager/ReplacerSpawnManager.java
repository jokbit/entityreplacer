package org.jokbit.entityreplacer.manager;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import org.jokbit.entityreplacer.Config;
import org.jokbit.entityreplacer.datarecords.ReplaceData;
import org.jokbit.entityreplacer.datarecords.ReplaceInfo;
import org.jokbit.entityreplacer.datarecords.Replacer;
import org.jokbit.entityreplacer.datarecords.SimpleEntityInfo;
import org.jokbit.entityreplacer.thread.ReplacerExecutor;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReplacerSpawnManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile ReplacerSpawnManager INSTANCE;

    public static final String KEY_REPLACED_TAG = "entity_replaced";

    public final Queue<SimpleEntityInfo> replacerInfos = new ConcurrentLinkedDeque<>();

    public final Map<String, ReplaceInfo> spawnReplaceMap = Maps.newHashMap();

    public final Map<String, ReplaceInfo> joinedReplaceMap = Maps.newHashMap();

    private ReplacerSpawnManager() {
        this.init();

    }

    public void reload() {
        spawnReplaceMap.clear();
        joinedReplaceMap.clear();
        replacerInfos.clear();
        init();
    }

    private void init() {
        spawnReplaceMap.putAll(
                Config.getReplaceData()
                        .stream()
                        .filter(replaceData -> !replaceData.join())
                        .map(replaceData -> replaceData
                                .replaceds()
                                .stream()
                                .map(mobId -> Map.entry(mobId,
                                        new ReplaceInfo(
                                                replaceData.replacers()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .toList(),
                                                replaceData.chance(),
                                                replaceData.rolls(),
                                                replaceData.additionMode(),
                                                replaceData.replacers()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .mapToInt(Replacer::weight)
                                                        .sum()
                                        )))
                                .collect(Collectors.toUnmodifiableSet())
                        )
                        .flatMap((Function<Set<Map.Entry<String, ReplaceInfo>>,
                                Stream<Map.Entry<String, ReplaceInfo>>>) Collection::stream)
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                Map.Entry::getValue,
                                (replaceInfo, replaceInfo2) -> new ReplaceInfo(
                                        Stream.concat(
                                                        replaceInfo
                                                                .replacers()
                                                                .stream(),
                                                        replaceInfo2
                                                                .replacers()
                                                                .stream())
                                                .toList(),
                                        1 - ((1.0 - replaceInfo.chance()) * (1.0 - replaceInfo2.chance())),
                                        replaceInfo.rolls() + replaceInfo2.rolls(),
                                        replaceInfo.additionMode() && replaceInfo2.additionMode(),
                                        Stream.concat(
                                                        replaceInfo.replacers().stream(),
                                                        replaceInfo2.replacers().stream()
                                                )
                                                .mapToInt(Replacer::weight)
                                                .sum()
                                ))));
        joinedReplaceMap.putAll(
                Config.getReplaceData()
                        .stream()
                        .filter(ReplaceData::join)
                        .map(replaceData -> replaceData
                                .replaceds()
                                .stream()
                                .map(mobId -> Map.entry(mobId,
                                        new ReplaceInfo(
                                                replaceData.replacers()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .toList(),
                                                replaceData.chance(),
                                                replaceData.rolls(),
                                                replaceData.additionMode(),
                                                replaceData.replacers()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .mapToInt(Replacer::weight)
                                                        .sum()
                                        )))
                                .collect(Collectors.toUnmodifiableSet())
                        )
                        .flatMap((Function<Set<Map.Entry<String, ReplaceInfo>>,
                                Stream<Map.Entry<String, ReplaceInfo>>>) Collection::stream)
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                Map.Entry::getValue,
                                (replaceInfo, replaceInfo2) -> new ReplaceInfo(
                                        Stream.concat(
                                                        replaceInfo
                                                                .replacers()
                                                                .stream(),
                                                        replaceInfo2
                                                                .replacers()
                                                                .stream())
                                                .toList(),
                                        1 - ((1.0 - replaceInfo.chance()) * (1.0 - replaceInfo2.chance())),
                                        replaceInfo.rolls() + replaceInfo2.rolls(),
                                        replaceInfo.additionMode() && replaceInfo2.additionMode(),
                                        Stream.concat(
                                                        replaceInfo.replacers().stream(),
                                                        replaceInfo2.replacers().stream()
                                                )
                                                .mapToInt(Replacer::weight)
                                                .sum()
                                ))));
    }

    public static ReplacerSpawnManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ReplacerSpawnManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ReplacerSpawnManager();
                }
            }
        }
        return INSTANCE;
    }

    public boolean hasReplacerWaitToSummon() {
        return !this.replacerInfos.isEmpty();
    }

    public boolean hasSpawnReplaced() {
        return !this.spawnReplaceMap.isEmpty();
    }

    public boolean hasJoinedReplaced() {
        return !this.joinedReplaceMap.isEmpty();
    }

    public boolean containSpawnReplaced(String mobId) {
        return this.spawnReplaceMap.containsKey(mobId);
    }

    public boolean containJoinedReplaced(String mobId) {
        return this.joinedReplaceMap.containsKey(mobId);
    }

    public boolean testSpawn(String mobId) {
        if (!containSpawnReplaced(mobId)) {
            return false;
        }
        ReplaceInfo replaceInfo = this.spawnReplaceMap.get(mobId);
        if (replaceInfo == null) {
            return false;
        }
        return Math.random() <= replaceInfo.chance();
    }

    public boolean testJoin(String mobId) {
        if (!containJoinedReplaced(mobId)) {
            return false;
        }
        ReplaceInfo replaceInfo = this.joinedReplaceMap.get(mobId);
        if (replaceInfo == null) {
            return false;
        }
        return Math.random() <= replaceInfo.chance();
    }

    public void catchSpawnedReplaced(SimpleEntityInfo replaced) {
        ReplacerExecutor.getInstance().submit(() -> catchReplaced(this.spawnReplaceMap, replaced));
    }

    public void catchJoinedReplaced(SimpleEntityInfo replaced) {
        ReplacerExecutor.getInstance().submit(() -> catchReplaced(this.joinedReplaceMap, replaced));
    }

    private void catchReplaced(Map<String, ReplaceInfo> replaceMap, SimpleEntityInfo replaced) {
        ReplaceInfo replaceInfo = replaceMap.get(replaced.mobId());
        if (replaceInfo == null) {
            return;
        }

        for (int i = 0; i < replaceInfo.rolls(); i++) {
            int random = ThreadLocalRandom.current().nextInt(replaceInfo.totalWeight()) + 1;
            int pos = Arrays.binarySearch(replaceInfo.weightList(), random);
            int index = Math.min(pos >= 0 ? pos : -pos - 1, replaceInfo.replacers().size() - 1);
            Replacer replacer = replaceInfo.replacers().get(index);
            if (replacer == null) {
                return;
            }
            SimpleEntityInfo replacerInfo =
                    new SimpleEntityInfo(
                            replacer.mobId(),
                            replaced.x(),
                            replaced.y(),
                            replaced.z(),
                            replacer.nbt(),
                            replaced.levelKey()
                    );
            replacerInfos.offer(replacerInfo);
        }
    }

    public Optional<SimpleEntityInfo> pollReplacer() {
        return Optional.ofNullable(this.replacerInfos.poll());
    }
}
