package com.mahjong.match.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board {
    public List<Tile> tiles = new ArrayList<>();
    private Random rng = new Random();

    // Build board from layout positions and assign tile types
    public void build(int[][] positions, long seed) {
        tiles.clear();
        rng = new Random(seed);

        int n = positions.length;
        if (n % 2 != 0) n--; // must be even

        // Generate tile types: n/2 pairs
        List<Integer> typeList = new ArrayList<>();
        List<Integer> subTypeList = new ArrayList<>();
        int pairs = n / 2;

        // Distribute pairs across types
        // We have 36 tile types. Flowers (34) have 4 sub-types, Seasons (35) have 4 sub-types.
        // Standard: each of types 0-33 has exactly 4 copies (2 pairs), flower/season have 1 each
        // For flexible pair count, cycle through types

        // Create type pool: 0-33 each x4, flower x4 (4 subs), season x4 (4 subs) = 144 tiles / 72 pairs
        List<int[]> pairPool = new ArrayList<>();
        for (int t = 0; t <= 33; t++) {
            pairPool.add(new int[]{t, 0});
            pairPool.add(new int[]{t, 0});
        }
        // Flowers: 4 unique sub-types
        pairPool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_MEI});
        pairPool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_LAN});
        pairPool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_JU});
        pairPool.add(new int[]{Tile.TYPE_FLOWER, Tile.FLOWER_ZHU});
        // Seasons: 4 unique sub-types
        pairPool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_SPRING});
        pairPool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_SUMMER});
        pairPool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_AUTUMN});
        pairPool.add(new int[]{Tile.TYPE_SEASON, Tile.SEASON_WINTER});

        // Shuffle pair pool
        Collections.shuffle(pairPool, rng);

        // Take 'pairs' pairs from pool (wrap around if needed)
        List<int[]> selected = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            int[] pair = pairPool.get(i % pairPool.size());
            selected.add(new int[]{pair[0], pair[1]});
            selected.add(new int[]{pair[0], pair[1]});
        }
        Collections.shuffle(selected, rng);

        // Create tiles
        for (int i = 0; i < n; i++) {
            int[] pos = positions[i];
            Tile tile = new Tile(pos[0], pos[1], pos[2], selected.get(i)[0]);
            tile.subType = selected.get(i)[1];
            tiles.add(tile);
        }
    }

    // A tile is "free" if:
    // 1. Not covered (no tile on top overlapping it)
    // 2. Free on left OR free on right (not sandwiched)
    public boolean isFree(Tile tile) {
        if (tile.removed) return false;
        if (isCovered(tile)) return false;
        boolean blockedLeft = isBlockedLeft(tile);
        boolean blockedRight = isBlockedRight(tile);
        return !blockedLeft || !blockedRight;
    }

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
