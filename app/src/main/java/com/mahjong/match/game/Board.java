package com.mahjong.match.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board {
    public List<Tile> tiles = new ArrayList<>();
    private Random rng = new Random();

    /**
     * Build a guaranteed-solvable board using backwards simulation.
     * Instead of randomly assigning types (which may create deadlocks),
     * we repeatedly pick free-tile pairs and assign them matching types.
     * This ensures at least one valid solution exists.
     */
    public void build(int[][] positions, long seed) {
        tiles.clear();
        rng = new Random(seed);

        int n = positions.length;
        if (n % 2 != 0) n--;

        // Create all tiles with placeholder type
        for (int i = 0; i < n; i++) {
            int[] pos = positions[i];
            Tile tile = new Tile(pos[0], pos[1], pos[2], 0);
            tiles.add(tile);
        }

        // Try solvable generation up to 20 times with different sub-seeds
        for (int attempt = 0; attempt < 20; attempt++) {
            if (assignSolvable(new ArrayList<>(tiles), buildTypePool(n / 2, rng), new Random(seed + attempt * 7919L))) {
                return;
            }
        }
        // Fallback: random assignment (shouldn't reach here for valid layouts)
        assignRandom(n, seed);
    }

    /**
     * Reshuffle remaining tiles in a solvable way (used by GameEngine.shuffle).
     * Tiles passed in must be the current non-removed tiles.
     */
    public void reshuffleSolvable(List<Tile> remainingTiles, Random rng) {
        if (remainingTiles.size() < 2) return;

        // Collect type pool from current remaining tiles
        List<int[]> typePool = new ArrayList<>();
        for (Tile t : remainingTiles) {
            typePool.add(new int[]{t.type, t.subType});
        }
        Collections.shuffle(typePool, rng);

        // Try solvable assignment up to 20 times
        for (int attempt = 0; attempt < 20; attempt++) {
            List<int[]> pool = new ArrayList<>(typePool);
            Collections.shuffle(pool, new Random(rng.nextLong()));
            if (assignSolvable(new ArrayList<>(remainingTiles), pool, new Random(rng.nextLong()))) {
                return;
            }
        }
        // Fallback: just assign in shuffled order (maintains pairs via typePool)
        assignPoolToTiles(remainingTiles, typePool, rng);
    }

    /**
     * Core solvable assignment algorithm.
     * Simulates game play: repeatedly finds free tiles and assigns them pair types.
     * Returns true if all tiles were assigned successfully.
     */
    private boolean assignSolvable(List<Tile> tileList, List<int[]> typePool, Random rng) {
        // Reset type assignments for simulation
        for (Tile t : tileList) {
            t.type = 0;
            t.removed = false;
        }

        List<Tile> working = new ArrayList<>(tileList);
        int poolIdx = 0;

        while (working.size() >= 2) {
            List<Tile> free = new ArrayList<>();
            for (Tile t : working) {
                if (isFree(t)) free.add(t);
            }

            if (free.size() < 2) {
                // Stuck — reset and signal failure
                for (Tile t : tileList) t.removed = false;
                return false;
            }

            Collections.shuffle(free, rng);
            Tile a = free.get(0);
            Tile b = free.get(1);

            int[] tp = typePool.get(poolIdx % typePool.size());
            a.type = tp[0]; a.subType = tp[1];
            b.type = tp[0]; b.subType = tp[1];
            poolIdx++;

            // Mark removed to simulate this pair being matched
            a.removed = true;
            b.removed = true;
            working.remove(a);
            working.remove(b);
        }

        // Success — reset removed flags
        for (Tile t : tileList) t.removed = false;
        return true;
    }

    /** Assign type pool to tiles directly (fallback, maintains pairs but no solvability guarantee). */
    private void assignPoolToTiles(List<Tile> tileList, List<int[]> typePool, Random rng) {
        List<Tile> shuffled = new ArrayList<>(tileList);
        Collections.shuffle(shuffled, rng);
        for (int i = 0; i < shuffled.size() && i < typePool.size(); i++) {
            shuffled.get(i).type = typePool.get(i)[0];
            shuffled.get(i).subType = typePool.get(i)[1];
        }
        for (Tile t : tileList) t.removed = false;
    }

    /** Random assignment fallback for build() — no solvability guarantee. */
    private void assignRandom(int n, long seed) {
        rng = new Random(seed + 999999L);
        List<int[]> typePool = buildTypePool(n / 2, rng);
        List<int[]> selected = new ArrayList<>();
        for (int[] tp : typePool) {
            selected.add(tp);
            selected.add(tp);
        }
        Collections.shuffle(selected, rng);
        for (int i = 0; i < tiles.size() && i < selected.size(); i++) {
            tiles.get(i).type = selected.get(i)[0];
            tiles.get(i).subType = selected.get(i)[1];
        }
    }

    /** Build a shuffled type pool with the given number of pairs. */
    private List<int[]> buildTypePool(int pairs, Random rng) {
        // 34 regular types × 2 = 68 pairs; flowers × 4 + seasons × 4 = 8 more → 76 total
        List<int[]> pool = new ArrayList<>();
        for (int t = 0; t <= 33; t++) {
            pool.add(new int[]{t, 0});
            pool.add(new int[]{t, 0});
        }
        pool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_MEI});
        pool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_LAN});
        pool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_JU});
        pool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_ZHU});
        pool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_SPRING});
        pool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_SUMMER});
        pool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_AUTUMN});
        pool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_WINTER});

        Collections.shuffle(pool, rng);

        // Return exactly 'pairs' entries (cycle if board is larger than standard)
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            result.add(pool.get(i % pool.size()));
        }
        return result;
    }

    // A tile is "free" if:
    // 1. Not removed
    // 2. Not covered (no tile on top overlapping it)
    // 3. Free on left OR free on right (not sandwiched)
    public boolean isFree(Tile tile) {
        if (tile.removed) return false;
        if (isCovered(tile)) return false;
        boolean blockedLeft = isBlockedLeft(tile);
        boolean blockedRight = isBlockedRight(tile);
        return !blockedLeft || !blockedRight;
    }

    /** Public version of {@link #isCovered} so the renderer can hide obscured tiles. */
    public boolean hasCoverAbove(Tile a) { return isCovered(a); }

    // Tile B covers tile A if: B.z == A.z+1 AND |B.x - A.x| <= 1 AND |B.y - A.y| <= 1
    private boolean isCovered(Tile a) {
        for (Tile b : tiles) {
            if (b.removed || b == a) continue;
            if (b.z == a.z + 1 && Math.abs(b.x - a.x) <= 1 && Math.abs(b.y - a.y) <= 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlockedLeft(Tile a) {
        for (Tile b : tiles) {
            if (b.removed || b == a) continue;
            if (b.z == a.z && b.x == a.x - 2 && Math.abs(b.y - a.y) <= 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlockedRight(Tile a) {
        for (Tile b : tiles) {
            if (b.removed || b == a) continue;
            if (b.z == a.z && b.x == a.x + 2 && Math.abs(b.y - a.y) <= 1) {
                return true;
            }
        }
        return false;
    }

    public List<Tile> getFreeTiles() {
        List<Tile> free = new ArrayList<>();
        for (Tile t : tiles) {
            if (!t.removed && isFree(t)) free.add(t);
        }
        return free;
    }

    public int getRemainingCount() {
        int count = 0;
        for (Tile t : tiles) if (!t.removed) count++;
        return count;
    }

    public boolean isComplete() {
        return getRemainingCount() == 0;
    }

    // Returns a matching pair from free tiles, or null if stuck
    public Tile[] findHint() {
        List<Tile> free = getFreeTiles();
        for (int i = 0; i < free.size(); i++) {
            for (int j = i + 1; j < free.size(); j++) {
                if (free.get(i).matches(free.get(j))) {
                    return new Tile[]{free.get(i), free.get(j)};
                }
            }
        }
        return null;
    }

    public boolean hasValidMove() {
        return findHint() != null;
    }

    public void clearHighlights() {
        for (Tile t : tiles) t.highlighted = false;
    }

    public void clearSelection() {
        for (Tile t : tiles) t.selected = false;
    }
}
