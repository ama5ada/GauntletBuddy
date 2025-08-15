package org.gauntletbuddy.modules;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class TornadoTracker {
    // Track the tiles that are safe from tornadoes on each tick
    // Start from 11 ticks remaining precomputing for 10 -> 1 ticks
    //
    // Stores the current tiles to highlight
    @Getter
    private Set<WorldPoint> highlightTiles = new HashSet<>();
    @Getter
    private int currentTick;

    private HashMap<Set<WorldPoint>, Set<WorldPoint>> possibleHighlightTiles = new HashMap<>();

    private final int CORRUPTED_X_LOWER = 1970;
    private final int CORRUPTED_Y_LOWER = 5682;
    private final int CORRUPTED_X_UPPER = 1981;
    private final int CORRUPTED_Y_UPPER = 5693;

    private final int GAUNTLET_X_LOWER = 1906;
    private final int GAUNTLET_Y_LOWER = 5682;
    private final int GAUNTLET_X_UPPER = 1917;
    private final int GAUNTLET_Y_UPPER = 5693;

    private final int GAUNTLET_PLANE = 1;

    @Getter
    private int X_LOWER;
    @Getter
    private int Y_LOWER;
    @Getter
    private int X_UPPER;
    @Getter
    private int Y_UPPER;

    @Inject
    private Client client;

    private final List<Point> adjacencies = Arrays.asList(
            new Point(-1, -1),
            new Point(-1, 0),
            new Point(-1, 1),
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 0),
            new Point(1, -1),
            new Point(0, -1)
    );

    @Getter
    private List<List<WorldPoint>> bossRoomTiles;

    // Generate all sets of possible tornadoes in the next step along with their dangerous tiles
    private void buildNextToradoes(int tick, Set<WorldPoint> tornadoPoints) {
        if (tick < 0) {
            reset();
            return;
        }
        // Step 1 is get all possible adjacent tiles by current tile
        HashMap<WorldPoint, List<WorldPoint>> adjacentTiles = new HashMap<>();
        for (final WorldPoint currentPoint : tornadoPoints) {
            if (adjacentTiles.containsKey(currentPoint)) continue;
            List<WorldPoint> currentAdjacencies = getPossibleTornadoSteps(currentPoint);
            adjacentTiles.put(currentPoint, currentAdjacencies);
        }
        // Step 2 get all tiles that could be made dangerous by any possible adjacent tile
        HashMap<WorldPoint, List<WorldPoint>> dangerousTiles = getDangerousTiles(tick, adjacentTiles);
        // Step 3 Create all possible adjacent tile combinations
        List<Set<WorldPoint>> tileCombinations = new ArrayList<>();
        buildPointCombinations(tileCombinations, new HashSet<>(), adjacentTiles);
        // Step 4 Join the sets of possible dangerous tiles of the tiles in all possible adjacent tile combinations
        possibleHighlightTiles = new HashMap<>();
        for (Set<WorldPoint> currPoints : tileCombinations) {
            List<WorldPoint> combinationTiles = new ArrayList<>();
            for (WorldPoint curr : currPoints) {
                combinationTiles.addAll(dangerousTiles.get(curr));
            }
            possibleHighlightTiles.put(currPoints, new HashSet<>(combinationTiles));
        }
    }

    private void buildPointCombinations(List<Set<WorldPoint>> combinations, Set<WorldPoint>base,
                                        HashMap<WorldPoint, List<WorldPoint>> options) {
        if (options.isEmpty()) {
            combinations.add(new HashSet<>(base));
            return;
        }
        
        WorldPoint temp = options.keySet().iterator().next();
        List<WorldPoint> items = options.get(temp);
        HashMap<WorldPoint, List<WorldPoint>> copy = new HashMap<>(options);
        copy.remove(temp);
        
        for (WorldPoint curr : items) {
            Set<WorldPoint> newBase = new HashSet<>(base);
            newBase.add(curr);
            buildPointCombinations(combinations, newBase, copy);
        }
    }

    private HashMap<WorldPoint, List<WorldPoint>> getDangerousTiles(int tick, HashMap<WorldPoint,
            List<WorldPoint>> adjacentTiles) {
        // Get a set of base tiles (possible next steps for tornadoes) to identify tiles close enough to be dangerous
        Set<WorldPoint> baseTiles = new HashSet<>();
        for (final WorldPoint parentPoint : adjacentTiles.keySet()) {
            baseTiles.addAll(adjacentTiles.get(parentPoint));
        }
        HashMap<WorldPoint, List<WorldPoint>> dangerousTiles = new HashMap<>();
        for (final WorldPoint adjacentPoint : baseTiles) {
            List<WorldPoint> currentDanger = new ArrayList<>();
            int x_low = Math.max(adjacentPoint.getX() - tick , this.X_LOWER);
            int x_high = Math.min(adjacentPoint.getX() + tick, this.X_UPPER);
            int y_low = Math.max(adjacentPoint.getY() - tick, this.Y_LOWER);
            int y_high = Math.min(adjacentPoint.getY() + tick, this.Y_UPPER);
            int temp = 0;
            for (int y = y_low; y <= y_high; y++) {
                for (int x = x_low; x <= x_high; x++) {
                    int list_x = x - this.X_LOWER;
                    int list_y = y - this.Y_LOWER;
                    currentDanger.add(bossRoomTiles.get(list_y).get(list_x));
                    temp += 1;
                }
            }
            System.out.println(temp);
            dangerousTiles.put(adjacentPoint, currentDanger);
        }
        return dangerousTiles;
    }

    private List<WorldPoint> getPossibleTornadoSteps(WorldPoint currentPoint) {
        List<WorldPoint> possibleSteps = new ArrayList<>();
        int wpx = currentPoint.getX();
        int wpy = currentPoint.getY();
        for (Point pair : adjacencies) {
            int apx = wpx + pair.x;
            int apy = wpy + pair.y;
            if (X_LOWER <= apx  && apx <= X_UPPER && Y_LOWER <= apy && apy <= Y_UPPER) {
                WorldPoint adjacentPoint = new WorldPoint(apx, apy, currentPoint.getPlane());
                possibleSteps.add(adjacentPoint);
            }
        }
        return possibleSteps;
    }

    private Set<WorldPoint> getTrueTornadoTiles(List<NPC> tornadoList) {
        Set<WorldPoint> tornadoPoints = new HashSet<>();
        for (final NPC tornado : tornadoList) {
            WorldPoint wp = tornado.getWorldLocation();
            LocalPoint trueTile = LocalPoint.fromWorld(client, wp);
            WorldPoint relativeWorldPoint = WorldPoint.fromLocalInstance(client, trueTile);
            tornadoPoints.add(relativeWorldPoint);
        }
        return tornadoPoints;
    }

    public void updateCache(int tick, List<NPC> tornadoList) {
        currentTick = tick;
        Set<WorldPoint> tornadoPoints = getTrueTornadoTiles(tornadoList);
        highlightTiles = possibleHighlightTiles.getOrDefault(tornadoPoints, new HashSet<>());
        if (tick > 7) return;
        CompletableFuture.runAsync(() -> {
            buildNextToradoes(tick - 1, tornadoPoints);
        });
    }

    public void reset() {
        highlightTiles = new HashSet<>();
        possibleHighlightTiles = new HashMap<>();
        currentTick = -1;
    }

    public void setConstants(boolean corrupted) {
        if (corrupted) {
            this.X_LOWER = CORRUPTED_X_LOWER;
            this.Y_LOWER = CORRUPTED_Y_LOWER;
            this.X_UPPER = CORRUPTED_X_UPPER;
            this.Y_UPPER = CORRUPTED_Y_UPPER;
        } else {
            this.X_LOWER = GAUNTLET_X_LOWER;
            this.Y_LOWER = GAUNTLET_Y_LOWER;
            this.X_UPPER = GAUNTLET_X_UPPER;
            this.Y_UPPER = GAUNTLET_Y_UPPER;
        }
        bossRoomTiles = new ArrayList<>();
        for (int y = this.Y_LOWER; y <= this.Y_UPPER; y ++) {
            List<WorldPoint> rowTiles = new ArrayList<>();
            for (int x = this.X_LOWER; x <= this.X_UPPER; x++) {
                rowTiles.add(new WorldPoint(x, y, GAUNTLET_PLANE));
            }
            bossRoomTiles.add(rowTiles);
        }
    }
}
