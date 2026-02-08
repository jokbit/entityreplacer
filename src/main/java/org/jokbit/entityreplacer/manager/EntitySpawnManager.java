package org.jokbit.entityreplacer.manager;

import com.google.common.collect.Maps;
import org.jokbit.entityreplacer.Config;
import org.jokbit.entityreplacer.datarecords.ReplaceData;
import org.jokbit.entityreplacer.datarecords.ReplaceInfo;
import org.jokbit.entityreplacer.datarecords.Replacer;
import org.jokbit.entityreplacer.datarecords.ReplacerSpawnInfo;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitySpawnManager {

    private static final int INIT_CAPACITY = 600;

    private static EntitySpawnManager INSTANCE;

    private final Deque<ReplacerSpawnInfo> replacerSpawnInfos = new ArrayDeque<>(INIT_CAPACITY);

    private final Map<String, ReplaceInfo> replaceMap = Maps.newHashMap();

    private EntitySpawnManager() {
        this.init();
    }

    private void init() {
        replaceMap.putAll(
                Config.getReplaceData()
                        .stream()
                        .map(replaceData -> replaceData
                                .replaceds()
                                .stream()
                                .map(mobId -> Map.entry(mobId,
                                        new ReplaceInfo(
                                                replaceData.replacers()
                                                        .stream()
                                                        .filter(replacer -> Config.getReplaceData()
                                                                .stream()
                                                                .map(ReplaceData::replaceds)
                                                                .flatMap(Collection::stream)
                                                                .noneMatch((replaced) -> replaced.equals(mobId)))
                                                        .toList(),
                                                replaceData.chance(),
                                                replaceData.rolls(),
                                                replaceData.additionMode(),
                                                replaceData.existed(),
                                                replaceData.replacers()
                                                        .stream()
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
                                        (1 - replaceInfo.chance()) * 1 - (replaceInfo2.chance()),
                                        replaceInfo.rolls() + replaceInfo2.rolls(),
                                        replaceInfo.additionMode() && replaceInfo2.additionMode(),
                                        replaceInfo.existed() && replaceInfo2.existed(),
                                        Stream.concat(
                                                        replaceInfo.replacers().stream(),
                                                        replaceInfo2.replacers().stream()
                                                )
                                                .mapToInt(Replacer::weight)
                                                .sum()
                                ))));
    }

    public boolean hasSpawnInfo() {
        return !replacerSpawnInfos.isEmpty();
    }

    public static EntitySpawnManager getInstance() {
        if (INSTANCE == null) {
            synchronized (EntitySpawnManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EntitySpawnManager();
                }
            }
        }
        return INSTANCE;
    }

    public boolean needReplaceEntity() {
        return !this.replaceMap.isEmpty();
    }

    public Optional<ReplaceInfo> findReplaceInfoByMobId(String mobId) {
        return Optional.ofNullable(replaceMap.get(mobId));
    }

    public Optional<Replacer> chooseReplacer(String mobId) {
        return this.findReplaceInfoByMobId(mobId)
                .map((replaceInfo -> {
                    if (replaceInfo.replacers().isEmpty()) {
                        return null;
                    }
                    int random = ThreadLocalRandom.current().nextInt(replaceInfo.totalWeight()) + 1;
                    int pos = Arrays.binarySearch(replaceInfo.weightList(), random);
                    pos = Math.min(pos >= 0 ? pos : -pos - 1, replaceInfo.replacers().size() - 1);
                    return replaceInfo.replacers().get(pos);
                }));
    }

    public boolean containReplaced(String mobId) {
        return this.replaceMap.containsKey(mobId);
    }

    public void offer(ReplacerSpawnInfo replacerSpawnInfo) {
        replacerSpawnInfos.offer(replacerSpawnInfo);
    }

    public Optional<ReplacerSpawnInfo> poll() {
        return Optional.ofNullable(replacerSpawnInfos.poll());
    }
}
