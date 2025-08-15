package org.gauntletbuddy.utility;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class InstanceTileUtil {
    private Map<WorldPoint, WorldPoint> offsetCache = new HashMap<>();

    public void resetCache() {
        offsetCache = new HashMap<>();
    }

    public void addPoint(LocalPoint local, WorldPoint candidate, Client client) {
        WorldPoint relative = WorldPoint.fromLocalInstance(client, local);
        if (offsetCache.get(relative) != null) return;
        WorldPoint planed = new WorldPoint(candidate.getX(), candidate.getY(), 1);
        offsetCache.put(relative, planed);
    }

    @Nullable
    public WorldPoint getRealPoint(WorldPoint instanced) {
        return offsetCache.get(instanced);
    }
}
